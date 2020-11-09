import numpy as np
import copy
from tablut.utils.state_utils import *
from tablut.utils.bitboards import *


class State:

    def __init__(self, json_string):
        white_bitboard = np.zeros(9, dtype=int)
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
        i_row += 1
        pass

    def __init__(self, s, k, start_row, start_col, end_row, end_col):
        """"
        s= original state,
        k == True -> king moves, k==False ->pawn moves
        start_row,start_col -> pieces coordinates
        end_row, end_col -> final coordinates
        """
        self = copy.deepcopy(s)
        if s.turn == "WHITE":
            "in the original state, white moves, so in the new state black moves"
            self.turn = "BLACK"
            if k:
                self.white_bitboard[start_row] -= (1 << (8 - start_col))
                self.white_bitboard[end_row] += (1 << (8 - end_col))
            else:
                self.king_bitboard[start_row] -= (1 << (8 - start_col))
                self.king_bitboard[end_row] += (1 << (8 - end_col))

        else:
            self.turn = "WHITE"
            self.black_bitboard[start_row] -= (1 << (8 - start_col))
            self.black_bitboard[end_row] += (1 << (8 - end_col))

        pass

    def compute_heuristic(self,weights):
        h=0
        return h

