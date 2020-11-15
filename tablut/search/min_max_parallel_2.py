"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
import numpy as np
import time
from tablut.state.tablut_state import State
from tablut.utils.state_utils import MAX_VAL_HEURISTIC
from threading import Thread, Lock


lock_2 = Lock()
#TODO: remove num_state_visited, use just in test phase


def max_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table, id_m, v, lock_p):
    lock_2.acquire()
    state_hash = state.get_hash()
    hash_result = state_hash_table.get(state_hash)
    lock_2.release()
    all_actions = None
    if hash_result is not None:
        if hash_result['used'] == 1:
            v[id_m] = 0
            return
        if hash_result.get('all_actions') is not None:
            all_actions = hash_result.get('all_actions')
    if cutoff_test(depth, max_depth, game.max_time, time_start):  # If reached maximum depth or total time
        if hash_result is not None:
            v[id_m] = hash_result["value"]  # If state previously evaluated don't recompute heuristic
            return
        value = state.compute_heuristic(game.weights, game.color)  # If state not previously evaluated
        lock_2.acquire()
        add_to_hash(state_hash_table, state_hash, value, all_actions)  # Add state and value to hash table
        lock_2.release()
        v[id_m] = value
        return
    tmp_victory = state.check_victory()
    if tmp_victory == -1 and game.color == "BLACK":  # king captured and black player -> Win
        v[id_m] = -MAX_VAL_HEURISTIC
        return
    elif tmp_victory == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        v[id_m] = MAX_VAL_HEURISTIC
        return
    elif tmp_victory == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        v[id_m] = MAX_VAL_HEURISTIC
        return
    elif tmp_victory == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        v[id_m] = -MAX_VAL_HEURISTIC
        return

    # Body
    if all_actions is None:
        all_actions = game.produce_actions(state)
        if hash_result is not None:
            add_to_hash(state_hash_table, state_hash, hash_result['value'], all_actions)
    if len(all_actions) == 0:
        return -MAX_VAL_HEURISTIC

    return_values = [np.inf for x in range(len(all_actions))]
    lock_p.acquire()
    best_score = [alpha[0]]
    lock_p.release()
    thread_list = []
    lock_m = Lock()
    a = all_actions[0]
    thread = Thread(target=min_value,
                    args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                          game, best_score, beta, depth + 1, max_depth,
                          time_start, state_hash_table, 0, return_values, lock_m))
    thread.start()
    thread.join()
    if return_values[0] > best_score[0]:
        best_score[0] = return_values[0]

    for i in range(len(all_actions[1:int(len(all_actions)/2)])):
        a = all_actions[i]
        lock_p.acquire()
        if alpha[0] > best_score[0]:
            best_score[0] = alpha[0]
        lock_p.release()
        thread_list.append(Thread(target=min_value,
                                  args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                                        game, best_score, beta, depth + 1, max_depth,
                                        time_start, state_hash_table, i, return_values, lock_m)))

        thread_list[i].start()

    flag_t = True
    while flag_t:
        flag_t = False
        for i in range(len(thread_list)):
            if not thread_list[i].is_alive():
                lock_m.acquire()
                if return_values[i] >= beta[0]:
                    v[id_m] = return_values[i]
                    lock_m.release()
                    return
                best_score[0] = max(best_score[0], return_values[i])
                lock_m.release()
            else:
                flag_t = True
    for i in range(len(thread_list)):
        thread_list[i].join()
    thread_list = []
    for i in range(len(all_actions[int(len(all_actions)/2)+1:])):
        a = all_actions[i]
        lock_p.acquire()
        if alpha[0] > best_score[0]:
            best_score[0] = alpha[0]
        lock_p.release()
        thread_list.append(Thread(target=min_value,
                                  args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                                        game, best_score, beta, depth + 1, max_depth,
                                        time_start, state_hash_table, i, return_values, lock_m)))

        thread_list[i].start()

    flag_t = True
    while flag_t:
        flag_t = False
        for i in range(len(thread_list)):
            if not thread_list[i].is_alive():
                lock_m.acquire()
                if return_values[i] >= beta[0]:
                    v[id_m] = return_values[i]
                    lock_m.release()
                    return
                best_score[0] = max(best_score[0], return_values[i])
                lock_m.release()
            else:
                flag_t = True
    for i in range(len(thread_list)):
        thread_list[i].join()

    v[id_m] = return_values[-1]
    return


