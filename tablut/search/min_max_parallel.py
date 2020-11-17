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
from threading import Lock, Thread
import copy

N_THREAD = 16
lock_hash = Lock()
lock_value = Lock()
lock_bool = Lock()
lock_time = Lock()

#TODO: remove num_state_visited, use just in test phase


def max_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table):
    tmp_victory = state.check_victory()
    if tmp_victory == -1 and game.color == "BLACK":  # king captured and black player -> Win
        return -MAX_VAL_HEURISTIC
    elif tmp_victory == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        return -MAX_VAL_HEURISTIC
    state_hash = state.get_hash()
    index_checkers = MAX_NUM_CHECKERS-cont_pieces(state)
    lock_hash.acquire()
    hash_result = copy.deepcopy(state_hash_table[index_checkers].get(state_hash))
    lock_hash.release()
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
                             game, alpha, beta, depth + 1, max_depth, time_start, state_hash_table))
        if v >= beta:
            return v
        alpha = max(alpha, v)
    return v


def min_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table):
    tmp_victory = state.check_victory()
    if tmp_victory == -1 and game.color == "BLACK":  # king captured and black player -> Win
        return -MAX_VAL_HEURISTIC
    elif tmp_victory == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        return MAX_VAL_HEURISTIC
    elif tmp_victory == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        return -MAX_VAL_HEURISTIC
    state_hash = state.get_hash()
    index_checkers = MAX_NUM_CHECKERS - cont_pieces(state)
    lock_hash.acquire()
    hash_result = copy.deepcopy(state_hash_table[index_checkers].get(state_hash))
    lock_hash.release()
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
                             game, alpha, beta, depth + 1, max_depth, time_start, state_hash_table))
        if v <= alpha:
            return v
        beta = min(beta, v)
    return v


def add_to_hash(table, state_hash, value, all_actions, index_checkers, turn, change_actions=False):
    """
    Adds current state and its value to hash table.
    """
    lock_hash.acquire()
    if table[index_checkers].get(state_hash) is not None and not change_actions:
        table[index_checkers][state_hash]['value'] = value
    elif change_actions and table[index_checkers].get(state_hash) is not None and \
            table[index_checkers][state_hash].get('all_actions') is not None:
        table[index_checkers][state_hash]['all_actions'][turn] = all_actions
    elif change_actions and table[index_checkers].get(state_hash) is not None and \
            table[index_checkers][state_hash].get('all_actions') is None:
        table[index_checkers][state_hash]['all_actions'] = {turn: all_actions}
    elif change_actions and table[index_checkers].get(state_hash) is None:
        table[index_checkers][state_hash] = {"used": 0, 'all_actions': {turn: all_actions}, 'value': None}
    else:
        table[index_checkers][state_hash] = {"value": value, "used": 0, 'all_actions': {turn: all_actions}}
    lock_hash.release()


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
    max_depth = 2
    flag = False
    best_score_end = np.inf
    best_action_end = None
    best_action = None
    alpha = -np.inf
    flag_time = [False]
    best_scores = []
    all_actions = game.produce_actions(state)  # Getting all possible actions given state
    if len(all_actions) > 0:
        thread_list = []
        active = []
        action = []
        while time.time()-time_start < game.max_time:
            best_score = [np.inf]
            for i in range(len(best_scores)):
                best_scores[i] = np.inf
            for j in range(len(all_actions)):
                a = all_actions[j]
                if j == 0:
                    v = max_value(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                                  game, alpha, best_score[0], 1, max_depth, time_start, state_hash_table)
                    if v < best_score[0]:
                        best_score[0] = v
                        best_action = a
                else:
                    if len(thread_list) < N_THREAD:
                        active.append(False)
                        action.append(a)
                        best_scores.append(np.inf)
                        thread_list.append(Thread(target=search_thread,
                                                  args=(state, action, game, alpha, best_score, 1,
                                                        max_depth, time_start, state_hash_table, flag_time, active,
                                                        len(thread_list), best_scores)))
                        thread_list[len(thread_list) - 1].start()
                    else:
                        flag_assign = True
                        while flag_assign:
                            for i in range(len(thread_list)):
                                lock_bool.acquire()
                                tmp = active[i]
                                lock_bool.release()
                                if not tmp:
                                    lock_value.acquire()
                                    if best_scores[i] < best_score[0]:
                                        best_score[0] = best_scores[i]
                                        best_action = action[i]
                                    lock_value.release()
                                    action[i] = a
                                    lock_bool.acquire()
                                    active[i] = True
                                    lock_bool.release()
                                    flag_assign = False
                                    break

                if time.time() - time_start >= game.max_time:
                    lock_time.acquire()
                    flag_time[0] = True
                    lock_time.release()
                    break
            flag_t = True
            while flag_t and not flag_time[0]:
                flag_t = False
                for i in range(N_THREAD):
                    lock_bool.acquire()
                    tmp = active[i]
                    lock_bool.release()
                    if tmp:
                        flag_t = True
                if time.time() - time_start >= game.max_time:
                    lock_time.acquire()
                    flag_time[0] = True
                    lock_time.release()
            # If search at current maximum depth is finished, update best action
            tmp_time = flag_time[0]
            if not tmp_time:
                for i in range(len(thread_list)):
                    if best_scores[i] < best_score[0]:
                        best_score[0] = best_scores[i]
                        best_action = action[i]
                best_score_end = best_score[0]
                best_action_end = best_action
                flag = True
                print("Depth reached:", max_depth)
            elif flag:
                print("Depth reached:", max_depth - 1)
            else:
                print("Minimum depth not reached")
            max_depth += 1  # Iteratively increasing depth
        for i in range(len(thread_list)):
            thread_list[i].join()
    return best_action_end, best_score_end


def search_thread(state, action, game, alpha, best_score, depth, max_depth, time_start, state_hash_table,
                  stop, active, id_m, best_scores):
    lock_time.acquire()
    tmp_time = stop[0]
    lock_time.release()
    while not tmp_time:
        lock_bool.acquire()
        active[id_m] = False
        tmp = active[id_m]
        lock_bool.release()
        while not tmp and not tmp_time:
            lock_bool.acquire()
            tmp = active[id_m]
            lock_bool.release()
            lock_time.acquire()
            tmp_time = stop[0]
            lock_time.release()
        if tmp_time:
            break
        lock_value.acquire()
        tmp_best = best_score[0]
        a = action[id_m]
        lock_value.release()
        v = max_value(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                      game, alpha, tmp_best, depth+1, max_depth, time_start, state_hash_table)
        if v < best_scores[id_m]:
            best_scores[id_m] = v
        lock_time.acquire()
        tmp_time = stop[0]
        lock_time.release()
    return
