import numpy as np
import copy
from tablut.utils.state_utils import *
from tablut.utils.bitboards import *


class State:

    def __init__(self, json_string=None, second_init_args=None):
        """"
        s= original state,
        k == True -> king moves, k==False ->pawn moves
        start_row,start_col -> pieces coordinates
        end_row, end_col -> final coordinates
        """
        if json_string is not None:
            self.white_bitboard = np.zeros(9, dtype=int)
            self.black_bitboard = np.zeros(9, dtype=int)
            self.king_bitboard = np.zeros(9, dtype=int)

            self.turn = json_string["turn"]
            i_row = 0
            for row in json_string.get("board"):
                for col in row:
                    self.white_bitboard[i_row] <<= 1
                    self.black_bitboard[i_row] <<= 1
                    self.king_bitboard[i_row] <<= 1
                    if col == "WHITE":
                        self.white_bitboard[i_row] ^= 1
                    elif col == "BLACK":
                        self.black_bitboard[i_row] ^= 1
                    elif col == "KING":
                        self.king_bitboard[i_row] ^= 1
                i_row += 1

            pass
        elif second_init_args is not None:
            s = second_init_args[0]
            k = second_init_args[1]
            start_row = second_init_args[2]
            start_col = second_init_args[3]
            end_row = second_init_args[4]
            end_col = second_init_args[5]
            self.white_bitboard = copy.deepcopy(s.white_bitboard)
            self.black_bitboard = copy.deepcopy(s.black_bitboard)
            self.king_bitboard = copy.deepcopy(s.king_bitboard)

            if s.turn == "WHITE":
                "in the original state, white moves, so in the new state black moves"
                self.turn = "BLACK"
                if k is False:
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
        if np.count_nonzero(self.king_bitboard) == 0:
            "king captured"
            return -1
        if np.count_nonzero(self.king_bitboard & escapes_bitboard) != 0:
            "king escaped"
            return 1
        return 0

    def compute_heuristic(self, weights, color):
        "victory condition, of course"
        victory_cond = self.check_victory()
        if victory_cond == -1 and color == "BLACK":  # king captured and black player -> Win
            return -MAX_VAL_HEURISTIC
        elif victory_cond == -1 and color == "WHITE":  # King captured and white player -> Lose
            return MAX_VAL_HEURISTIC
        elif victory_cond == 1 and color == "BLACK":  # King escaped and black player -> Lose
            return MAX_VAL_HEURISTIC
        elif victory_cond == 1 and color == "WHITE":  # King escaped and white player -> Win
            return -MAX_VAL_HEURISTIC

        "if the exits are blocked, white has a strong disadvantage"
        blocks_occupied_by_black = np.count_nonzero(self.black_bitboard & blocks_bitboard)
        blocks_occupied_by_white = np.count_nonzero(self.white_bitboard & blocks_bitboard) + \
                                   np.count_nonzero(self.king_bitboard & blocks_bitboard)
        coeff_min_black = (-1) ** (color == "BLACK")
        coeff_min_white = (-1) ** (color == "WHITE")
        blocks_cond = coeff_min_black * weights[0] * blocks_occupied_by_black \
                      + coeff_min_white * weights[1] * blocks_occupied_by_white
        open_blocks_cond = coeff_min_white * weights[2]* (8 - blocks_occupied_by_white - blocks_occupied_by_black)
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

        remaining_whites_cond = coeff_min_white * weights[3] * white_cnt
        remaining_blacks_cond = coeff_min_black * weights[4] * black_cnt

        "aggressive king condition"
        ak_cond = coeff_min_white*weights[5]*self.open_king_paths()
        h = blocks_cond + remaining_whites_cond + remaining_blacks_cond + open_blocks_cond + ak_cond
        return h

    def compute_heuristic_test(self, weights, color):
        "victory condition, of course"
        victory_cond = self.check_victory()
        if victory_cond == -1 and color == "BLACK":  # king captured and black player -> Win
            return -MAX_VAL_HEURISTIC, 0, 0, 0, 0
        elif victory_cond == -1 and color == "WHITE":  # King captured and white player -> Lose
            return MAX_VAL_HEURISTIC, 0, 0, 0, 0
        elif victory_cond == 1 and color == "BLACK":  # King escaped and black player -> Lose
            return MAX_VAL_HEURISTIC, 0, 0, 0, 0
        elif victory_cond == 1 and color == "WHITE":  # King escaped and white player -> Win
            return -MAX_VAL_HEURISTIC, 0, 0, 0, 0

        "if the exits are blocked, white has a strong disadvantage"
        blocks_occupied_by_black = np.count_nonzero(self.black_bitboard & blocks_bitboard)
        blocks_occupied_by_white = np.count_nonzero(self.white_bitboard & blocks_bitboard) + \
                                   np.count_nonzero(self.king_bitboard & blocks_bitboard)
        coeff_min_black = (-1) ** (color == "BLACK")
        coeff_min_white = (-1) ** (color == "WHITE")
        blocks_cond = coeff_min_black * weights[0] * blocks_occupied_by_black \
                      + coeff_min_white * weights[1] * blocks_occupied_by_white
        open_blocks_cond = coeff_min_white * weights[2]* (8 - blocks_occupied_by_white - blocks_occupied_by_black)
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

        remaining_whites_cond = coeff_min_white * weights[3] * white_cnt
        remaining_blacks_cond = coeff_min_black * weights[4] * black_cnt

        "aggressive king condition"
        ak_cond = coeff_min_white*weights[5]*self.open_king_paths()
        h = blocks_cond + remaining_whites_cond + remaining_blacks_cond + open_blocks_cond + ak_cond
        return blocks_cond, remaining_blacks_cond, remaining_whites_cond, open_blocks_cond, ak_cond

    def open_king_paths(self):
        "king coordinates"
        king_row = np.nonzero(self.king_bitboard)[0]
        king_bin_col = self.king_bitboard[king_row]
        king_col = int(8 - np.log2(king_bin_col))

        "check for pawns/camps left and right"
        right_mask = 511 * np.ones(1, dtype=int)

        left_mask = (king_bin_col << 1) * np.ones(1, dtype=int)
        for col in range(0, king_col+1):
            right_mask >>= 1
            if col <= king_col - 2:
                left_mask ^= king_bin_col
                left_mask <<= 1

        "check for pawns/camps up and down"
        above_the_column = []
        below_the_column = []
        for row in range(0, 9):
            if row != king_row and row < king_row:
                above_the_column += list(bit(camps_bitboard[row])) + list(bit(self.white_bitboard[row])) + list(bit(self.black_bitboard[row]))
            elif row != king_row and row > king_row:
                below_the_column += list(bit(camps_bitboard[row])) + list(bit(self.white_bitboard[row])) + list(
                    bit(self.black_bitboard[row]))
        open_paths = 4

        if (king_row in [3, 4, 5] or
                ((right_mask & self.white_bitboard[king_row]) + (right_mask & self.black_bitboard[king_row])
                + (right_mask & camps_bitboard[king_row]) != 0)):
            open_paths -= 1
        if (king_row in [3, 4, 5] or
                (left_mask & self.white_bitboard[king_row]) + (left_mask & self.black_bitboard[king_row])
                + (left_mask & camps_bitboard[king_row]) != 0):
            open_paths -= 1

        if king_col in [3, 4, 5] or king_bin_col in above_the_column:
            open_paths -= 1
        if king_col in [3, 4, 5] or king_bin_col in below_the_column:
            open_paths -= 1

        return open_paths

    def get_hash(self):
        """
        Returns an identifier for the state.
        Identifier is unique.
        """
        return tuple(self.king_bitboard), tuple(self.white_bitboard), tuple(self.black_bitboard)

    def equal(self, m_bitboards):
        """
        Returns true if all bitboards are equal to corresponding ones.
        False otherwise.
        """
        for row in range(len(self.white_bitboard)):  # Check white bitboard
            if self.white_bitboard[row] ^ m_bitboards["white"][row] != 0:
                return False
        for row in range(len(self.black_bitboard)):  # Check black bitboard
            if self.black_bitboard[row] ^ m_bitboards["black"][row] != 0:
                return False
        for row in range(len(self.king_bitboard)):  # Check king bitboard
            if self.king_bitboard[row] ^ m_bitboards["king"][row] != 0:
                return False
        return True
