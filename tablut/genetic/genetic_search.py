"""
Date: 05/11/2020
Author: Carlo Cena & Giacomo Zamprogno

Population-based search for best weight of heuristic's component.
"""
from tablut.client.tablut_client import Client
import numpy as np

N_POP = 200  # Number of solutions in population.
NUM_MATCH = 1  # Percentage of solutions to play against.
N_PARAM = 8
MAX_PARAM_VALUE = 500
MIN_PARAM_VALUE = -500
MAX_ITER = 5000
PERC_NEW_POP = .5


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
    prob_surv /= 3*num_games
    return prob_surv


def mate(sol1, sol2):
    """
    Create a new individual by applying crossover.
    """
    perc_sol1 = np.random.rand()
    perc_sol2 = np.random.rand()
    start_index_sol1 = int(N_PARAM * perc_sol1) - 1 if np.random.rand() > 0.5 else 0
    sol1_newborn = int(N_PARAM * perc_sol1) -1 if np.random.rand() > 0.5 else 0
    start_index_sol2 = int(N_PARAM * perc_sol2) - 1 if np.random.rand() > 0.5 else 0
    m_newborn = []
    for i in range(0, int(perc_sol1*N_PARAM)):
        m_newborn[sol1_newborn+i] = sol1[start_index_sol1+i]
    if sol1_newborn == 0:
        for i in range(0, int(perc_sol2*N_PARAM)):
            m_newborn[sol1_newborn+perc_sol1*N_PARAM+1+i] = sol2[start_index_sol2+i]
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
            if random_mutation > prob_surv[i]:
                if np.random.rand() > 0.5:
                    solutions[j][param_index] += int((random_mutation-prob_surv[j])*solutions[j][param_index])
                else:
                    solutions[j][param_index] -= int((random_mutation - prob_surv[j]) * solutions[j][param_index])


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
while num_iter <= MAX_ITER:
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
        mutations(population, prob_survival)  # Mutations hit less strong individual with higher probability
        i += 1
    prob_survival = eval_pop(population)  # Population evaluation
    num_iter += 1

index_best_sol = find_best_sol(population)
print(population[index_best_sol])


