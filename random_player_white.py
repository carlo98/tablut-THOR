"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
from tablut.state.tablut_state import State
from tablut.client.connection_handler import ConnectionHandler
from tablut.search.game import Game
from tablut.search.min_max import choose_action, update_used
from tablut.utils.state_utils import action_to_server_format
import pickle
import os
import sys

MAX_SIZE_DICT = 1.8 * 1024 * 1024 * 1024  # GB, MB, kB, B
# Size of dict is 296B at 14/11/2020


class Client(ConnectionHandler):
    """Extends ConnectionHandler, handling the connection between client and server."""

    def __init__(self, port, color, max_time, host="localhost", weights=None, name='WHITE_RANDOM', file_access=False):
        super().__init__(port, host)
        self.color = color
        self.max_time = max_time
        self.player_name = name
        self.weights = weights
        self.file_access = file_access
        self.game = Game(self.max_time, self.color, self.weights)

    def run(self):
        """Client's body."""

        if self.file_access:
            exists = os.path.isfile('state_hash')
            if exists:
                file = open("state_hash", "rb")
                state_hash_table = pickle.load(file)
                file.close()
                state_hash_table = convert_state_hash(state_hash_table)
            else:
                state_hash_table = dict()
            state_list = []
        id_win = None
        try:
            self.connect()
            self.send_string(self.player_name)
            state = State(self.read_string())
            state_hash_table_tmp = {state.get_hash(): {"value": 0, 'used': 1}}
            while True:  # Playing
                if self.color == state.turn:  # check turn
                    action, value = choose_action(state, self.game,
                                                  state_hash_table_tmp)  # Retrieving best action and its value and pass weights
                    self.send_string(action_to_server_format(action))
                    print("Choosen action value:", value)
                else:
                    print(sys.getsizeof(state_hash_table_tmp) - MAX_SIZE_DICT)
                    clear_hash_table_1(state_hash_table_tmp)
                state_server = self.read_string()
                state = State(state_server)
                blocks_cond, remaining_blacks_cond, remaining_whites_cond, open_blocks_cond, ak_cond = \
                    state.compute_heuristic_test(self.game.weights, self.game.color)
                print(self.game.color, blocks_cond, remaining_blacks_cond, remaining_whites_cond,
                      open_blocks_cond, ak_cond)
                if state_server['turn'] == "WHITEWIN":
                    id_win = "WHITE"
                    break
                elif state_server['turn'] == "BLACKWIN":
                    id_win = "BLACK"
                    break
                elif state_server['turn'] == "DRAW":
                    id_win = "DRAW"
                    break
                if self.file_access:
                    state_list.append(state)
                update_used(state_hash_table_tmp, state, self.game.weights, self.game.color)
        except Exception as e:
            print(e)
        finally:
            if self.file_access:
                for state in state_list:
                    state_hash = state.get_hash()
                    hash_result = state_hash_table.get(state_hash)

                    if hash_result is not None:
                        if id_win == "WHITE":
                            hash_result["value"]["white"] += 1
                        elif id_win == "BLACK":
                            hash_result["value"]["black"] += 1
                        elif id_win == "DRAW":
                            hash_result["value"]["black"] += 0.3
                            hash_result["value"]["white"] += 0.3
                        hash_result["games"] += 1

                    else:
                        value = dict()
                        if id_win == "WHITE":
                            value["white"] = 1
                            value["black"] = 0
                        elif id_win == "BLACK":
                            value["black"] = 1
                            value["white"] = 0
                        elif id_win == "DRAW":
                            value["black"] = 0.3
                            value["white"] = 0.3
                        add_to_hash(state_hash_table, state_hash, value)  # Add state and value to hash table
                file = open("state_hash", "wb")
                pickle.dump(state_hash_table, file)
                file.close()
            print("Game ended.")


def add_to_hash(table, state_hash, value):
    """
    Adds current state and its value to hash table.
    """
    table[state_hash] = {"value": value, "games": 1}


def convert_state_hash(state_hash_table):
    new_state_hash_table = dict()
    for key in state_hash_table.keys():
        tmp = state_hash_table[key]
        if tmp.get('bitboards') is not None:
            new_key = (tuple(tmp['bitboards']['king']), tuple(tmp['bitboards']['white']),
                       tuple(tmp['bitboards']['black']))
            if new_state_hash_table.get(new_key) is None:
                new_state_hash_table[new_key] = {"value": tmp['value'], "games": tmp['games']}
        else:
            new_state_hash_table[key] = tmp
    return new_state_hash_table


def clear_hash_table_1(state_has_table):
    if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
        for key in state_has_table.keys():
            if state_has_table[key]['used'] == 0:
                state_has_table.pop(key)
            if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
                break


def clear_hash_table_2(state_has_table, state):
    for key in state_has_table.keys():
        if cont_pieces(state_has_table[key]) > cont_pieces(state):
            state_has_table.pop(key)
        if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
            break
    if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
        for key in state_has_table.keys():
            if state_has_table[key]['used'] == 0:
                state_has_table.pop(key)
            if sys.getsizeof(state_has_table) > MAX_SIZE_DICT:
                break


def cont_pieces(state):
    cnt = 1
    for r in range(0, 9):
        for c in range(0, 9):
            curr_mask = 256 >> c
            if state.white_bitboard[r] & curr_mask != 0:
                cnt += 1
            if state.black_bitboard[r] & curr_mask != 0:
                cnt += 1
    return cnt
