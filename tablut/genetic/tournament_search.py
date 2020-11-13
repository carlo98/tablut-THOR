"""
Date: 05/11/2020
Author: Carlo Cena & Giacomo Zamprogno

Population-based search for best weight of heuristic's component.
"""
from tablut.client.tablut_client import Client
from multiprocessing import Pool
from tablut.utils.state_utils import q
import subprocess
import numpy as np
import time
from itertools import permutations

N_PARAM = 6  # Number of parameter of each solution
MAX_TIME_ACTION = 5  # Maximum time allowed to search for action
NUM_MATCH = 4
PARAMS = [20, 30, 30, 20, 20, 100]


def create_play_player(port, color, max_time_act, weights, name, host, args=None):
    client_m = Client(port, color, max_time_act, weights=weights, name=name, host=host)
    client_m.run(args)


def eval_match(sol1, sol2):
    """
    Starts two matches between a couple of solutions,
    returns point earned by each one.
    """
    sol1_points = 0
    sol2_points = 0
    result = []
    pool = Pool()
    proc = subprocess.Popen(['ant', 'server'])  # Requires server files in path
    time.sleep(2)
    white_thread = pool.apply_async(create_play_player,
                                    args=(5800, "WHITE", MAX_TIME_ACTION, sol1, "sol1", "127.0.0.1", result))
    # Important, pass list just to one of the two threads, to avoid synchronization problems
    black_thread = pool.apply_async(create_play_player,
                                    args=(5801, "BLACK", MAX_TIME_ACTION, sol2, "sol2", "127.0.0.1", result))
    black_thread.wait()
    white_thread.wait()
    pool.close()
    while not q.empty():
        result.append(q.get())
    proc.wait()
    proc.terminate()

    if len(result) > 0 and result[0] == "WHITE":
        sol1_points += 3
    elif len(result) > 0 and result[0] == "BLACK":
        sol2_points += 3
    else:
        sol1_points += 1
        sol2_points += 1

    result = []
    pool = Pool()
    proc = subprocess.Popen(['ant', 'server'])  # Requires server files in path
    time.sleep(2)
    white_thread = pool.apply_async(create_play_player,
                                    args=(5800, "WHITE", MAX_TIME_ACTION, sol2, "sol2", "127.0.0.1", result))
    # Important, pass list just to one of the two threads, to avoid synchronization problems
    black_thread = pool.apply_async(create_play_player,
                                    args=(5801, "BLACK", MAX_TIME_ACTION, sol1, "sol1", "127.0.0.1", result))
    black_thread.wait()
    white_thread.wait()
    while not q.empty():
        result.append(q.get())
    pool.close()
    proc.wait()
    proc.terminate()

    if len(result) > 0 and result[0] == "WHITE":
        sol2_points += 3
    elif len(result) > 0 and result[0] == "BLACK":
        sol1_points += 3
    else:
        sol2_points += 1
        sol1_points += 1

    return sol1_points, sol2_points


def eval_pop():
    """
    Evaluates solutions, returns a list of floats, between 0 and 1
    (probabilities of survival and reproduction).
    """
    solutions = [[2, 1, 2, 1, 1, 100]]
    solutions_tmp = list(permutations(PARAMS))
    for sol in solutions_tmp:
        if sol not in solutions:
            solutions.append(sol)
    print(len(solutions))
    dones = [[] for x in range(len(solutions))]
    points = np.zeros(len(solutions))
    for i in range(len(solutions)):
        for y in range(len(solutions)):
            if len(dones[i]) >= NUM_MATCH:
                break
            if y != i and y not in dones[i]:
                dones[i].append(y)
                dones[y].append(i)
                tmp_p_1, tmp_p_2 = eval_match(solutions[i], solutions[y])
                points[i] += tmp_p_1
                points[y] += tmp_p_2
                print("MATCH BETWEEN ", i, " AND ", y, " RESULT: ", tmp_p_1, " - ", tmp_p_2)
    print("Results:")
    for i in range(len(solutions)):
        print("Solution ", solutions[i], " Points ", points[i])


eval_pop()
