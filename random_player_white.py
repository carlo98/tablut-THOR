"""
Date: 07/11/2020
Author: Carlo Cena

Implementation of minmax algorithm with alpha-beta pruning.
"""
from tablut.state.tablut_state import State
from tablut.client.connection_handler import ConnectionHandler
from tablut.search.game import Game
from tablut.search.min_max import choose_action
from tablut.utils.state_utils import action_to_server_format
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
        game = Game(self.max_time, self.color, self.weights)
        try:
            self.connect()
            self.send_string(self.player_name)
            state = State(self.read_string())
            
            while True:  # Playing
                if self.color == state.turn:  # check turn
                    action, _ = choose_action(state, game)  # Retrieving best action and its value and pass weights
                    self.send_string(action_to_server_format(action))
                state_server = self.read_string()
                state = State(state_server)
                blocks_cond, remaining_blacks_cond, remaining_whites_cond, open_blocks_cond, ak_cond = state.compute_heuristic_test(
                    game.weights, game.color)
                print(game.color, blocks_cond, remaining_blacks_cond, remaining_whites_cond, open_blocks_cond, ak_cond)
                if state_server['turn'] == "WHITEWIN":
                    id_win = "WHITE"
                    break
                elif state_server['turn'] == "BLACKWIN":
                    id_win = "BLACK"
                    break
                elif state_server['turn'] == "DRAW":
                    id_win = "DRAW"
                    break
                #state = State(state_server)
                if self.file_access:
                    state_list.append(state)
        except Exception as e:
            print(e)
        finally:
            if self.file_access:
                for state in state_list:
                    quad_prob = 1
                    pot = 1
                    state_hash = state.get_hash()
                    m_key = (state_hash[0] + pot, state_hash[1] + pot, state_hash[2] + pot)
                    hash_result = state_hash_table.get(m_key)

                    while hash_result is not None:
                        if state.equal(hash_result["bitboards"]):
                            if id_win == "WHITE":
                                hash_result["value"]["white"] += 1
                            elif id_win == "BLACK":
                                hash_result["value"]["black"] += 1
                            elif id_win == "DRAW":
                                hash_result["value"]["black"] += 0.3
                                hash_result["value"]["white"] += 0.3
                            hash_result["games"] += 1

                        quad_prob += 1
                        pot = quad_prob ** quad_prob
                        m_key = (state_hash[0] + pot, state_hash[1] + pot, state_hash[2] + pot)
                        hash_result = state_hash_table.get(m_key)

                    if hash_result is None:
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
                        add_to_hash(state_hash_table, state, state_hash, value)  # Add state and value to hash table
                file = open("state_hash", "wb")
                pickle.dump(state_hash_table, file)
                file.close()
            print("Game ended.")


def add_to_hash(table, state, state_hash, value):
    """
    Adds current state and its value to hash table.
    """
    quad_prob = 1
    pot = 1
    m_key = (state_hash[0]+pot, state_hash[1]+pot, state_hash[2]+pot)
    while table.get(m_key) is not None:
        quad_prob += 1
        pot = quad_prob ** quad_prob
        m_key = (state_hash[0]+pot, state_hash[1]+pot, state_hash[2]+pot)
    table[m_key] = {"bitboards": {"black": state.black_bitboard, "white": state.white_bitboard,
                                  "king": state.king_bitboard},
                    "value": value, "games": 1}