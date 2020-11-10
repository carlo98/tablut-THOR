from tablut.state.tablut_state import *
board_string=[]
board_string.append(str.split("WHITE WHITE WHITE WHITE WHITE BLACK BLACK BLACK BLACK"))
board_string.append(str.split("WHITE SPACE WHITE SPACE WHITE SPACE WHITE SPACE WHITE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))


json_string = {"turn":"WHITE", "board":board_string}
print(json_string.get("turn"))

s = State(json_string)

print(s.white_bitboard)