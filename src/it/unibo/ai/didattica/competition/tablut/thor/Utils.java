package it.unibo.ai.didattica.competition.tablut.thor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class Utils {

	static final int MAX_NUM_CHECKERS = 25;
	static final int MAX_VAL_HEURISTIC = 5000;
	static final int DRAW_POINTS = MAX_VAL_HEURISTIC-1;
	static final Hashtable<Integer, Integer> lut_positions = new Hashtable<Integer, Integer>() {private static final long serialVersionUID = 1L;
																							  { put(1, 8); put(2, 7); 
																								put(4, 6); put(8, 5);
																								put(16, 4); put(32, 3);
																								put(64, 2); put(128, 1);
																								put(256, 0);}};
	static int[] castle_bitboard = {0b000000000,
		    					    0b000000000,
		    					    0b000000000,
		    					    0b000000000,
		    					    0b000010000,
		    					    0b000000000,
		    					    0b000000000,
		    					    0b000000000,
		    				     	0b000000000};
	static int[] escapes_bitboard = {0b011000110,
									 0b100000001,
								     0b100000001,
								     0b000000000,
								     0b000000000,
								     0b000000000,
								     0b100000001,
								     0b100000001,
								     0b011000110};
	static int[] camps_bitboard = {0b000111000,
							       0b000010000,
							       0B000000000,
							       0b100000001,
							       0b110000011,
							       0b100000001,
							       0b000000000,
							       0b000010000,
							       0b000111000};
	static int[] blocks_bitboard = {0b000000000,
								    0b001000100,
								    0b010000010,
								    0b000000000,
								    0b000000000,
								    0b000000000,
								    0b010000010,
								    0b001000100,
								    0b000000000};
	
	static int build_column(int[] bitboard, int mask) {
	    int num = 0;
	    for (int i=0; i < bitboard.length; i++)
	        if ((bitboard[i] & mask) != 0)
	            num ^= 256 >> i;
	    return num;
	}
	
	static Action action_to_server_format(List<List<Integer>> actions_int){
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
	
	int cont_pieces(BitState state) {
	    int cnt = 0;
	    for (int r = 0; r < 9; r++) {
	        for (int c = 0; c < 9; c++) {
	            int curr_mask = 256 >> c;
	            if ((state.getWhite_bitboard()[r] & curr_mask) != 0)
	                cnt += 1;
	            else if ((state.getBlack_bitboard()[r] & curr_mask) != 0)
	                cnt += 1;
	            else if ((state.getKing_bitboard()[r] & curr_mask) != 0)
	                cnt += 1;
	        }
	    }
	    return cnt;
	}
	
	void clear_hash_table(Hashtable<Integer, Hashtable<Integer, Float>> state_hash_tables, BitState state) {
	    int index_hash = MAX_NUM_CHECKERS - cont_pieces(state) - 1;
	    while (index_hash >= 0 && state_hash_tables.containsKey(index_hash)) {
	        state_hash_tables.remove(index_hash);
	        index_hash -= 1;
	    }
	}
	
	void update_used(Hashtable<Integer, Hashtable<Integer, Float>> state_hash_table, BitState state, int[] weights, String color) {
	    int state_hash = state.hashCode();
	    int index_hash = MAX_NUM_CHECKERS - cont_pieces(state);
	    float hash_result = (float) 0.0;
	    if (state_hash_table.get(index_hash).contains(state_hash)) {
	    	hash_result = state_hash_table.get(index_hash).get(state_hash);
	        //state_hash_table[index_hash][state_hash]['used'] = 1
	    }
	    else {
	        state_hash_table.get(index_hash).put(state_hash, state.compute_heuristic(weights, color)); //{"value": state.compute_heuristic(weights, color),
	                                                                                                    //   "used": 1}
	    }
	}
	
	int[] bit(int n) {
		int[] to_be_returned = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	    int mask = 0b000000001;
	    int i = 0;
	    int b = -1;
	    while (i <= 8) {
	        b = n & mask;
	        to_be_returned[i] = b;
	        mask = mask << 1;
	        i += 1;
	    }
	    return to_be_returned;
	}

	int[] step_rotate(int[] bitboard) {
	    int[] new_bitboard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	    int col = 1, j, i;
	    int[] positions = null;
	    for (int r = 0; r < bitboard.length; r++) {
	        positions = bit(bitboard[r]);
	        for (j = 0; j < positions.length; j++)
	            if(positions[j] != 0) {
	                i = lut_positions.get(positions[j]);
	                new_bitboard[i] ^= col;
	            }
	        col <<=1;
	    }
	    return new_bitboard;
	}
	
	int[] black_tries_capture_white_pawn(int[] black_bitboard, int[] white_bitboard, int row, int col) {
	    int binary_column = 1 << (8 - col);
	    if (row >= 2) {
	        if (Arrays.stream(bit(white_bitboard[row - 1])).anyMatch(i -> i == binary_column)) {
	            if (Arrays.stream(bit(black_bitboard[row - 2])).anyMatch(i -> i == binary_column) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row - 2])).anyMatch(i -> i == binary_column) || 
	            			Arrays.stream(bit(Utils.castle_bitboard[row - 2])).anyMatch(i -> i == binary_column)) {
	                white_bitboard[row - 1] ^= binary_column;
	            }
	        }
	    }
	    if (col <= 6) {
	        if (Arrays.stream(bit(white_bitboard[row])).anyMatch(i -> i == binary_column>>1)) {
	            if (Arrays.stream(bit(black_bitboard[row])).anyMatch(i -> i == binary_column>>2) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row])).anyMatch(i -> i == binary_column>>2) ||
	            			Arrays.stream(bit(Utils.castle_bitboard[row])).anyMatch(i -> i == binary_column>>2)) {
	                white_bitboard[row] ^= binary_column >> 1;
	            }
	        }
	    }
	    if (row <= 6) {
	        if (Arrays.stream(bit(white_bitboard[row+1])).anyMatch(i -> i == binary_column)) {
	            if (Arrays.stream(bit(black_bitboard[row+2])).anyMatch(i -> i == binary_column) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row+2])).anyMatch(i -> i == binary_column) || 
	            			Arrays.stream(bit(Utils.castle_bitboard[row+2])).anyMatch(i -> i == binary_column)) {
	                white_bitboard[row + 1] ^= binary_column;
	            }
	        }
	    }
	    if (col >= 2) {
	        if (Arrays.stream(bit(white_bitboard[row])).anyMatch(i -> i == binary_column<<1)) {
	            if (Arrays.stream(bit(black_bitboard[row])).anyMatch(i -> i == binary_column<<2) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row])).anyMatch(i -> i == binary_column<<2) || 
	            			Arrays.stream(bit(Utils.castle_bitboard[row])).anyMatch(i -> i == binary_column<<2)) {
	                white_bitboard[row] ^= binary_column << 1;
	            }
	        }
	    }
	    return white_bitboard;
	}
}