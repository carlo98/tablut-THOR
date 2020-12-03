# tablut-THOR
Agent for University of Bologna Fundamentals of Artificial Intelligence tablut competition:
The agent plays Tablut using Ashton's rules.
The time limit for a single move is 60s

## Instructions
A server build from https://github.com/AGalassi/TablutCompetition.git is required to use the agent.
From VM, in "/tablut", after server launch, launch player from terminal:
- ./runmyplayer "white" time ip_server
- ./runmyplayer "black" time ip_server

time: time available to find action, in seconds, int

ip_server: String
 
 ## General Algorithm
The agent uses the minmax algorithm with alpha-beta pruning, implemented in Java. A worse performing python implementation was also attempted, and can be found in the /tablut folder.
An additional (partial) optimization step has been takend introducing parallelization of search at first depth: while possibly excluding cuts among the main subtrees, it allows to better exploitation of the hardware, with minimal communication overhead.

## Heuristics and Strategy
The heuristic has been kept relatively simple, due to the time limit.
Various conditions are evaluated, giving an idea of both white and black players' strategy.
* Remaining pieces: the difference in pieces is a clear condition of advantage, the weigth of white pieces is higher, since they are fewer
* Aggressive king: positions in which the king has one open path to the exit have a high weight, avoiding (or reaching, depending on colour) such positions is incetivized expecially in they are at max depth: being aggressive might be a strong tactics when the depth reached by both programs is the same.
* Blocks: both white and blacks try to obtain ("block") specific positions on the board, that allow easier movement or control over the area
* Fronts: black player tries to keep a pawn in the front line of the camps: this position is quite powerful because it has rapid access to many quadrants, so it is preferred to first move rear pawns.

## Further ideas and improvements
At the time of submission, we were not able to implement some ideas suggested, which might have improved performance, some are listed below in case of future experimentation:

* Dictionary of states and actions, with symmetry: since the board is extremely symmetric, we considered the option of keeping a dictionary of all the positions reached, their value, and the actions that they generate, in order not to compute them again. At the moment we have not yet found a satisfactory implementation.
* Lazy SMP strategy: the idea is running multiple threads at different levels, each filling the dictonary suggested above, many current chess algorithm use similar strategies, but particular attention must be paid regarding concurrent accessses or data integrity.
* Updating heuristic: heuristics should likely change during the duration of the game: some of the above conditions are better in the early game: a turn or diminishing return might be used to change the strategy.
* King heatmap, last move heatmap: players should keep in consideration the opponent's last move and act accordingly, this might be done including heatmaps changing positional heuristic depending on moves. Due to the usage of bitmap instead of arrrays, heatmaps would introduce overhead in the conversion.
* An additional strategy for black would be the blockade: reduction of number of available moves for the white player.
