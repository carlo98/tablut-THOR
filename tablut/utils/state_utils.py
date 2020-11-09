from tablut.utils import bitboards

"cancellare le bitboards"

escape_bitboard = []
camps_bitboard = []
castle_bitboard = []


def black_check_captures(black_bitboard, white_bitboard, king_bitboard, row, col):
    if row >= 2: "upwards capture"
    # todo
    if col <= 6: "right capture"
    # todo
    if row <= 6: "downwards capture"
    # todo
    if col >= 2: "lef capture"
    # todo
    return 0
