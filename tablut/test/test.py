from tablut.state.tablut_state import *
board_string=[]
board_string.append(str.split("SPACE BLACK SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE KING SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE SPACE"))


json_string = {"turn":"BLACK", "board":board_string}
print(json_string.get("turn"))

s = State(json_string)

s.compute_heuristic([1,1,1,1,1,1,1], "WHITE")

bb=np.array([256,256,0,0,0,0,0,0,0])
nb = step_rotate(bb)
print(nb)
nb = step_rotate(nb)
print(nb)
nb = step_rotate(nb)
print(nb)
nb = step_rotate(nb)
print(nb)
