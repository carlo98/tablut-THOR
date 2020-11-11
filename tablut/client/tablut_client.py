from tablut.client.connection_handler import ConnectionHandler
from tablut.search import min_max
from tablut.state.tablut_state import State
from tablut.search.game import Game
from tablut.utils.state_utils import action_to_server_format


class Client(ConnectionHandler):
    """Extends ConnectionHandler, handling the connection between client and server."""

    def __init__(self, port, color, max_time, host="localhost", weights=None, name=None):
        super().__init__(port, host)
        self.color = color
        self.max_time = max_time
        if weights is None:
            self.weights = [1, 1, 1, 1]  # Best weights find by our genetic algorithm
        else:
            self.weights = weights  # Searching best params
        if name is None:
            self.player_name = "THOR"
        else:
            self.player_name = name

    def run(self, result_search=None):
        """Client's body."""
        try:
            self.connect()
            self.send_string(self.player_name)
            state = State(self.read_string())
            game = Game(self.max_time, self.color, self.weights)
            
            while True:  # Playing
                if self.color == state.turn:  # check turn
                    action, value = min_max.choose_action(state, game)  # Retrieving best action and its value and pass weights
                    self.send_string(action_to_server_format(action))
                    print("Choosen action:", action_to_server_format(action))
                if result_search is not None:
                    state_server = self.read_string()
                    if state_server.turn == "WHITEWIN":
                        result_search.append("WHITEWIN")
                    elif state_server.turn == "BLACKWIN":
                        result_search.append("BLACKWIN")
                    else:
                        state = State(self.read_string())
                else:
                    state = State(self.read_string())
        except Exception as e:
            print(e)
        finally:
            print("Game ended.")
