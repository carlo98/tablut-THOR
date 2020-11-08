"""
Date: 08/11/2020
Author: Carlo Cena

Implementation of method required by tablut game.
"""
import time
from threading import Thread
import numpy as np


class Game:
    def __init__(self, max_time, weights):
        self.max_time = max_time
        self.weights = weights
        self.castle_bitboard = np.zeros(shape=9, dtype=int)
        self.escapes_bitboard = np.zeros(shape=9, dtype=int)
        self.camps_bitboard = np.zeros(shape=9, dtype=int)
        self.possible_actions_hor = np.empty(shape=(9, 9), dtype=int)
        self.possible_actions_ver = np.empty(shape=(9, 9), dtype=int)

        self.castle_bitboard[4] = 0b000010000

        self.escapes_bitboard[0] = 0b011000110
        self.escapes_bitboard[1] = 0b100000001
        self.escapes_bitboard[2] = 0b100000001
        self.escapes_bitboard[6] = 0b100000001
        self.escapes_bitboard[7] = 0b100000001
        self.escapes_bitboard[8] = 0b011000110

        self.camps_bitboard[0] = 0b000111000
        self.camps_bitboard[1] = 0b000010000
        self.camps_bitboard[3] = 0b100000001
        self.camps_bitboard[4] = 0b110000011
        self.camps_bitboard[5] = 0b100000001
        self.camps_bitboard[7] = 0b000010000
        self.camps_bitboard[8] = 0b000111000

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

    def produce_actions(self, state, turn, time_start):
        """

        """
        # Iterate over action, if time.time() - time_start >= self.max_time stop
        # Use Threads
        pass

