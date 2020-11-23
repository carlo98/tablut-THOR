package thor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class Utils {

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
	
	public static Action action_to_server_format(List<Integer> list){
		String start_row = "";
		String end_row = "";
		String start_col = "";
		String end_col = "";
		Action a = null;
		start_row = Integer.toString(list.get(1) + 1);
		end_row = Integer.toString(list.get(3) + 1);
		start_col = Integer.toString(65 + list.get(2));
		end_col = Integer.toString(65 + list.get(4));
		try {
			a = new Action(start_col + start_row, end_col + end_row, Turn.DRAW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return a;
	}
	
	static int cont_pieces(BitState state) {
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
	
	static void clear_hash_table(Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_tables, BitState state) {
	    int index_hash = MAX_NUM_CHECKERS - cont_pieces(state) - 1;
	    while (index_hash >= 0 && state_hash_tables.containsKey(index_hash)) {
	        state_hash_tables.remove(index_hash);
	        index_hash -= 1;
	    }
	}
	
	static void update_used(Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table, BitState state, int[] weights, String color) {
	    int state_hash = state.hashCode();
	    int index_hash = MAX_NUM_CHECKERS - cont_pieces(state);
	    if (state_hash_table.get(index_hash).contains(state_hash)) {
	        state_hash_table.get(index_hash).get(state_hash).setUsed();
	    }
	    else {
	        state_hash_table.get(index_hash).put(state_hash, new StateDictEntry(state_hash, state.compute_heuristic(weights, color), null, 1, 0, 0));
	    }
	}
	
	static int[] bit(int n) {
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

	static int[] step_rotate(int[] bitboard) {
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
	
	static int[] black_tries_capture_white_pawn(int[] black_bitboard, int[] white_bitboard, int row, int col) {
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
	
	static int[] black_tries_capture_king(int[] black_bitboard, int[] king_bitboard, int row, int col) {
		int king_row;
		for(king_row = 0; king_row < 9; king_row++)
			if(king_bitboard[king_row] != 0)
				break;
	    int king_col = 8 - lut_positions.get(king_bitboard[king_row]);

	    if ((king_row == 0 || king_row ==  8 || king_col == 0 || king_row == 8) || 
	    		(row != king_row+1 && row != king_row-1 && col != king_col+1 && col != king_col-1))
	        return king_bitboard;

	    int king_bin_col = 1 << (8 - king_col);
	    if (king_row == 4 && king_col == 4)
	        if (Arrays.stream(bit(black_bitboard[king_row - 1])).anyMatch(i -> i == 16) && Arrays.stream(bit(black_bitboard[king_row + 1])).anyMatch(i -> i == 16) 
	        		&& Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 32) && Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 8))
	            king_bitboard[king_row] = 0;
	    else if (king_row == 3 && king_col == 4)
	        if (Arrays.stream(bit(black_bitboard[king_row - 1])).anyMatch(i -> i == 16) && 
	        		Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 32) && 
	        			Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 8))
	            king_bitboard[king_row] = 0;
	    else if (king_row == 4 && king_col == 5)
	        if (Arrays.stream(bit(black_bitboard[king_row - 1])).anyMatch(i -> i == 8) && Arrays.stream(bit(black_bitboard[king_row + 1])).anyMatch(i -> i == 8)
	                && Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 4))
	            king_bitboard[king_row] = 0;
	    else if (king_row == 5 && king_col == 4)
	        if (Arrays.stream(bit(black_bitboard[king_row + 1])).anyMatch(i -> i == 16) && Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 32) 
	        		&& Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 8))
	            king_bitboard[king_row] = 0;
	    else if (king_row == 4 && king_col == 3)
	        if (Arrays.stream(bit(black_bitboard[king_row - 1])).anyMatch(i -> i == 32) && Arrays.stream(bit(black_bitboard[king_row + 1])).anyMatch(i -> i == 32)
	                && Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == 64))
	            king_bitboard[king_row] = 0;
	    else if (king_row == row) {
	        int other_col = 2 * king_col - col;
	        int other_col_bin = 1 << (8 - other_col);
	        if (Arrays.stream(bit(black_bitboard[king_row])).anyMatch(i -> i == other_col_bin))
	            king_bitboard[king_row] = 0;
	    }
	    else {
	        int other_row = 2 * king_row - row;
	        if (Arrays.stream(bit(black_bitboard[other_row])).anyMatch(i -> i == king_bin_col))
	            king_bitboard[king_row] = 0;
	    }
	    return king_bitboard;
	}
	
	static int[] white_tries_capture_black_pawn(int[] white_bitboard, int[] black_bitboard, int row, int col) {
	    int binary_column = 1 << (8 - col);
	    if (row >= 2)
	        if (Arrays.stream(bit(black_bitboard[row-1])).anyMatch(i -> i == binary_column) && (row!=2 || col!= 4))
	            if (Arrays.stream(bit(white_bitboard[row-2])).anyMatch(i -> i == binary_column) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row-2])).anyMatch(i -> i == binary_column) ||
	            			Arrays.stream(bit(Utils.castle_bitboard[row-2])).anyMatch(i -> i == binary_column))
	                black_bitboard[row - 1] ^= binary_column;

	    if (col <= 6)
	        if (Arrays.stream(bit(black_bitboard[row])).anyMatch(i -> i == (binary_column>>1)) && (row!=4 || col!= 6))
	            if (Arrays.stream(bit(white_bitboard[row])).anyMatch(i -> i == (binary_column>>2)) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row])).anyMatch(i -> i == (binary_column>>2))
	                    || Arrays.stream(bit(Utils.castle_bitboard[row])).anyMatch(i -> i == (binary_column>>2)))
	                black_bitboard[row] ^= binary_column >> 1;

	    if (row <= 6)
	        if (Arrays.stream(bit(black_bitboard[row+1])).anyMatch(i -> i == binary_column) && (row!=6 || col!= 4))
	            if (Arrays.stream(bit(white_bitboard[row+2])).anyMatch(i -> i == binary_column) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row+2])).anyMatch(i -> i == binary_column) || 
	            			Arrays.stream(bit(Utils.castle_bitboard[row+2])).anyMatch(i -> i == binary_column))
	                black_bitboard[row + 1] ^= binary_column;
	    if (col >= 2)
	        if (Arrays.stream(bit(black_bitboard[row])).anyMatch(i -> i == (binary_column<<1)) && (row != 4 || col != 2))
	            if (Arrays.stream(bit(white_bitboard[row])).anyMatch(i -> i == (binary_column<<2)) || 
	            		Arrays.stream(bit(Utils.camps_bitboard[row])).anyMatch(i -> i == (binary_column<<2)) || 
	            			Arrays.stream(bit(Utils.castle_bitboard[row])).anyMatch(i -> i == (binary_column<<2)))
	                black_bitboard[row] ^= binary_column << 1;

	    return black_bitboard;
	}
}