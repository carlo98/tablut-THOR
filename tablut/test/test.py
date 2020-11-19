from tablut.search.game import Game
from tablut.search.min_max import choose_action
from tablut.state.tablut_state import *
from tablut.test.future_min_max_test import lazy_smp

board_string = []
board_string.append(str.split("SPACE SPACE SPACE BLACK BLACK BLACK SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE BLACK SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE WHITE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("BLACK SPACE SPACE SPACE WHITE SPACE SPACE SPACE BLACK"))
board_string.append(str.split("BLACK BLACK WHITE WHITE KING WHITE WHITE BLACK BLACK"))
board_string.append(str.split("BLACK SPACE SPACE SPACE WHITE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE WHITE SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE SPACE BLACK SPACE SPACE SPACE SPACE"))
board_string.append(str.split("SPACE SPACE SPACE BLACK BLACK BLACK SPACE SPACE SPACE"))

json_string = {"turn": "BLACK", "board": board_string}
print(json_string.get("turn"))

game = Game(30, "WHITE", [70, 85, 99, 73, 66, 81, 1])
s = State(json_string)
ht = {}
state_hash_tables_tmp = []
for i in range(MAX_NUM_CHECKERS):
    state_hash_tables_tmp.append({})
state_hash_tables_tmp[i] = dict()
state_hash_tables_tmp[0][s.get_hash()] = {"value": 0, 'used': 1}

a,v = choose_action(s,game,state_hash_tables_tmp)
a, v = lazy_smp(s, game, ht)
print(a)
