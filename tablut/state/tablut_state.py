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
            self.black_bitboard = white_tries_capture_black_pawn(self.white_bitboard + self.king_bitboard,
                                                                 self.black_bitboard, end_row, end_col)
        else:
            self.turn = "WHITE"
            self.black_bitboard[start_row] -= (1 << (8 - start_col))
            self.black_bitboard[end_row] += (1 << (8 - end_col))
            self.white_bitboard = black_tries_capture_white_pawn(self.black_bitboard, self.white_bitboard, end_row,
                                                                 end_col)
            self.king_bitboard = black_tries_capture_king(self.black_bitboard, self.king_bitboard, end_row, end_col)
        pass

    def check_victory(self):
        if np.count_nonzero(self.king_biboard) == 0:
            "king captured"
            return -1
        if np.count_nonzero(self.king_bitboard & escapes_bitboard) != 0:
            "king escaped"
            return 1
        return 0

    def compute_heuristic(self, weights):
        "victory condition, of course"
        victory_cond = weights[0] * self.check_victory()

        "if the exits are blocked, white has a strong disadvantage"
        blocks_occupied_by_black = np.count_nonzero(self.black_bitboard & blocks_bitboard)
        blocks_occupied_by_white = np.count_nonzero(self.white_bitboard & blocks_bitboard) + \
                                   np.count_nonzero(self.king_bitboard & blocks_bitboard)
        blocks_cond = -weights[1] * blocks_occupied_by_black + weights[2] * blocks_occupied_by_white

        "remaining pieces are considered"
        white_cnt = 0
        black_cnt = 0
        for r in range(0, 9):
            for c in range(0, 9):
                curr_mask = 1 << (8 - c)
                if self.white_bitboard[r] & curr_mask != 0:
                    white_cnt += 1
                if self.black_bitboard[r] & curr_mask != 0:
                    black_cnt += 1

        remaining_whites_cond = weights[3] * (8 - white_cnt)
        remaining_blacks_cond = weights[4] * (16 - black_cnt)

        h = victory_cond + blocks_cond + remaining_whites_cond + remaining_blacks_cond
        return h

    def get_hash(self):
        """
        Returns an identifier for the state.
        Identifier is not unique, probing required in eventual has table.
        """
        state_id = 0
        for row in range(len(self.white_bitboard)):
            state_id += row * (self.white_bitboard[row] + self.black_bitboard[row] + self.king_bitboard[row])
        return state_id

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
