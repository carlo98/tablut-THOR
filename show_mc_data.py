"""
Date: 07/11/2020
Author: Carlo Cena
"""
import pickle
import os


exists = os.path.isfile('state_hash')
if exists:
    file = open("state_hash", "rb")
    state_hash_table = pickle.load(file)
    file.close()
    point_white = 0
    point_black = 0
    games = 0
    for state in state_hash_table.values():
        point_white += state['value']['white']
        point_black += state['value']['black']
        games += state['games']
        print("White: ", state['value']['white'] / state['games'], " Black: ", state['value']['black'] / state['games'])
    print("Points over total number of matches:")
    print("Total Black: ", point_black / games)
    print("Total White: ", point_white / games)
else:
    print("File doesn't exist.")
