"""
Date: 05/11/2020
Author: Carlo Cena & Giacomo Zamprogno

Population-based search for best weight of heuristic's component.
"""
from multiprocessing import Pool
import subprocess
import time
import numpy as np
from random_player_white import Client

MAX_TIME_ACTION = 5  # Maximum time allowed to search for action


def create_play_player(port, color, max_time_act, weights, name, host, file_access):
    client_m = Client(port, color, max_time_act, weights=weights, name=name, host=host, file_access=file_access)
    client_m.run()


def run():
    """
    Starts two matches between a couple of solutions,
    returns point earned by each one.
    """
    pool = Pool()
    proc = subprocess.Popen('ant server', shell=True)  # Requires server files in path
    time.sleep(5)
    white_thread = pool.apply_async(create_play_player,
                                    args=(5800, "WHITE", MAX_TIME_ACTION,
                                          [np.random.rand() * 50, np.random.rand() * 50,
                                           np.random.rand() * 50, np.random.rand() * 50,
                                           np.random.rand() * 50, np.random.rand() * 50],
                                          "EHILA", "127.0.0.1", True))
    #
    time.sleep(1)
    # Important, pass list just to one of the two threads, to avoid synchronization problems
    black_thread = pool.apply_async(create_play_player,
                                    args=(5801, "BLACK", MAX_TIME_ACTION,
                                          [np.random.rand() * 50, np.random.rand() * 50,
                                           np.random.rand() * 50, np.random.rand() * 50,
                                           np.random.rand() * 50, np.random.rand() * 50],
                                          "OOOOO", "127.0.0.1", False))
    black_thread.wait()
    white_thread.wait()
    pool.close()
    proc.terminate()


cont = 0
while cont <= 30:
    run()
    cont += 1
