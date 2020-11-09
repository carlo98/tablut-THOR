"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
import numpy as np
import time


def max_value(state, game, alpha, beta, depth, max_depth, time_start):
    if cutoff_test(state, depth, max_depth, game.max_time, time_start):
        return state.compute_heuristic(game.weights)
    v = -np.inf
    for a in game.produce_actions(state):
        v = max(v, min_value(game.apply_action(state, a), game, alpha, beta, depth + 1, max_depth, time_start))
        if v >= beta:
            return v
        alpha = max(alpha, v)
    return v


def min_value(state, game, alpha, beta, depth, max_depth, time_start):
    if cutoff_test(state, depth, max_depth, game.max_time, time_start):
        return state.compute_heuristic(game.weights)
    v = np.inf
    for a in game.produce_actions(state):
        v = min(v, max_value(game.apply_action(state, a), game, alpha, beta, depth + 1, max_depth, time_start))
        if v <= alpha:
            return v
        beta = min(beta, v)
    return v


def cutoff_test(state, depth, max_depth, max_time, time_start):
    """

    """
    return depth >= max_depth or time.time()-time_start >= max_time


def choose_action(state, game):
    """

    """
    time_start = time.time()
    best_score = -np.inf
    best_score_end = -np.inf
    beta = np.inf
    best_action = None
    best_action_end = None
    max_depth = 0
    while time.time()-time_start < game.max_time:
        max_depth += 1
        all_actions = game.produce_actions(state)
        cont = 0
        for a in all_actions:
            v = min_value(game.apply_action(state, a), game, best_score, beta, 1, max_depth, time_start)
            cont += 1
            if v > best_score:
                best_score = v
                best_action = a
        if cont == len(all_actions):
            best_score_end = best_score
            best_action_end = best_action
    return best_action_end, best_score_end
