import numpy as np
import copy
from tablut.utils.state_utils import *
from tablut.utils.bitboards import *


class State:

    def __init__(self, json_string):
        self.white_bitboard = np.zeros(9, dtype=int)
        self.black_bitboard = np.zeros(9, dtype=int)
        self.king_bitboard = np.zeros(9, dtype=int)

        self.turn = json_string["turn"]
        i_row = 0
        for row in json_string["board"]:
            for col in row:
                if col == "WHITE":
                    self.white_bitboard[i_row] ^= 1
                elif col == "BLACK":
                    self.black_bitboard[i_row] ^= 1
                elif col == "KING":
                    self.king_bitboard[i_row] ^= 1
            self.white_bitboard[i_row] <<= 1
            self.black_bitboard[i_row] <<= 1
            self.king_bitboard[i_row] <<= 1
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

    def get_hash(self):
        """
        Returns an identifier for the state.
        Identifier is not unique, probing required in eventual hash table.
        """
        king_sum = 0
        white_sum = 0
        black_sum = 0
        for row in range(len(self.black_bitboard)):
            king_sum += self.king_bitboard[row]*(row+1)
            white_sum += self.white_bitboard[row]*(row+1)
            black_sum += self.black_bitboard[row]*(row+1)
        return king_sum, white_sum, black_sum

    def equal(self, m_bitboards):
        """
        Returns true if all bitboards are equal to corresponding ones.
        False otherwise.
        """
        for row in range(len(self.white_bitboard)):  # Check white bitboard
            if self.white_bitboard[row] ^ m_bitboards["white"] != 0:
                return False
        for row in range(len(self.black_bitboard)):  # Check black bitboard
            if self.black_bitboard[row] ^ m_bitboards["black"] != 0:
                return False
        for row in range(len(self.king_bitboard)):  # Check king bitboard
            if self.king_bitboard[row] ^ m_bitboards["king"] != 0:
                return False
        return True

