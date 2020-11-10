"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
import numpy as np
import time
from tablut.state.tablut_state import State
from tablut.utils.state_utils import MAX_VAL_HEURISTIC


def max_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table):
    quad_prob = 1
    hash_result = state_hash_table.get(state.get_hash() + quad_prob ** quad_prob)
    while hash_result is not None:
        if state.equal(hash_result["bitboards"]):
            return hash_result["value"]
        quad_prob += 1
        hash_result = state_hash_table.get(state.get_hash() + quad_prob ** quad_prob)

    if cutoff_test(depth, max_depth, game.max_time, time_start):
        value = state.compute_heuristic(game.weights)
        add_to_hash(state_hash_table, state, value)
        return value
    if state.is_terminal():
        return MAX_VAL_HEURISTIC

    v = -np.inf
    for a in game.produce_actions(state):
        v = max(v, min_value(State(state, a[0], a[1], a[2], a[3], a[4]),
                             game, alpha, beta, depth + 1, max_depth, time_start))
        if v >= beta:
            return v
        alpha = max(alpha, v)
    return v


def min_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table):
    quad_prob = 1
    hash_result = state_hash_table.get(state.get_hash() + quad_prob ** quad_prob)
    while hash_result is not None:
        if state.equal(hash_result["bitboards"]):
            return hash_result["value"]
        quad_prob += 1
        hash_result = state_hash_table.get(state.get_hash() + quad_prob ** quad_prob)

    if cutoff_test(depth, max_depth, game.max_time, time_start):
        value = state.compute_heuristic(game.weights)
        add_to_hash(state_hash_table, state, value)
        return value
    if state.is_terminal():
        return MAX_VAL_HEURISTIC

    v = np.inf
    for a in game.produce_actions(state):
        v = min(v, max_value(State(state, a[0], a[1], a[2], a[3], a[4]),
                             game, alpha, beta, depth + 1, max_depth, time_start))
        if v <= alpha:
            return v
        beta = min(beta, v)
    return v


def add_to_hash(table, state, value):
    """
    Adds current state and its value to hash table.
    """
    quad_prob = 1
    pot = 1
    hash_result = table.get(state.get_hash() + quad_prob ** quad_prob)
    while hash_result is not None:
        quad_prob += 1
        pot = quad_prob ** quad_prob
        hash_result = table.get(state.get_hash() + pot)
    table[state.get_hash() + pot] = {"bitboards": {"black": state.black_bitboard, "white": state.white_bitboard,
                                                   "king": state.king_bitboard},
                                     "value": value}


def cutoff_test(depth, max_depth, max_time, time_start):
    """
    Returns True if reached maximum depth or finished time
    False if search is to be continued
    """
    if depth >= max_depth or time.time()-time_start >= max_time:
        return 0
    return -1


def choose_action(state, game):
    """
    Search for the best action using min max with alpha beta pruning
    iteratively increasing the maximum depth.
    It stops only when available time is almost up.
    """
    time_start = time.time()
    best_score = np.inf
    best_score_end = np.inf
    alpha = -np.inf
    best_action = None
    best_action_end = None
    max_depth = 0
    state_hash_table = dict()
    while time.time()-time_start < game.max_time:
        max_depth += 1  # Iteratively increasing depth
        all_actions = game.produce_actions(state)  # Getting all possible actions given state
        cont = 0
        for a in all_actions:
            v = min_value(State(state, a[0], a[1], a[2], a[3], a[4]),  #TODO: add is_terminal method in tablut_state
                          game, alpha, best_score, 1, max_depth, time_start, state_hash_table)
            cont += 1
            if v > best_score:
                best_score = v
                best_action = a
        if cont == len(all_actions):  # If search at current maximum depth is finished, update best action
            best_score_end = best_score
            best_action_end = best_action
    return best_action_end, best_score_end
