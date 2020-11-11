"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
import numpy as np
import time
from tablut.state.tablut_state import State
from tablut.utils.state_utils import MAX_VAL_HEURISTIC

#TODO: remove num_state_visited, use just in test phase


def max_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table, num_state_visited):
    num_state_visited[0] += 1
    if cutoff_test(depth, max_depth, game.max_time, time_start):  # If reached maximum depth or total time
        quad_prob = 1
        pot = 1
        state_hash = state.get_hash()
        m_key = (state_hash[0] + pot, state_hash[1] + pot, state_hash[2] + pot)
        hash_result = state_hash_table.get(m_key)

        while hash_result is not None:
            if state.equal(hash_result["bitboards"]):
                return hash_result["value"]  # If state previously evaluated don't recompute heuristic
            quad_prob += 1
            pot = quad_prob ** quad_prob
            m_key = (state_hash[0] + pot, state_hash[1] + pot, state_hash[2] + pot)
            hash_result = state_hash_table.get(m_key)

        value = state.compute_heuristic(game.weights, game.color)  # If state not previously evaluated
        add_to_hash(state_hash_table, state, state_hash, value)  # Add state and value to hash table
        return value

    if state.check_victory() == -1 and game.color == "BLACK":  # king captured and black player -> Win
        return -MAX_VAL_HEURISTIC
    elif state.check_victory() == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        return MAX_VAL_HEURISTIC
    elif state.check_victory() == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        return MAX_VAL_HEURISTIC
    elif state.check_victory() == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        return -MAX_VAL_HEURISTIC

    # Body
    v = -np.inf
    all_actions = game.produce_actions(state)
    if len(all_actions) == 0:
        return -MAX_VAL_HEURISTIC
    for a in all_actions:
        v = max(v, min_value(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                             game, alpha, beta, depth + 1, max_depth, time_start, state_hash_table, num_state_visited))
        if v >= beta:
            return v
        alpha = max(alpha, v)
    return v


def min_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table, num_state_visited):
    num_state_visited[0] += 1
    if cutoff_test(depth, max_depth, game.max_time, time_start):  # If reached maximum depth or total time
        quad_prob = 1
        pot = 1
        state_hash = state.get_hash()
        m_key = (state_hash[0] + pot, state_hash[1] + pot, state_hash[2] + pot)
        hash_result = state_hash_table.get(m_key)

        while hash_result is not None:
            if state.equal(hash_result["bitboards"]):
                return hash_result["value"]  # If state previously evaluated don't recompute heuristic
            quad_prob += 1
            pot = quad_prob**quad_prob
            m_key = (state_hash[0] + pot, state_hash[1] + pot, state_hash[2] + pot)
            hash_result = state_hash_table.get(m_key)

        value = state.compute_heuristic(game.weights, game.color)  # If state not previously evaluated
        add_to_hash(state_hash_table, state, state_hash, value)  # Add state and value to hash table
        return value

    if state.check_victory() == -1 and game.color == "BLACK":  # king captured and black player -> Win
        return -MAX_VAL_HEURISTIC
    elif state.check_victory() == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        return MAX_VAL_HEURISTIC
    elif state.check_victory() == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        return MAX_VAL_HEURISTIC
    elif state.check_victory() == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        return -MAX_VAL_HEURISTIC

    # Body
    v = np.inf
    all_actions = game.produce_actions(state)
    if len(all_actions) == 0:
        return MAX_VAL_HEURISTIC
    for a in all_actions:
        v = min(v, max_value(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                             game, alpha, beta, depth + 1, max_depth, time_start, state_hash_table, num_state_visited))
        if v <= alpha:
            return v
        beta = min(beta, v)
    return v


def add_to_hash(table, state, state_hash, value):
    """
    Adds current state and its value to hash table.
    """
    quad_prob = 1
    pot = 1
    m_key = (state_hash[0]+pot, state_hash[1]+pot, state_hash[2]+pot)
    while table.get(m_key) is not None:
        quad_prob += 1
        pot = quad_prob ** quad_prob
        m_key = (state_hash[0]+pot, state_hash[1]+pot, state_hash[2]+pot)
    table[m_key] = {"bitboards": {"black": state.black_bitboard, "white": state.white_bitboard,
                                  "king": state.king_bitboard},
                    "value": value}


def cutoff_test(depth, max_depth, max_time, time_start):
    """
    Returns True if reached maximum depth or finished time
    False if search is to be continued
    """
    if depth >= max_depth or time.time()-time_start >= max_time:
        return True
    return False


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
    max_depth = 5
    num_state_visited = [0]
    state_hash_table = dict()

    while time.time()-time_start < game.max_time:
        all_actions = game.produce_actions(state)  # Getting all possible actions given state
        cont = 0
        for a in all_actions.keys():
            v = min_value(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                          game, alpha, best_score, 1, max_depth, time_start, state_hash_table, num_state_visited)
            cont += 1
            if v < best_score:
                best_score = v
                best_action = a
        if cont == len(all_actions):  # If search at current maximum depth is finished, update best action
            best_score_end = best_score
            best_action_end = best_action
            print("Depth reached:", max_depth)
        else:
            print("Depth reached:", max_depth-1)
        max_depth += 1  # Iteratively increasing depth

    print(num_state_visited, " state visited state in ", time.time()-time_start, " seconds.")
    return best_action_end, best_score_end
