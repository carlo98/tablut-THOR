"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
import numpy as np
import time
from tablut.state.tablut_state import State
from tablut.utils.state_utils import MAX_VAL_HEURISTIC, DRAW_POINTS
from tablut.utils.common_utils import cont_pieces, MAX_NUM_CHECKERS

#TODO: remove num_state_visited, use just in test phase


def max_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table, num_state_visited):
    num_state_visited[0] += 1
    state_hash = state.get_hash()
    index_checkers = MAX_NUM_CHECKERS-cont_pieces(state)
    hash_result = state_hash_table[index_checkers].get(state_hash)
    all_actions = None
    if hash_result is not None:
        if hash_result['used'] == 1:
            return DRAW_POINTS
        if hash_result.get('all_actions') is not None:
            all_actions = hash_result.get('all_actions').get(state.turn)
    if cutoff_test(depth, max_depth, game.max_time, time_start):  # If reached maximum depth or total time
        if hash_result is not None and hash_result.get("value") is not None:
            return hash_result["value"]  # If state previously evaluated don't recompute heuristic
        value = state.compute_heuristic(game.weights, game.color)  # If state not previously evaluated
        add_to_hash(state_hash_table, state_hash, value, None, index_checkers, state.turn)  # Add state and value to hash table
        return value
    tmp_victory = state.check_victory()
    if tmp_victory == -1 and game.color == "BLACK":  # king captured and black player -> Win
        return -MAX_VAL_HEURISTIC
    elif tmp_victory == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        return -MAX_VAL_HEURISTIC

    # Body
    v = -np.inf
    if all_actions is None:
        all_actions = game.produce_actions(state)
        if hash_result is not None:
            add_to_hash(state_hash_table, state_hash, None, all_actions, index_checkers, state.turn, True)
        else:
            add_to_hash(state_hash_table, state_hash, None, all_actions, index_checkers, state.turn, True)
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
    state_hash = state.get_hash()
    index_checkers = MAX_NUM_CHECKERS-cont_pieces(state)
    hash_result = state_hash_table[index_checkers].get(state_hash)
    all_actions = None
    if hash_result is not None:
        if hash_result['used'] == 1:
            return DRAW_POINTS
        if hash_result.get('all_actions') is not None:
            all_actions = hash_result.get('all_actions').get(state.turn)
    if cutoff_test(depth, max_depth, game.max_time, time_start):  # If reached maximum depth or total time
        if hash_result is not None and hash_result.get("value") is not None:
            return hash_result["value"]  # If state previously evaluated don't recompute heuristic
        value = state.compute_heuristic(game.weights, game.color)  # If state not previously evaluated
        add_to_hash(state_hash_table, state_hash, value, None, index_checkers, state.turn)  # Add state and value to hash table
        return value
    tmp_victory = state.check_victory()
    if tmp_victory == -1 and game.color == "BLACK":  # king captured and black player -> Win
        return -MAX_VAL_HEURISTIC
    elif tmp_victory == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        return -MAX_VAL_HEURISTIC

    # Body
    v = np.inf
    if all_actions is None:
        all_actions = game.produce_actions(state)
        if hash_result is not None:
            add_to_hash(state_hash_table, state_hash, None, all_actions, index_checkers, state.turn, True)
        else:
            add_to_hash(state_hash_table, state_hash, None, all_actions, index_checkers, state.turn, True)
    if len(all_actions) == 0:
        return MAX_VAL_HEURISTIC
    for a in all_actions:
        v = min(v, max_value(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                             game, alpha, beta, depth + 1, max_depth, time_start, state_hash_table, num_state_visited))
        if v <= alpha:
            return v
        beta = min(beta, v)
    return v


def add_to_hash(table, state_hash, value, all_actions, index_checkers, turn, change_actions=False):
    """
    Adds current state and its value to hash table.
    """
    if table[index_checkers].get(state_hash) is not None and not change_actions:
        table[index_checkers][state_hash]['value'] = value
    elif change_actions and table[index_checkers].get(state_hash) is not None and table[index_checkers][state_hash].get(
            'all_actions') is not None:
        table[index_checkers][state_hash]['all_actions'][turn] = all_actions
    elif change_actions and table[index_checkers].get(state_hash) is not None and table[index_checkers][state_hash].get(
            'all_actions') is None:
        table[index_checkers][state_hash]['all_actions'] = {turn: all_actions}
    elif change_actions and table[index_checkers].get(state_hash) is None:
        table[index_checkers][state_hash] = {"used": 0, 'all_actions': {turn: all_actions}}
    else:
        table[index_checkers][state_hash] = {"value": value, "used": 0, 'all_actions': {turn: all_actions}}


def cutoff_test(depth, max_depth, max_time, time_start):
    """
    Returns True if reached maximum depth or finished time
    False if search is to be continued
    """
    if depth >= max_depth or time.time()-time_start >= max_time:
        return True
    return False


def choose_action(state, game, state_hash_table):
    """
    Search for the best action using min max with alpha beta pruning
    iteratively increasing the maximum depth.
    It stops only when available time is almost up.
    """
    time_start = time.time()
    best_score_end = np.inf
    alpha = -np.inf
    best_action = None
    best_action_end = None
    max_depth = 2
    num_state_visited = [0]
    flag = False
    all_actions = game.produce_actions(state)  # Getting all possible actions given state
    while time.time()-time_start < game.max_time:
        cont = 0
        best_score = np.inf
        for a in all_actions:
            v = max_value(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                          game, alpha, best_score, 1, max_depth, time_start, state_hash_table, num_state_visited)
            cont += 1
            if v < best_score:
                best_score = v
                best_action = a
            if time.time()-time_start >= game.max_time:
                break
        # If search at current maximum depth is finished, update best action
        if cont == len(all_actions):
            best_score_end = best_score
            best_action_end = best_action
            flag = True
            print("Depth reached:", max_depth)
        elif flag:
            print("Depth reached:", max_depth-1)
        else:
            print("Minimum depth not reached")
        max_depth += 1  # Iteratively increasing depth

    print(num_state_visited, " state visited state in ", time.time()-time_start, " seconds.")
    return best_action_end, best_score_end
