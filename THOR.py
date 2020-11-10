import argparse

from tablut.client.tablut_client import Client

parser = argparse.ArgumentParser(description='Default: Color = White and Timeout = 60s')
parser.add_argument("-c", "--color", default='white', help="Set the player color.")
parser.add_argument("-t", "--max_time", type=int, default=10, help="Change max_time")

args = parser.parse_args()
if args.color.lower() == 'white':
    color = "WHITE"  
elif args.color.lower() == 'black':
    color = "BLACK"
else:
    print("Wrong color, possible choices are white/black")
    exit(-1)
max_time = args.max_time

if color == "WHITE":
    server_port = 5800
else:
    server_port = 5801

Client(server_port, color, max_time-1).run()
