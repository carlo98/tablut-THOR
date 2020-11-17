"""
Date: 16/11/2020
Author: Carlo Cena

Population-based search for best weight of heuristic's component, using data obtained
by random matches.
"""
import numpy as np
import os
import pickle
from tablut.utils.state_utils import *
from tablut.utils.bitboards import *
from concurrent.futures import ProcessPoolExecutor

N_POP = 200  # Number of solutions in population.
N_PARAM = 6  # Number of parameter of each solution
MAX_PARAM_VALUE = 200  # Maximum value allowed for each parameter
MIN_PARAM_VALUE = 0  # Minimum value allowed for each parameter
MAX_ITER = 1000  # Maximum number of iterations
PERC_NEW_POP = .5  # Percentage of new individuals at each iteration
EPS = 10  # Maximum change of each parameter due to mutation
MAX_ITER_NO_BETTER = 10  # Maximum number of iterations without better solution
N_PROC = 8  # Keep N_POP/N_PROC integer
ERROR_ZERO = 5

def check_victory(state):
    if np.count_nonzero(state['king_bitboard']) == 0:
        "king captured"
        return -1
    if np.count_nonzero(state['king_bitboard'] & escapes_bitboard) != 0:
        "king escaped"
        return 1
    return 0


def open_king_paths(state):
    "king coordinates"
    king_row = np.nonzero(state['king_bitboard'])[0]
    king_bin_col = state['king_bitboard'][king_row]
    king_col = int(8 - np.log2(king_bin_col))

    "check for pawns/camps left and right"
    right_mask = 511 * np.ones(1, dtype=int)

    left_mask = (king_bin_col << 1) * np.ones(1, dtype=int)
    for col in range(0, king_col+1):
        right_mask >>= 1
        if col <= king_col - 2:
            left_mask ^= king_bin_col
            left_mask <<= 1

    "check for pawns/camps up and down"
    above_the_column = []
    below_the_column = []
    for row in range(0, 9):
        if row != king_row and row < king_row:
            above_the_column += list(bit(camps_bitboard[row])) + list(bit(state['white_bitboard'][row])) + \
                                list(bit(state['black_bitboard'][row]))
        elif row != king_row and row > king_row:
            below_the_column += list(bit(camps_bitboard[row])) + list(bit(state['white_bitboard'][row])) + \
                                list(bit(state['black_bitboard'][row]))
    open_paths = 4

    if (king_row in [3, 4, 5] or
            ((right_mask & state['white_bitboard'][king_row]) + (right_mask & state['black_bitboard'][king_row])
            + (right_mask & camps_bitboard[king_row]) != 0)):
        open_paths -= 1
    if (king_row in [3, 4, 5] or
            (left_mask & state['white_bitboard'][king_row]) + (left_mask & state['black_bitboard'][king_row])
            + (left_mask & camps_bitboard[king_row]) != 0):
        open_paths -= 1

    if king_col in [3, 4, 5] or king_bin_col in above_the_column:
        open_paths -= 1
    if king_col in [3, 4, 5] or king_bin_col in below_the_column:
        open_paths -= 1

    return open_paths


