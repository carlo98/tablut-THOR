from tablut.client.connection_handler import ConnectionHandler
from tablut.search import min_max
from tablut.state.tablut_state import State
from tablut.search.game import Game


class Client(ConnectionHandler):
    """Extends ConnectionHandler, handling the connection between client and server."""

    def __init__(self, port, color, max_time, weights=None):
        super().__init__(port, "localhost")
        self.player_name = "THOR"
        self.color = color
        self.max_time = max_time
        if weights is None:
            self.weights = [120, 30, 20, 10, 5]  # Best weights find by our genetic algorithm
        else:
            self.weights = weights  # Searching best params

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
                    self.send_string(action.to_server_format())
                    print("Choosen action:", action.to_server_format())
                state = State(self.read_string())
                if result_search is not None and state.win != "NO":  # TODO: Look at how win is returned by state
                    break

            if result_search is not None:
                result_search.append(state.win)  # Append winning color for genetic algorithm
        except Exception as e:
            print(e)
        finally:
            print("Game ended.")
