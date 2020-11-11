from tablut.utils.bitboards import *
import numpy as np

MAX_VAL_HEURISTIC = 200  # TODO: to be set at maximum value achievable by heuristic


def build_column(bitboard, mask):
    """
    Builds column at given position.
    """
    num = 0
    for i in range(len(bitboard)):
        if bitboard[i] & mask != 0:
            num ^= 1 << (8 - i)
    return num


def bit(n):
    mask = 0b000000001
    i = 0
    while i <= 8:
        b = n & mask
        yield b
        mask = mask << i
        i += 1


def black_tries_capture_white_pawn(black_bitboard, white_bitboard, row, col):
    binary_column = 1 << (8 - col)
    if row >= 2:
        "upwards capture"
        if binary_column in bit(white_bitboard[row - 1]):
            "a white pawn is above"
            if binary_column in bit(black_bitboard[row - 2] or binary_column in bit(camps_bitboard[row - 2]) \
                                    or binary_column in bit(castle_bitboard[row - 2])):
                "capture is possible, proceed"
                white_bitboard[row - 1] ^= binary_column

    if col <= 6:
        "right capture"
        if (binary_column >> 1) in bit(white_bitboard[row]):
            "a white pawn is right"
            if (binary_column >> 2) in bit(black_bitboard[row]) or (binary_column >> 2) in bit(camps_bitboard[row]) \
                    or (binary_column >> 2) in bit(castle_bitboard[row]):
                white_bitboard[row] ^= binary_column >> 1

    if row <= 6:
        "downwards capture"
        binary_column = 1 << (8 - col)
        if binary_column in bit(white_bitboard[row + 1]):
            "a white pawn is below"
            if binary_column in bit(black_bitboard[row + 2] or binary_column in bit(camps_bitboard[row + 2]) \
                                    or binary_column in bit(castle_bitboard[row + 2])):
                "capture is possible, proceed"
                white_bitboard[row + 1] ^= binary_column
    if col >= 2:
        "left capture"
        if (binary_column << 1) in bit(white_bitboard[row]):
            "a white pawn is right"
            if (binary_column << 2) in bit(black_bitboard[row]) or (binary_column << 2) in bit(camps_bitboard[row]) \
                    or (binary_column << 2) in bit(castle_bitboard[row]):
                white_bitboard[row] ^= binary_column << 1

    return white_bitboard


def black_tries_capture_king(black_bitboard, king_bitboard, row, col):
    king_row = np.nonzero(king_bitboard)[0]
    king_col = int(8 - np.log2(king_bitboard[king_row]))

    if king_row in (0, 8) or king_col in (0, 8) \
            or (row, col) not in (
            (king_row, king_col + 1), (king_row, king_col - 1), (king_row + 1, king_col), (king_row - 1, king_col)):
        "the move does not attack the king, or the king cannot be attacked (last rows/cols)"
        return king_bitboard

    king_bin_col = (1 << (8 - king_col))
    if king_row == 4 and king_col == 4:
        if 16 in bit(black_bitboard[king_row - 1]) and 16 in bit(black_bitboard[king_row + 1]) \
                and 32 in bit(black_bitboard[king_row]) and 8 in bit(black_bitboard[king_row]):
            king_bitboard[king_row] = 0
    elif king_row == 3 and king_col == 4:
        if 16 in bit(black_bitboard[king_row - 1]) \
                and 32 in bit(black_bitboard[king_row]) and 8 in bit(black_bitboard[king_row]):
            king_bitboard[king_row] = 0
    elif king_row == 4 and king_col == 5:
        if 8 in bit(black_bitboard[king_row - 1]) and 8 in bit(black_bitboard[king_row + 1]) \
                and 4 in bit(black_bitboard[king_row]):
            king_bitboard[king_row] = 0
    elif king_row == 5 and king_col == 4:
        if 16 in bit(black_bitboard[king_row + 1]) \
                and 32 in bit(black_bitboard[king_row]) and 8 in bit(black_bitboard[king_row]):
            king_bitboard[king_row] = 0
    elif king_row == 4 and king_col == 3:
        if 32 in bit(black_bitboard[king_row - 1]) and 32 in bit(black_bitboard[king_row + 1]) \
                and 64 in bit(black_bitboard[king_row]):
            king_bitboard[king_row] = 0
    elif king_row == row:
        other_col = 2 * king_col - col
        other_col_bin = 1 << (8 - other_col)
        if other_col_bin in bit(black_bitboard[king_row]):
            king_bitboard[king_row] = 0
    else:
        other_row = 2 * king_row - row
        if king_bin_col in bit(black_bitboard[other_row]):
            king_bitboard[king_row] = 0
    return king_bitboard


def white_tries_capture_black_pawn(white_bitboard, black_bitboard, row, col):
    binary_column = 1 << (8 - col)
    if row >= 2:
        "upwards capture"
        if binary_column in bit(black_bitboard[row - 1]) and (row, col) != (2, 4):
            "a black pawn is above"
            if binary_column in bit(white_bitboard[row - 2] or binary_column in bit(camps_bitboard[row - 2]) \
                                    or binary_column in bit(castle_bitboard[row - 2])):
                "capture is possible, proceed"
                black_bitboard[row - 1] ^= binary_column

    if col <= 6:
        "right capture"
        if (binary_column >> 1) in bit(black_bitboard[row]) and (row, col) != (4, 6):
            "a black pawn is right"
            if (binary_column >> 2) in bit(white_bitboard[row]) or (binary_column >> 2) in bit(camps_bitboard[row]) \
                    or (binary_column >> 2) in bit(castle_bitboard[row]):
                black_bitboard[row] ^= binary_column >> 1

    if row <= 6:
        "downwards capture"
        binary_column = 1 << (8 - col)
        if binary_column in bit(black_bitboard[row + 1]) and (row, col) != (6, 4):
            "a black pawn is below"
            if binary_column in bit(white_bitboard[row + 2] or binary_column in bit(camps_bitboard[row + 2]) \
                                    or binary_column in bit(castle_bitboard[row + 2])):
                "capture is possible, proceed"
                black_bitboard[row + 1] ^= binary_column
    if col >= 2:
        "left capture"
        if (binary_column << 1) in bit(black_bitboard[row]) and (row, col) != (4, 2):
            "a black pawn is left"
            if (binary_column << 2) in bit(white_bitboard[row]) or (binary_column << 2) in bit(camps_bitboard[row]) \
                    or (binary_column << 2) in bit(castle_bitboard[row]):
                black_bitboard[row] ^= binary_column << 1

    return black_bitboard


def action_to_server_format(action):
    """
    Return format required by server.
    """
    start_row = action[1] + 1
    end_row = action[3] + 1
    start_col = chr(65+action[2])
    end_col = chr(65+action[4])
    return {
        'from': str(start_col) + str(start_row),
        'to': str(end_col) + str(end_row)
    }
