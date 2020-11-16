from tablut.client.connection_handler import ConnectionHandler
from tablut.search.min_max_parallel import choose_action
from tablut.state.tablut_state import State
from tablut.search.game import Game
from tablut.utils.state_utils import action_to_server_format
from tablut.utils.common_utils import clear_hash_table, update_used, MAX_NUM_CHECKERS


class Client(ConnectionHandler):
    """Extends ConnectionHandler, handling the connection between client and server."""

    def __init__(self, port, color, max_time, host="localhost", weights=None, name=None):
        super().__init__(port, host)
        self.color = color
        self.max_time = max_time
        if weights is None:
            self.weights = [2, 1, 2, 1, 1, 10]  # Best weights find by our genetic algorithm
        else:
            self.weights = weights  # Searching best params
        if name is None:
            self.player_name = "THOR"
        else:
            self.player_name = name
        self.game = Game(self.max_time, self.color, self.weights)
        self.state_hash_tables_tmp = dict()
        for i in range(MAX_NUM_CHECKERS):
            self.state_hash_tables_tmp[i] = dict()

    def run(self, result_search=None):
        """Client's body."""
        try:
            self.connect()
            self.send_string(self.player_name)
            state = State(self.read_string())
            self.state_hash_tables_tmp[0][state.get_hash()] = {"value": 0, 'used': 1}
            while True:  # Playing
                if self.color == state.turn:  # check turn
                    action, value = choose_action(state, self.game, self.state_hash_tables_tmp)  # Retrieving best action and its value and pass weights
                    self.send_string(action_to_server_format(action))
                    print("Choosen action:", action_to_server_format(action))
                    print("Choosen action value:", value)
                else:
                    clear_hash_table(self.state_hash_tables_tmp, state)
                if result_search is not None:
                    state_server = self.read_string()
                    if state_server['turn'] == "WHITEWIN":
                        q.put("WHITE")
                        break
                    elif state_server['turn'] == "BLACKWIN":
                        q.put("BLACK")
                        break
                    elif state_server['turn'] == "DRAW":
                        q.put("DRAW")
                        break
                    else:
                        state = State(state_server)
                else:
                    state = State(self.read_string())
                update_used(self.state_hash_tables_tmp, state, self.game.weights, self.game.color)
        except Exception as e:
            print(e)
        finally:
            print("Game ended.")


