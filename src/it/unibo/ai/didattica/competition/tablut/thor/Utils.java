package it.unibo.ai.didattica.competition.tablut.thor;

import java.io.IOException;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class Utils {

	public static final int MAX_NUM_CHECKERS = 25;
	public static final int MAX_VAL_HEURISTIC = 5000;
	public static final int DRAW_POINTS = MAX_VAL_HEURISTIC-1;

	public static int[] castle_bitboard = {0b000000000,
		    					    0b000000000,
		    					    0b000000000,
		    					    0b000000000,
		    					    0b000010000,
		    					    0b000000000,
		    					    0b000000000,
		    					    0b000000000,
		    				     	0b000000000};
	public static int[] escapes_bitboard = {0b011000110,
									 0b100000001,
								     0b100000001,
								     0b000000000,
								     0b000000000,
								     0b000000000,
								     0b100000001,
								     0b100000001,
								     0b011000110};
	public static int[] camps_bitboard = {0b000111000,
							       0b000010000,
							       0B000000000,
							       0b100000001,
							       0b110000011,
							       0b100000001,
							       0b000000000,
							       0b000010000,
							       0b000111000};
	public static int[] blocks_bitboard = {0b000000000,
								    0b001000100,
								    0b010000010,
								    0b000000000,
								    0b000000000,
								    0b000000000,
								    0b010000010,
								    0b001000100,
								    0b000000000};
	
	public static int build_column(int[] bitboard, int mask) {
	    int num = 0;
	    for (int i=0; i < bitboard.length; i++)
	        if ((bitboard[i] & mask) != 0)
	            num ^= 256 >> i;
	    return num;
	}
	
	public static Action action_to_server_format(List<List<Integer>> actions_int){
		String start_row = "";
		String end_row = "";
		String start_col = "";
		String end_col = "";
		Action a = null;
		for(int i = 0; i < actions_int.size(); i++) {
		    start_row = Integer.toString(actions_int.get(i).get(1) + 1);
		    end_row = Integer.toString(actions_int.get(i).get(3) + 1);
		    start_col = Integer.toString(65 + actions_int.get(i).get(2));
		    end_col = Integer.toString(65 + actions_int.get(i).get(4));
		}
		try {
			a = new Action(start_col + start_row, end_col + end_row, Turn.DRAW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return a;
	}
}