def compute_heuristic(s, color, weights):
    state = {'king_bitboard': np.array(s[0], np.int), 'white_bitboard': np.array(s[1], np.int),
             'black_bitboard': np.array(s[2], np.int)}
    "victory condition, of course"
    victory_cond = check_victory(state)
    if victory_cond == -1 and color == "BLACK":  # king captured and black player -> Win
        return -1
    elif victory_cond == -1 and color == "WHITE":  # King captured and white player -> Lose
        return 1
    elif victory_cond == 1 and color == "BLACK":  # King escaped and black player -> Lose
        return 1
    elif victory_cond == 1 and color == "WHITE":  # King escaped and white player -> Win
        return -1

    "if the exits are blocked, white has a strong disadvantage"
    blocks_occupied_by_black = np.count_nonzero(state['black_bitboard'] & blocks_bitboard)
    blocks_occupied_by_white = np.count_nonzero(state['white_bitboard'] & blocks_bitboard) + \
                               np.count_nonzero(state['king_bitboard'] & blocks_bitboard)
    coeff_min_black = (-1) ** (color == "BLACK")
    coeff_min_white = (-1) ** (color == "WHITE")
    blocks_cond = coeff_min_black * weights[0] * blocks_occupied_by_black \
                    + coeff_min_white * weights[1] * blocks_occupied_by_white
    open_blocks_cond = coeff_min_white * weights[2] * (8 - blocks_occupied_by_white - blocks_occupied_by_black)
    "remaining pieces are considered"
    white_cnt = 0
    black_cnt = 0
    for r in range(0, 9):
        for c in range(0, 9):
            curr_mask = 1 << (8 - c)
            if state['white_bitboard'][r] & curr_mask != 0:
                white_cnt += 1
            if state['black_bitboard'][r] & curr_mask != 0:
                black_cnt += 1

    remaining_whites_cond = coeff_min_white * weights[3] * white_cnt
    remaining_blacks_cond = coeff_min_black * weights[4] * black_cnt

    "aggressive king condition"
    ak_cond = coeff_min_white * weights[5] * open_king_paths(state)
    return_value = blocks_cond + remaining_whites_cond + remaining_blacks_cond + open_blocks_cond + ak_cond
    return return_value


def eval_pop_thread(args):
    """
    Evaluates solutions, returns a list of floats, between 0 and 1
    (probabilities of survival and reproduction).
    """
    m_solutions, m_state_hash_table, id_mi = args[0], args[1], args[2]
    step = int(N_POP/N_PROC)
    prob_surv = np.zeros(step)
    for index_sol in range(len(m_solutions)):
        print("Solution ", index_sol, " Id: ", id_mi)
        sol = m_solutions[index_sol]
        tmp_points = 0
        for state_key in m_state_hash_table:
            state = m_state_hash_table[state_key]
            tmp_w = compute_heuristic(state_key, 'WHITE', sol)
            tmp_b = compute_heuristic(state_key, 'BLACK', sol)
            if tmp_w < 0 and state['value']['white'] / state['games'] > 0.5:
                tmp_points += 1
            elif tmp_w > 0 and state['value']['black'] / state['games'] > 0.5:
                tmp_points += 1
            elif (tmp_w <= 0+ERROR_ZERO or tmp_w >= 0-ERROR_ZERO) and \
                    state['value']['black'] / state['games'] < 0.5 and state['value']['white'] / state['games'] < 0.5:
                tmp_points += 1
            if tmp_b < 0 and state['value']['black'] / state['games'] > 0.5:
                tmp_points += 1
            elif tmp_b > 0 and state['value']['white'] / state['games'] > 0.5:
                tmp_points += 1
            elif (tmp_b <= 0+ERROR_ZERO or tmp_b >= 0-ERROR_ZERO) and \
                    state['value']['black'] / state['games'] < 0.5 and state['value']['white'] / state['games'] < 0.5:
                tmp_points += 1
        tmp_points /= 2
        prob_surv[index_sol] = tmp_points
    return prob_surv


def eval_pop(solutions, m_state_hash_table):
    pool = ProcessPoolExecutor(N_PROC)
    step = int(len(solutions)/N_PROC)
    args_list = []
    for i_m in range(N_PROC):
        args_list.append((solutions[step*i_m: step*i_m+step], m_state_hash_table, i_m))
    prob_surv_m = []
    for s_list in pool.map(eval_pop_thread, args_list):
        prob_surv_m += list(s_list)
    prob_surv_m = np.array(prob_surv_m)
    prob_surv_m /= len(m_state_hash_table)
    print(prob_surv_m)
    return prob_surv_m


