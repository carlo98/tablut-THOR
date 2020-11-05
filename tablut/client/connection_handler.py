import json
import socket

class ConnectionHandler:
    """Manages the connection between client and server"""

    def __init__(self, port=80, host="localhost"):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.port = port
        self.host = host

    def connect(self):
        """Connect to the server. Throws Exception in case of error"""
        try:
            self.socket.connect((self.host, self.port))
            return self
        except socket.error:
            print("Can't connect to host: %s at port: %d" % (self.host, self.port))

    def disconnect(self):
        """Disconnect the client."""
        self.socket.close()
        return self

    def send_int(self, integer):
        """Send an integer value to the server."""
        bytes_to_send = integer.to_bytes(4, byteorder='big')
        return self.socket.send(bytes_to_send)  # Send the integer, converted in bytes, to the server

    def send_string(self, message):
        """Send a string value, converted to json, to the server."""
        message = json.dumps(message)  # Convert message to JSON
        self.send_int(len(message))  # Send message length to the server
        self.socket.sendall(message.encode())  # Send the string to the server
        return self

    def read_string(self):
        """Return a string value sent by the server converted in json format."""
        string_length = self.read_int()  # Read the length of the string from an integer sent by the server itself
        message = self.socket.recv(string_length, socket.MSG_WAITALL)  # Read the actual string
        message = message.decode('utf-8')  # Decode the message
        return json.loads(message)  # Convert object to JSON

    def read_int(self):
        """Return an integer value sent by the server."""
        read_bytes = b''  # Create empty bytes
        while len(read_bytes) < 4:  # Wait for all the bytes
            data = self.socket.recv(4 - len(read_bytes))
            if not data:
                break
            read_bytes += data
        return int.from_bytes(read_bytes, byteorder='big')  # Converts bytes to int
