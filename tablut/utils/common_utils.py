from tablut.utils.bitboards import MAX_NUM_CHECKERS


def clear_hash_table(state_hash_tables, state):
    index_hash = MAX_NUM_CHECKERS - cont_pieces(state) - 1
    while index_hash >= 0 and state_hash_tables.get(index_hash) is not None:
        state_hash_tables.pop(index_hash)
        index_hash -= 1


def cont_pieces(state):
    cnt = 0
    for r in range(0, 9):
        for c in range(0, 9):
            curr_mask = 256 >> c
            if state.white_bitboard[r] & curr_mask != 0:
                cnt += 1
            elif state.black_bitboard[r] & curr_mask != 0:
                cnt += 1
            elif state.king_bitboard[r] & curr_mask != 0:
                cnt += 1
    return cnt


def update_used(state_hash_table, state, weights, color):
    """
    Adds current state and its value to hash table.
    """
    state_hash = state.get_hash()
    index_hash = MAX_NUM_CHECKERS - cont_pieces(state)
    hash_result = state_hash_table[index_hash].get(state_hash)
    if hash_result is not None:
        state_hash_table[index_hash][state_hash]['used'] = 1
    else:
        state_hash_table[index_hash][state_hash] = {"value": state.compute_heuristic(weights, color), "used": 1}