def mate(sol1, sol2):
    """
    Create a new individual by applying crossover.
    Linear combination with random weights between 0 and 1
    """
    lambda_1 = np.random.rand()
    lambda_2 = np.random.rand()
    m_newborn = []
    for x in range(N_PARAM):
        new_value = lambda_1*sol1[x] + lambda_2*sol2[x]
        if new_value > MAX_PARAM_VALUE:
            new_value = MAX_PARAM_VALUE
        elif new_value < MIN_PARAM_VALUE:
            new_value = MIN_PARAM_VALUE
        m_newborn.append(new_value)
    return m_newborn


def add_newborn(m_newborn, solutions, prob_surv):
    """
    Removing solution based on strength to make room for new individual.
    """
    index_remove = np.random.randint(0, N_POP)
    while np.random.rand() <= prob_surv[index_remove]:
        index_remove = np.random.randint(0, N_POP)
    solutions[index_remove] = m_newborn


def mutations(solutions, prob_surv):
    """
    Mutating individuals based on their probability of survival.
    """
    for j in range(N_POP):
        number_poss_mutations = np.random.randint(0, N_PARAM)  # Choosing number of possible mutations
        for k in range(number_poss_mutations):
            param_index = np.random.randint(0, N_PARAM)  # Choosing parameter to mutate
            random_mutation = np.random.rand()
            if random_mutation > prob_surv[j]:
                if np.random.rand() > 0.5:
                    solutions[j][param_index] += int((random_mutation-prob_surv[j])*EPS)
                    if solutions[j][param_index] > MAX_PARAM_VALUE:
                        solutions[j][param_index] = MAX_PARAM_VALUE
                    elif solutions[j][param_index] < MIN_PARAM_VALUE:
                        solutions[j][param_index] = MIN_PARAM_VALUE
                else:
                    solutions[j][param_index] -= int((random_mutation - prob_surv[j]) * EPS)
                    if solutions[j][param_index] > MAX_PARAM_VALUE:
                        solutions[j][param_index] = MAX_PARAM_VALUE
                    elif solutions[j][param_index] < MIN_PARAM_VALUE:
                        solutions[j][param_index] = MIN_PARAM_VALUE


exists = os.path.isfile('state_hash')
if exists:
    file = open("state_hash", "rb")
    state_hash_table = pickle.load(file)
    file.close()

num_iter = 0
population = []
for i in range(N_POP):  # Randomly initializing population
    population.append([np.random.randint(MIN_PARAM_VALUE, MAX_PARAM_VALUE+1) for x in range(N_PARAM)])
prob_survival = eval_pop(population, state_hash_table)  # First evaluation of pop
best_sol = []
best_sol_prob = 0.0
no_best_sol = 0
while num_iter <= MAX_ITER and no_best_sol <= MAX_ITER_NO_BETTER:
    i = 0
    while i <= PERC_NEW_POP*N_POP:
        index_par1 = np.random.randint(0, N_POP)  # Picking random parents
        while np.random.rand() > prob_survival[index_par1]:
            index_par1 = np.random.randint(0, N_POP)  # Choosing parents based on their strength
        index_par2 = np.random.randint(0, N_POP)
        while index_par2 == index_par1 or np.random.rand() > prob_survival[index_par2]:
            index_par2 = np.random.randint(0, N_POP)
        newborn = mate(population[index_par1], population[index_par2])  # Creating new individual
        add_newborn(newborn, population, prob_survival)  # Add new individual by removing less strong old ones
        i += 1
    mutations(population, prob_survival)  # Mutations hit less strong individual with higher probability
    prob_survival = eval_pop(population, state_hash_table)  # Population evaluation
    best_sol_find_index = int(np.argmax(prob_survival))
    if best_sol_prob < prob_survival[best_sol_find_index]:
        best_sol = population[best_sol_find_index]
        best_sol_prob = prob_survival[best_sol_find_index]
        no_best_sol = 0
    print("Best sol: ", population[best_sol_find_index])
    print("Value: ", prob_survival[best_sol_find_index])
    print("Iteration: ", num_iter)
    num_iter += 1
    no_best_sol += 1