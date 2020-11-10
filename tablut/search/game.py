"""
Date: 08/11/2020
Author: Carlo Cena

Implementation of method required by tablut game.
"""
import numpy as np
from tablut.utils.bitboards import escapes_bitboard

class Game:
    def __init__(self, max_time, color, weights):
        self.max_time = max_time
        self.color = color
        self.weights = weights
        self.possible_actions_hor = np.empty(shape=(9, 9), dtype=int)
        self.possible_actions_ver = np.empty(shape=(9, 9), dtype=int)

        # Create possible actions from each position
        self.possible_actions_hor[0][0] = 0b011000000
        self.possible_actions_hor[0][1] = 0b101000000
        self.possible_actions_hor[0][2] = 0b110000000
        self.possible_actions_hor[0][3] = 0b111011111
        self.possible_actions_hor[0][4] = 0b111101111
        self.possible_actions_hor[0][5] = 0b111110111
        self.possible_actions_hor[0][6] = 0b000000011
        self.possible_actions_hor[0][7] = 0b000000101
        self.possible_actions_hor[0][8] = 0b000000110
        self.possible_actions_hor[1][0] = 0b011100000
        self.possible_actions_hor[1][1] = 0b101100000
        self.possible_actions_hor[1][2] = 0b110100000
        self.possible_actions_hor[1][3] = 0b111000000
        self.possible_actions_hor[1][4] = 0b111101111
        self.possible_actions_hor[1][5] = 0b000000111
        self.possible_actions_hor[1][6] = 0b000001011
        self.possible_actions_hor[1][7] = 0b000001101
        self.possible_actions_hor[1][8] = 0b000001110
        self.possible_actions_hor[2][0] = 0b011111111
        self.possible_actions_hor[2][1] = 0b101111111
        self.possible_actions_hor[2][2] = 0b110111111
        self.possible_actions_hor[2][3] = 0b111011111
        self.possible_actions_hor[2][4] = 0b111101111
        self.possible_actions_hor[2][5] = 0b111110111
        self.possible_actions_hor[2][6] = 0b111111011
        self.possible_actions_hor[2][7] = 0b111111101
        self.possible_actions_hor[2][8] = 0b111111110
        self.possible_actions_hor[3][0] = 0b011111110
        self.possible_actions_hor[3][1] = 0b001111110
        self.possible_actions_hor[3][2] = 0b010111110
        self.possible_actions_hor[3][3] = 0b011011110
        self.possible_actions_hor[3][4] = 0b011101110
        self.possible_actions_hor[3][5] = 0b011110110
        self.possible_actions_hor[3][6] = 0b011111010
        self.possible_actions_hor[3][7] = 0b011111100
        self.possible_actions_hor[3][8] = 0b011111110
        self.possible_actions_hor[4][0] = 0b011100000
        self.possible_actions_hor[4][1] = 0b101100000
        self.possible_actions_hor[4][2] = 0b000100000
        self.possible_actions_hor[4][3] = 0b001000000
        self.possible_actions_hor[4][4] = 0b001101100
        self.possible_actions_hor[4][5] = 0b000000100
        self.possible_actions_hor[4][6] = 0b000001000
        self.possible_actions_hor[4][7] = 0b000001101
        self.possible_actions_hor[4][8] = 0b000001110
        self.possible_actions_hor[5] = self.possible_actions_hor[3]
        self.possible_actions_hor[6] = self.possible_actions_hor[2]
        self.possible_actions_hor[7] = self.possible_actions_hor[1]
        self.possible_actions_hor[8] = self.possible_actions_hor[0]

        self.possible_actions_ver = np.transpose(self.possible_actions_hor)

    def produce_actions(self, state):
        """
        Returns a list of possible actions, each action is:
        [k, start_row, start_col, end_row, end_col]
        where
        k == True -> king moves, k==False ->pawn moves
        start_row,start_col -> pieces coordinates
        end_row, end_col -> final coordinates
        """
        action_list = []
        if state.turn == "WHITE":
            r = 0
            while state.king_bitboard[r] == 0:  # Searching king row
                r += 1
            c = 0
            curr_pos_mask = (1 << (8 - c))
            while state.king_bitboard[r] & curr_pos_mask != curr_pos_mask:  # Searching king column
                c += 1
                curr_pos_mask = (1 << (8 - c))
            # First look for actions that lead to escapes
            camp_0 = 0b100000000  # TODO: remove same actions below
            camp_1 = 0b000000001
            if camp_0 & escapes_bitboard[r] == camp_0:  # If escape present horizontally left
                if (camp_0 & ~state.white_bitboard[r]) & ~state.black_bitboard[r] == camp_0:  # If path free
                    action_list.append([True, r, c, r, 0])
            if camp_1 & escapes_bitboard[r] == camp_1:  # If escape present horizontally right
                if (camp_1 & ~state.white_bitboard[r]) & ~state.black_bitboard[r] == camp_1:  # If path free
                    action_list.append([True, r, c, r, 8])
            if camp_0 & escapes_bitboard[0] == camp_0:  # If escape present vertically up
                if (camp_0 & ~state.white_bitboard[0]) & ~state.black_bitboard[0] == camp_0:  # If path free
                    action_list.append([True, r, c, 0, c])
            if camp_1 & escapes_bitboard[8] == camp_1:  # If escape present vertically down
                if (camp_1 & ~state.white_bitboard[8]) & ~state.black_bitboard[8] == camp_1:  # If path free
                    action_list.append([True, r, c, 8, c])

            poss_actions_mask = ~state.white_bitboard[r] & self.possible_actions_hor[r][c]  # Horizontal actions
            poss_actions_mask &= ~state.black_bitboard[r]
            i = 1
            new_pos_mask = curr_pos_mask << i
            while i <= c and poss_actions_mask & new_pos_mask != 0:  # Actions to the left, checkers cannot jump
                action_list.append([True, r, c, r, c - i])
                i += 1
                if i <= c:
                    new_pos_mask = new_pos_mask << 1
            i = 1
            new_pos_mask = curr_pos_mask >> i
            while i <= (8 - c) and poss_actions_mask & new_pos_mask != 0:  # Actions to the right, checkers cannot jump
                action_list.append([True, r, c, r, c + i])
                i += 1
                if i <= (8 - c):
                    new_pos_mask = new_pos_mask >> 1
            i = 1
            while i <= r:  # Actions up
                poss_actions_mask = ~state.white_bitboard[r - i] & self.possible_actions_ver[r][c]  # Vertical actions
                poss_actions_mask &= ~state.black_bitboard[r - i]
                if poss_actions_mask & curr_pos_mask != 0:  # Checkers cannot jump
                    action_list.append([True, r, c, r - i, c])
                    i += 1
                else:
                    break
            i = 1
            while i <= (8 - r):  # Actions down
                poss_actions_mask = ~state.white_bitboard[r + i] & self.possible_actions_ver[r][c]  # Vertical actions
                poss_actions_mask &= ~state.black_bitboard[r + i]
                if poss_actions_mask & curr_pos_mask != 0:  # Checkers cannot jump
                    action_list.append([True, r, c, r + i, c])
                    i += 1
                else:
                    break

            for r in range(len(state.white_bitboard)):  # Searching white pawns
                if state.white_bitboard[r] != 0:
                    for c in range(len(state.white_bitboard)):
                        curr_pos_mask = (1 << (8 - c))
                        # If current position is occupied by a white pawn
                        if state.white_bitboard[r] & curr_pos_mask == curr_pos_mask:
                            poss_actions_mask = ~state.white_bitboard[r] & self.possible_actions_hor[r][c]  #Horizontal moves
                            poss_actions_mask &= ~state.king_bitboard[r]
                            poss_actions_mask &= ~state.black_bitboard[r]
                            i = 1
                            new_pos_mask = curr_pos_mask << i
                            while i <= c and poss_actions_mask & new_pos_mask != 0:  # Actions to the left, checkers cannot jump
                                action_list.append([False, r, c, r, c - i])
                                i += 1
                                if i <= c:
                                    new_pos_mask = new_pos_mask << 1
                            i = 1
                            new_pos_mask = curr_pos_mask >> i
                            while i <= (8 - c) and poss_actions_mask & new_pos_mask != 0:  # Actions to the right, checkers cannot jump
                                action_list.append([False, r, c, r, c + i])
                                i += 1
                                if i <= (8 - c):
                                    new_pos_mask = new_pos_mask >> 1
                            i = 1
                            while i <= r:  # Actions up
                                poss_actions_mask = ~state.white_bitboard[r - i] & self.possible_actions_ver[r][c]  # Vertical actions
                                poss_actions_mask &= ~state.king_bitboard[r - i]
                                poss_actions_mask &= ~state.black_bitboard[r - i]
                                if poss_actions_mask & curr_pos_mask != 0:  # Checkers cannot jump
                                    action_list.append([False, r, c, r - i, c])
                                    i += 1
                                else:
                                    break
                            i = 1
                            while i <= (8 - r):  # Actions down
                                poss_actions_mask = ~state.white_bitboard[r + i] & self.possible_actions_ver[r][c]
                                poss_actions_mask &= ~state.king_bitboard[r + i]
                                poss_actions_mask &= ~state.black_bitboard[r + i]
                                if poss_actions_mask & curr_pos_mask != 0:  # Checkers cannot jump
                                    action_list.append([False, r, c, r + i, c])
                                    i += 1
                                else:
                                    break

        if state.turn == "BLACK":
            for r in range(len(state.black_bitboard)):  # Searching black pawns
                if state.black_bitboard[r] != 0:
                    for c in range(len(state.black_bitboard)):
                        curr_pos_mask = (1 << (8 - c))
                        # If current position is occupied by a black pawn
                        if state.black_bitboard[r] & curr_pos_mask == curr_pos_mask:
                            poss_actions_mask = ~state.white_bitboard[r] & self.possible_actions_hor[r][c]  # Horizontal actions
                            poss_actions_mask &= ~state.king_bitboard[r]
                            poss_actions_mask &= ~state.black_bitboard[r]
                            i = 1
                            new_pos_mask = curr_pos_mask << i
                            while i <= c and poss_actions_mask & new_pos_mask != 0:  # Actions to the left, checkers cannot jump
                                action_list.append([False, r, c, r, c - i])
                                i += 1
                                if i <= c:
                                    new_pos_mask = new_pos_mask << 1
                            i = 1
                            new_pos_mask = curr_pos_mask >> i
                            while i <= (8 - c) and poss_actions_mask & new_pos_mask != 0:  # Actions to the right, checkers cannot jump
                                action_list.append([False, r, c, r, c + i])
                                i += 1
                                if i <= (8 - c):
                                    new_pos_mask = new_pos_mask >> 1
                            i = 1
                            while i <= r:  # Actions up
                                poss_actions_mask = ~state.white_bitboard[r - i] & self.possible_actions_ver[r][c]  # Vertical actions
                                poss_actions_mask &= ~state.king_bitboard[r - i]
                                poss_actions_mask &= ~state.black_bitboard[r - i]
                                if poss_actions_mask & curr_pos_mask != 0:  # Checkers cannot jump
                                    action_list.append([False, r, c, r - i, c])
                                    i += 1
                                else:
                                    break
                            i = 1
                            while i <= (8 - r):  # Actions down
                                poss_actions_mask = ~state.white_bitboard[r + i] & self.possible_actions_ver[r][c]
                                poss_actions_mask &= ~state.king_bitboard[r + i]
                                poss_actions_mask &= ~state.black_bitboard[r + i]
                                if poss_actions_mask & curr_pos_mask != 0:  # Checkers cannot jump
                                    action_list.append([False, r, c, r + i, c])
                                    i += 1
                                else:
                                    break
        return action_list
