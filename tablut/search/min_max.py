"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
import numpy as np
from tablut.search.game import Game
from tablut.search.heuristics import compute_heuristic
import time


def max_value(state, turn, alpha, beta, depth, max_depth, max_time, time_start):
    if cutoff_test(state, depth, max_depth, max_time, time_start):
        return compute_heuristic(state)
    v = -np.inf
    for a in Game.produce_actions(state, turn):
        v = max(v, min_value(Game.apply_action(state, a), turn, alpha, beta, depth + 1, max_depth, max_time, time_start))
        if v >= beta:
            return v
        alpha = max(alpha, v)
    return v


def min_value(state, turn, alpha, beta, depth, max_depth, max_time, time_start):
    if cutoff_test(state, depth, max_depth, max_time, time_start):
        return compute_heuristic(state)
    v = np.inf
    for a in Game.produce_actions(state,turn):
        v = min(v, max_value(Game.apply_action(state, a), turn, alpha, beta, depth + 1, max_depth, max_time, time_start))
        if v <= alpha:
            return v
        beta = min(beta, v)
    return v


def cutoff_test(state, depth, max_depth, max_time, time_start):
    """

    """
    return depth >= max_depth or time.time()-time_start >= max_time


def choose_action(state, turn, max_time):
    """

    """
    time_start = time.time()
    best_score = -np.inf
    best_score_end = -np.inf
    beta = np.inf
    best_action = None
    best_action_end = None
    max_depth = 0
    while time.time()-time_start < max_time:
        max_depth += 1
        for a in Game.produce_actions(state, turn):
            v = min_value(Game.apply_action(state, a), turn, best_score, beta, 1, max_depth, max_time, time_start)
            if v > best_score:
                best_score = v
                best_action = a
        best_score_end = best_score
        best_action_end = best_action
    return best_action_end, best_score_end