def min_value(state, game, alpha, beta, depth, max_depth, time_start, state_hash_table, id_m, v, lock_p):
    lock_2.acquire()
    state_hash = state.get_hash()
    hash_result = state_hash_table.get(state_hash)
    lock_2.release()
    all_actions = None
    if hash_result is not None:
        if hash_result['used'] == 1:
            v[id_m] = 0
            return
        if hash_result.get('all_actions') is not None:
            all_actions = hash_result.get('all_actions')
    if cutoff_test(depth, max_depth, game.max_time, time_start):  # If reached maximum depth or total time
        if hash_result is not None:
            v[id_m] = hash_result["value"]  # If state previously evaluated don't recompute heuristic
            return
        value = state.compute_heuristic(game.weights, game.color)  # If state not previously evaluated
        lock_2.acquire()
        add_to_hash(state_hash_table, state_hash, value, all_actions)  # Add state and value to hash table
        lock_2.release()
        v[id_m] = value
        return
    tmp_victory = state.check_victory()
    if tmp_victory == -1 and game.color == "BLACK":  # king captured and black player -> Win
        v[id_m] = -MAX_VAL_HEURISTIC
        return
    elif tmp_victory == -1 and game.color == "WHITE":  # King captured and white player -> Lose
        v[id_m] = MAX_VAL_HEURISTIC
        return
    elif tmp_victory == 1 and game.color == "BLACK":  # King escaped and black player -> Lose
        v[id_m] = MAX_VAL_HEURISTIC
        return
    elif tmp_victory == 1 and game.color == "WHITE":  # King escaped and white player -> Win
        v[id_m] = -MAX_VAL_HEURISTIC
        return

    # Body
    if all_actions is None:
        all_actions = game.produce_actions(state)
        if hash_result is not None:
            add_to_hash(state_hash_table, state_hash, hash_result['value'], all_actions)
    if len(all_actions) == 0:
        v[id_m] = MAX_VAL_HEURISTIC
        return

    return_values = [-np.inf for x in range(len(all_actions))]
    lock_p.acquire()
    best_score = [beta[0]]
    lock_p.release()
    thread_list = []
    lock_m = Lock()
    a = all_actions[0]
    thread = Thread(target=max_value,
                    args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                          game, alpha, best_score, depth+1, max_depth,
                          time_start, state_hash_table, 0, return_values, lock_m))
    thread.start()
    thread.join()
    if return_values[0] < best_score[0]:
        best_score[0] = return_values[0]

    for i in range(len(all_actions[1:int(len(all_actions)/2)])):
        a = all_actions[i]
        lock_p.acquire()
        if beta[0] < best_score[0]:
            best_score[0] = beta[0]
        lock_p.release()
        thread_list.append(Thread(target=max_value,
                                  args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                                        game, alpha, best_score, depth+1, max_depth,
                                        time_start, state_hash_table, i, return_values, lock_m)))

        thread_list[i].start()

    flag_t = True
    while flag_t:
        flag_t = False
        for i in range(len(thread_list)):
            if not thread_list[i].is_alive():
                lock_m.acquire()
                if return_values[i] <= alpha[0]:
                    v[id_m] = return_values[i]
                    lock_m.release()
                    return
                best_score[0] = min(best_score[0], return_values[i])
                lock_m.release()
            else:
                flag_t = True
    for i in range(len(thread_list)):
        thread_list[i].join()
    thread_list = []
    for i in range(len(all_actions[int(len(all_actions)/2)+1:])):
        a = all_actions[i]
        lock_p.acquire()
        if beta[0] < best_score[0]:
            best_score[0] = beta[0]
        lock_p.release()
        thread_list.append(Thread(target=max_value,
                                  args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                                        game, alpha, best_score, depth+1, max_depth,
                                        time_start, state_hash_table, i, return_values, lock_m)))

        thread_list[i].start()

    flag_t = True
    while flag_t:
        flag_t = False
        for i in range(len(thread_list)):
            if not thread_list[i].is_alive():
                lock_m.acquire()
                if return_values[i] <= alpha[0]:
                    v[id_m] = return_values[i]
                    lock_m.release()
                    return
                best_score[0] = min(best_score[0], return_values[i])
                lock_m.release()
            else:
                flag_t = True
    for i in range(len(thread_list)):
        thread_list[i].join()

    v[id_m] = return_values[-1]
    return


def add_to_hash(table, state_hash, value, all_actions):
    """
    Adds current state and its value to hash table.
    """
    table[state_hash] = {"value": value, "used": 0, 'all_actions': all_actions}


def update_used(state_hash_table, state, weights, color):
    """
    Adds current state and its value to hash table.
    """
    state_hash = state.get_hash()
    hash_result = state_hash_table.get(state_hash)
    if hash_result is not None:
        state_hash_table[state_hash]['used'] = 1
    else:
        state_hash_table[state_hash] = {"value": state.compute_heuristic(weights, color), "used": 1}


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
    all_actions = game.produce_actions(state)  # Getting all possible actions given state
    best_score = [np.inf]
    best_score_end = np.inf
    alpha = [-np.inf]
    best_action = None
    best_action_end = None
    max_depth = 2
    flag = False
    lock_m = Lock()
    return_values = [-np.inf for x in range(len(all_actions))]
    while time.time()-time_start < game.max_time:
        thread_list = []
        if len(all_actions) > 0:
            a = all_actions[0]
            thread_list.append(Thread(target=max_value,
                                      args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                                            game, alpha, best_score, 1, max_depth,
                                            time_start, state_hash_table, 0, return_values, lock_m)))
            thread_list[0].start()
            thread_list[0].join()

            if return_values[0] < best_score[0]:
                best_score[0] = return_values[0]
                best_action = a
            for i in range(len(all_actions[1:])):
                a = all_actions[i+1]
                thread_list.append(Thread(target=max_value,
                                          args=(State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4])),
                                                game, alpha, best_score, 1, max_depth,
                                                time_start, state_hash_table, i+1, return_values, lock_m)))

                thread_list[i+1].start()

            flag_t = True
            flag_time = False
            while flag_t and not flag_time:
                flag_t = False
                for i in range(len(all_actions)):
                    if not thread_list[i].is_alive():
                        lock_m.acquire()
                        if return_values[i] < best_score[0]:
                            best_score[0] = return_values[i]
                            best_action = all_actions[i]
                        lock_m.release()
                    else:
                        flag_t = True
                if time.time() - time_start >= game.max_time:
                    flag_time = True
            for i in range(len(thread_list)):
                thread_list[i].join()

        if not flag_time:
            best_score_end = best_score[0]
            best_action_end = best_action
            flag = True
            print("Depth reached:", max_depth)
        elif flag:
            print("Depth reached:", max_depth - 1)
        else:
            print("Minimum depth not reached")
        max_depth += 1  # Iteratively increasing depth
    return best_action_end, best_score_end

