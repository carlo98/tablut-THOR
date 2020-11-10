from tablut.state.tablut_state import *
board_string=[]
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE BLACK SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE BLACK KING SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE BLACK SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))


json_string = {"turn":"BLACK", "board":board_string}
print(json_string.get("turn"))

s = State(json_string)

print(s.king_bitboard)
print(s.black_bitboard)
s1 = State(second_init_args=[s,False,6,4,5,4])
print("s1")

print(s1.king_bitboard)
print(s1.black_bitboard)