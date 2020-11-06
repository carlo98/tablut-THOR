"""
Date: 05/11/2020
Author: Carlo Cena & Giacomo Zamprogno

Population-based search for best weight of heuristic's component.
"""
from tablut.client.tablut_client import Client
import numpy as np

N_POP = 200  # Number of solutions in population.
NUM_MATCH = 1  # Percentage of solutions to play against.
N_PARAM = 8  # Number of parameter of each solution
MAX_PARAM_VALUE = 500  # Maximum value allowed for each parameter
MIN_PARAM_VALUE = -500  # Minimum value allowed for each parameter
MAX_ITER = 5000  # Maximum number of iterations
PERC_NEW_POP = .3  # Percentage of new individuals at each iteration
EPS = MAX_PARAM_VALUE / 5  # Maximum change of each parameter due to mutation
MAX_ITER_NO_BETTER = 100  # Maximum number of iterations without better solution


def eval_match(sol1, sol2):
    """
    Starts two matches between a couple of solutions,
    returns point earned by each one.
    """
    sol1_points = 0
    sol2_points = 0
    client_white = Client("5000", "WHITE", 60, weights=sol1)
    client_black = Client("5001", "BLACK", 60, weights=sol2)
    result = client_white.run()
    client_black.run()
    if result == "win":
        sol1_points += 3
    elif result == "loose":
        sol2_points += 3
    else:
        sol1_points += 1
        sol2_points += 1

    client_white = Client("5000", "WHITE", 60, weights=sol2)
    client_black = Client("5001", "BLACK", 60, weights=sol1)
    result = client_white.run()
    client_black.run()
    if result == "win":
        sol2_points += 3
    elif result == "loose":
        sol1_points += 3
    else:
        sol2_points += 1
        sol1_points += 1

    return sol1_points, sol2_points


def eval_pop(solutions):
    """
    Evaluates solutions, returns a list of floats, between 0 and 1
    (probabilities of survival and reproduction).
    """
    prob_surv = [.0 for x in range(N_POP)]
    num_games = [0 for x in range(N_POP)]
    for index_sol1 in range(len(solutions)):
        old_index = index_sol1
        for l in range(NUM_MATCH):
            index_sol2 = np.random.randint(old_index, N_POP)
            while index_sol2 == old_index:
                index_sol2 = np.random.randint(0, N_POP)
            point_sol1, point_sol2 = eval_match(solutions[index_sol1], solutions[index_sol2])
            prob_surv[index_sol1] += point_sol1
            prob_surv[index_sol2] += point_sol2
            num_games[index_sol1] += 2
            num_games[index_sol2] += 2
            old_index = index_sol2
    prob_surv = np.array(prob_surv)
    prob_surv /= 3*num_games
    return prob_surv


def test_eval(solutions):
    max_dist = []
    for sol in solutions:
        par_first = 0
        par_second = 0
        for u in range(0, int(N_PARAM/2)):
            par_first += sol[u]
        for u in range(int(N_PARAM/2), N_PARAM):
            par_second += sol[u]
        max_dist.append(np.abs(float(par_first-par_second)) if par_first >= par_second else 0.0)

    max_dist = np.array(max_dist)
    max_dist /= 4000
    return max_dist


def mate(sol1, sol2):
    """
    Create a new individual by applying crossover.
    """
    perc_sol1 = np.random.randint(1, N_PARAM)
    perc_sol2 = N_PARAM-perc_sol1
    start_index_sol1 = np.random.randint(0, N_PARAM-perc_sol1)
    start_index_sol2 = np.random.randint(0, N_PARAM-perc_sol2)
    m_newborn = [0 for x in range(N_PARAM)]
    for p in range(0, perc_sol1):
        m_newborn[p] = sol1[start_index_sol1+p]
    for p in range(0, perc_sol2):
        m_newborn[perc_sol1+p] = sol2[start_index_sol2+p]
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


def find_best_sol(solutions):
    """
    Finding best solution, by evaluating all possible couples.
    """
    points = [0 for x in range(N_POP)]
    for index_sol1 in range(len(solutions)):
        for index_sol2 in range(len(solutions)):
            if index_sol2 > index_sol1:
                point_sol1, point_sol2 = eval_match(solutions[index_sol1], solutions[index_sol2])
                points[index_sol1] += point_sol1
                points[index_sol2] += point_sol2
    return np.argmax(points)


num_iter = 0
population = []
for i in range(N_POP):  # Randomly initializing population
    population.append([np.random.randint(MIN_PARAM_VALUE, MAX_PARAM_VALUE+1) for x in range(N_PARAM)])
prob_survival = eval_pop(population)  # First evaluation of pop
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
    prob_survival = eval_pop(population)  # Population evaluation
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

index_best_sol = find_best_sol(population)
print("Best sol: ", population[int(index_best_sol)])


