"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
from tablut.state.tablut_state import State
from tablut.client.connection_handler import ConnectionHandler
from tablut.search.game import Game
from tablut.search.min_max_parallel import choose_action
from tablut.utils.state_utils import action_to_server_format
from tablut.utils.common_utils import clear_hash_table, update_used, MAX_NUM_CHECKERS
import pickle
import os


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
        self.state_hash_tables_tmp = dict()
        for i in range(MAX_NUM_CHECKERS):
            self.state_hash_tables_tmp[i] = dict()

    def run(self):
        """Client's body."""

        if self.file_access:
            exists = os.path.isfile('state_hash')
            if exists:
                file = open("state_hash", "rb")
                state_hash_table = pickle.load(file)
                file.close()
            else:
                state_hash_table = dict()
            state_list = []
        id_win = None
        try:
            self.connect()
            self.send_string(self.player_name)
            state = State(self.read_string())
            self.state_hash_tables_tmp[0][state.get_hash()] = {"value": 0, 'used': 1}
            while True:  # Playing
                if self.color == state.turn:  # check turn
                    action, value = choose_action(state, self.game,
                                                  self.state_hash_tables_tmp)  # Retrieving best action and its value and pass weights
                    self.send_string(action_to_server_format(action))
                    print("Choosen action value:", value)
                else:
                    clear_hash_table(self.state_hash_tables_tmp, state)
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
                update_used(self.state_hash_tables_tmp, state, self.game.weights, self.game.color)
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
                        add_to_hash(state_hash_table, state_hash, value, self.game.produce_actions(state))  # Add state and value to hash table
                file = open("state_hash", "wb")
                pickle.dump(state_hash_table, file)
                file.close()
            print("Game ended.")


def add_to_hash(table, state_hash, value, all_actions):
    """
    Adds current state and its value to hash table.
    """
    table[state_hash] = {"value": value, "games": 1, "all_actions": all_actions}

