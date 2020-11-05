from tablut.client.connection_handler import ConnectionHandler
from tablut.search import min_max
from tablut.state import StateExchanger

class Client(ConnectionHandler):
    """Extends ConnectionHandler, handling the connection between client and server."""

    def __init__(self, port, color, max_time):
        super().__init__(port, "localhost")
        self.player_name = "THOR"
        self.color = color
        self.max_time = max_time

    def run(self):
        """Client's body."""
        try:
            self.connect()
            self.send_string(self.player_name)
            state = StateExchanger().load_state_from_json(self.read_string(), self.color)
            
            while True:  # Playing
                if self.color == state.turn:  # check turn
                    action, value = min_max.choose_action(#TODO) # Retrieving best action and its value
                    self.send_string(action.to_server_format())
                    print("Choosen action:", action.to_server_format())
                state = StateExchanger().load_state_from_json(self.read_string(), self.color)
                
        except Exception as e:
            print(e)
        finally:
            print("Game ended.")
