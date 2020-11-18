from tablut.search.game import Game
from tablut.state.tablut_state import *
from tablut.test.future_min_max_test import lazy_smp

board_string=[]
board_string.append(str.split("SPACE SPACE SPACE BLACK BLACK BLACK SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE BLACK SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE WHITE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("BLACK SPACE SPACE SPACE WHITE SPACE SPACE SPACE BLACK"))
board_string.append(str.split("BLACK BLACK WHITE WHITE KING WHITE WHITE BLACK BLACK"))
board_string.append(str.split("BLACK SPACE SPACE SPACE WHITE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE WHITE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE BLACK SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE BLACK BLACK BLACK SPACE SPACE SPACE"))


json_string = {"turn":"BLACK", "board":board_string}
print(json_string.get("turn"))

game=Game(10,"WHITE",[1,1,1,1,1,1,1])
s = State(json_string)

a,v = lazy_smp(s,game)
print(a)