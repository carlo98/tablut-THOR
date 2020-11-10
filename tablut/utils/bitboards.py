"""
Constant bitboards: castle, escapes and camps.
"""
import numpy as np

castle_bitboard = np.array([
    0b000000000,
    0b000000000,
    0b000000000,
    0b000000000,
    0b000010000,
    0b000000000,
    0b000000000,
    0b000000000,
    0b000000000], dtype=np.int)

escapes_bitboard = np.array([
    0b011000110,
    0b100000001,
    0b100000001,
    0b000000000,
    0b000000000,
    0b000000000,
    0b100000001,
    0b100000001,
    0b011000110], dtype=np.int)

camps_bitboard = np.array([
    0b000111000,
    0b000010000,
    0B000000000,
    0b100000001,
    0b110000011,
    0b100000001,
    0b000000000,
    0b000010000,
    0b000111000], dtype=np.int)

blocks_bitboard = np.array([
    0b000000000,
    0b001000100,
    0b010000010,
    0b000000000,
    0b000000000,
    0b000000000,
    0b010000010,
    0b001000100,
    0b000000000], dtype=np.int)
