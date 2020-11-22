from concurrent.futures import *

from tablut.utils.hashtable import *
import time
import numpy as np

from tablut.state.tablut_state import State
from multiprocessing import cpu_count


def lazy_smp_process(state, game, htable):
    time_start = time.time()
    print(time_start)
    cpu_num = cpu_count()
    print(cpu_num)
    pool = ProcessPoolExecutor(cpu_num)
    chosen_action = None
    chosen_value = None
    """futures = [pool.submit(alpha_beta_cutoff_search, state, game, 1, htable),
               pool.submit(alpha_beta_cutoff_search, state, game, 2, htable),
               pool.submit(alpha_beta_cutoff_search, state, game, 3, htable),
               pool.submit(alpha_beta_cutoff_search, state, game, 4, htable)]
    """
    futures = []
    for p in range(cpu_num):
        d = int(p / 2 + 1)
        futures.append(pool.submit(alpha_beta_cutoff_search, state, game, d, htable, time_start))
    "we suppose not to go above 7 levels"
    working_at_depth = [2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0]
    max_depth = 0
    try:
        while True:

            "wait returns completed tasks and working task"
            remaining_time = game.max_time - (time.time() - time_start)
            if remaining_time <= 0.1:
                raise TimeoutError
            (tasks_completed, working) = wait(futures, remaining_time,
                                              return_when=FIRST_COMPLETED)
            "check the completed results, give the new task"
            for x in tasks_completed:
                i = futures.index(x)
                (action, value, depth) = x.result()
                if depth > max_depth:
                    "a new depth has been completed"
                    max_depth = depth
                    print(max_depth)
                    chosen_action = action
                    chosen_value = value

                working_at_depth[depth] = 2
                dp = depth + 1

                while working_at_depth[dp] > 1:
                    dp += 1
                working_at_depth[dp] += 1
                futures[i] = pool.submit(alpha_beta_cutoff_search, state, game, dp, htable)
                print('I am %d, now going to %d' % (i, dp))

    finally:
        return chosen_action, chosen_value


def eval_fn(state, game):
    v = state.compute_heuristic(game.weights, game.color)
    return v


def min_value(state, game, alpha, beta, depth, max_depth, htable, time_start):
    if game.max_time - (time.time() - time_start) <= 0.1:
        return False
    hash_key = state.get_hash()
    update_hash = True
    hash_available = False
    if hash_key in htable:
        hash_available = True
        hash_state = htable.get(hash_key)
        if hash_state.get_forward_depth() >= max_depth - depth:

            return hash_state.get_value()
        else:
            update_hash = False

    if depth == max_depth:
        return eval_fn(state, game)

    v = np.inf
    if hash_available:
        all_actions = hash_state.get_actions()
    else:
        all_actions = game.produce_actions(state)
    np.random.shuffle(all_actions)
    for a in all_actions:
        if game.max_time - (time.time() - time_start) <= 0.1:
            return False
        new_state = State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4]))
        v = min(v, max_value(new_state, game, alpha, beta, depth + 1, max_depth, htable))
        if update_hash or not hash_available:
            key = new_state.get_hash()
            htable[key] = HashEntry(key, v, all_actions, max_depth, depth)
        if v <= alpha:
            return v
        beta = min(beta, v)
    return v


def max_value(state, game, alpha, beta, depth, max_depth, htable, time_start):
    if game.max_time - (time.time() - time_start) <= 0.1:
        return False
    hash_key = state.get_hash()
    update_hash = True
    hash_available = False
    if hash_key in htable:
        hash_available = True
        hash_state = htable.get(hash_key)
        if hash_state.get_forward_depth() >= max_depth - depth:

            return hash_state.get_value()
        else:
            update_hash = False

    if depth == max_depth:
        return eval_fn(state, game)
    v = -np.inf
    if hash_available:
        all_actions = hash_state.get_actions()
    else:
        all_actions = game.produce_actions(state)
    np.random.shuffle(all_actions)
    for a in all_actions:
        if game.max_time - (time.time() - time_start) <= 0.1:
            return False
        new_state = State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4]))
        v = max(v, min_value(new_state, game, alpha, beta, depth + 1, max_depth, htable))
        if update_hash or not hash_available:
            key = new_state.get_hash()
            htable[key] = HashEntry(key, v, all_actions, max_depth, depth)
        if v >= beta:
            return v
        alpha = max(alpha, v)
    return v


def alpha_beta_cutoff_search(state, game, max_depth, htable, time_start):
    """Search game to determine best action; use alpha-beta pruning.
    This version cuts off search and uses an evaluation function."""

    best_score = -np.inf
    beta = np.inf
    best_action = None
    action_list = game.produce_actions(state)
    np.random.shuffle(action_list)
    for a in action_list:
        new_state = State(second_init_args=(state, a[0], a[1], a[2], a[3], a[4]))
        v = min_value(new_state, game, best_score, beta, 1, max_depth, htable, time_start)
        if v > best_score:
            best_score = v
            best_action = a
    return best_action, best_score, max_depth
