"""
Date: 08/11/2020
Author: Carlo Cena

Implementation of method required by tablut game.
"""
import time
from threading import Thread


class Game:
    def __init__(self, max_time, weights):
        self.max_time = max_time
        self.weights = weights

    def produce_actions(self, state, turn, time_start):
        """

        """
        # Iterate over action, if time.time() - time_start >= self.max_time stop
        # Use Threads
        pass

    def apply_action(self, state, action):
        """

        """
        pass

