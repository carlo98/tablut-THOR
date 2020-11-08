import numpy as np


class State:

    def __init__(self):
        pass

    def __init__(self, json_string):
        white_bitboard= np.zeros(9, dtype=int)
        black_bitboard = np.zeros(9, dtype=int)
        king_bitboard = np.zeros(9, dtype=int)

        turn = json_string["turn"]
        i_row = 0
        for row in json_string["board"]:
            for col in row:
                if col == "WHITE":
                    white_bitboard[i_row] ^= 1
                elif col == "BLACK":
                    black_bitboard[i_row] ^= 1
                elif col == "KING":
                    king_bitboard[i_row] ^= 1
            white_bitboard[i_row] <<= 1
            black_bitboard[i_row] <<= 1
            king_bitboard[i_row] <<= 1
        i_row +=1
        pass

    def __init__(self, turn, start_pos, end_pos):
        pass

    def compute_heuristic(game):
        total_value = 0
        return total_value
