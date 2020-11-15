import sys


MAX_SIZE_DICT = 1.8 * 1024 * 1024 * 1024  # GB, MB, kB, B
# Size of dict is 296B at 14/11/2020


def clear_hash_table_1(state_has_table):
    if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
        for key in state_has_table.keys():
            if state_has_table[key]['used'] == 0:
                state_has_table.pop(key)
            if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
                break


def clear_hash_table_2(state_has_table, state):
    for key in state_has_table.keys():
        if cont_pieces(state_has_table[key]) > cont_pieces(state):
            state_has_table.pop(key)
        if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
            break
    if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
        for key in state_has_table.keys():
            if state_has_table[key]['used'] == 0:
                state_has_table.pop(key)
            if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
                break


def cont_pieces(state):
    cnt = 1
    for r in range(0, 9):
        for c in range(0, 9):
            curr_mask = 256 >> c
            if state.white_bitboard[r] & curr_mask != 0:
                cnt += 1
            if state.black_bitboard[r] & curr_mask != 0:
                cnt += 1
    return cnt
