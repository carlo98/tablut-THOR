from concurrent.futures import ThreadPoolExecutor, wait, FIRST_COMPLETED
import time

import numpy as np


# todo rewrite the task assignment better considering the list
def lazy_smp(state, game):
    time_start = time.time()
    pool = ThreadPoolExecutor(4)
    chosen_action = None
    chosen_value = None
    futures = [pool.submit(alpha_beta_cutoff_search, state, game, 1),
               pool.submit(alpha_beta_cutoff_search, state, game, 1),
               pool.submit(alpha_beta_cutoff_search, state, game, 2),
               pool.submit(alpha_beta_cutoff_search, state, game, 3)]
    "we suppose not to go above 7 levels"
    working_at_depth = [2, 1, 1, 0, 0, 0, 0, 0]
    max_depth = 0
    while time.time() - time_start < game.max_time:
        "wait returns completed tasks and working task"
        (tasks_completed, working) = wait(futures, game.max_time - (time.time() - time_start),
                                          return_when=FIRST_COMPLETED)
        "check the completed results, give the new task"
        for x in tasks_completed:
            i = futures.index(x)
            (value, action, depth) = x.result()
            if depth > max_depth:
                "a new depth has been completed"
                max_depth = depth
                chosen_action = action
                chosen_value = value

            "if task has completed, no point for new tasks to search that level"
            working_at_depth[depth - 1] = 2

            if working_at_depth[depth + 1] == 2:
                "if the next level has 2 threads working on it, search at max_depth+1"

                futures[i] = pool.submit(alpha_beta_cutoff_search, state, game, max_depth+1)
            else:
                "else help searching the next level"
                working_at_depth[depth + 1] += 1
                futures[i] = pool.submit(alpha_beta_cutoff_search, state, game, depth + 1)

    return chosen_action, chosen_value


#todo actually implment the tree search and the hash

def alpha_beta_cutoff_search(state, game, d=4, cutoff_test=None, eval_fn=None):
    """Search game to determine best action; use alpha-beta pruning.
    This version cuts off search and uses an evaluation function."""

    player = game.to_move(state)

    # Functions used by alpha_beta
    def max_value(state, alpha, beta, depth):
        if cutoff_test(state, depth):
            return eval_fn(state)
        v = -np.inf
        for a in game.actions(state):
            v = max(v, min_value(game.result(state, a), alpha, beta, depth + 1))
            if v >= beta:
                return v
            alpha = max(alpha, v)
        return v

    def min_value(state, alpha, beta, depth):
        if cutoff_test(state, depth):
            return eval_fn(state)
        v = np.inf
        for a in np.shuffle(game.produce_actions(state)):
            v = min(v, max_value(game.result(state, a), alpha, beta, depth + 1))
            if v <= alpha:
                return v
            beta = min(beta, v)
        return v

    # Body of alpha_beta_cutoff_search starts here:
    # The default test cuts off at depth d or at a terminal state
    cutoff_test = (cutoff_test or (lambda state, depth: depth > d or game.terminal_test(state)))
    eval_fn = eval_fn or (lambda state: game.utility(state, player))
    best_score = -np.inf
    beta = np.inf
    best_action = None
    for a in np.shuffle(game.produce_actions(state)):
        v = min_value(game.result(state, a), best_score, beta, 1)
        if v > best_score:
            best_score = v
            best_action = a
    return best_action
