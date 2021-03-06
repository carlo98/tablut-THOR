package thor;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

/**
 * @author Carlo Cena, Giacomo Zamprogno
 * @implNote Class used for static constants and functions used by more than one class.
 *
 */
public class Utils {

	static public final int MAX_VAL_HEURISTIC = 5000;
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
	
	static int[] blocksWhite_bitboard = {0b111000111,
									    0b100101001,
									    0b100000001,
									    0b010000010,
									    0b000000000,
									    0b010000010,
									    0b100000001,
									    0b100101001,
									    0b111000111};
	
	public static int[] wings_bitboard = {0b000101000,
										0b000000000,
										0b000000000,
										0b100000001,
										0b000000000,
										0b100000001,
										0b000000000,
										0b000000000,
										0b000101000};
	
	/**
	 * @param bitboard int[], bitboard from which one needs to extract a column
	 * @param mask int, Identifies the column, number represented with just one 1 in binary representation with 9 bits
	 * @return int, in binary representation 1s are pawns or king and 0s free positions.
	 */
	static int build_column(int[] bitboard, int mask) {
	    int num = 0;
	    for (int i=0; i < bitboard.length; i++) {
	        if ((bitboard[i] & mask) != 0) {
	            num ^= (256 >> i);
	        }
	    }
	    return num;
	}

	/**
	 * @param list List<Integer>, action to be sent to server
	 * @return Action, action in server format
	 */
	public static Action action_to_server_format(List<Integer> list){
		int start_row ;
		int end_row;
		char start_col;
		char end_col;
		Action a = null;
		start_row = (list.get(1) + 1);
		end_row = (list.get(3) + 1);
		start_col = (char)(65 + list.get(2));
		end_col = (char)(65 + list.get(4));
		try {
			a = new Action(String.valueOf(start_col) + String.valueOf(start_row), 
					String.valueOf(end_col) + String.valueOf(end_row), Turn.DRAW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return a;
	}

	/**
	 * @param state BitState, state of which to count number of pieces
	 * @return int, number of pieces.
	 */
	static int cont_pieces(BitState state) {
	    int cnt = 0;
	    int curr_mask;
	    for (int r = 0; r < 9; r++) {
	        for (int c = 0; c < 9; c++) {
	            curr_mask = (256 >> c);
	            if ((state.getWhite_bitboard()[r] & curr_mask) != 0) {
	                cnt += 1;
	            }
	            else if ((state.getBlack_bitboard()[r] & curr_mask) != 0) {
	                cnt += 1;
	            }
	            else if ((state.getKing_bitboard()[r] & curr_mask) != 0) {
	                cnt += 1;
	            }
	        }
	    }
	    return cnt;
	}

	/**
	 * @param state_hash_table ConcurrentHashMap, hash table in which to save state
	 * @param state BitState, state to be saved in hash table.
	 */
	static void update_used(ConcurrentHashMap<Integer, StateDictEntry> state_hash_table, BitState state) {
	    int state_hash = state.hashCode();
	    if (state_hash_table.contains(state_hash)) {
	        state_hash_table.get(state_hash).setUsed();
	    }
	    else {
	        state_hash_table.put(state_hash, new StateDictEntry(state_hash, 1));
	    }
	}
	
	/**
	 * @param n int, number of which one wants the binary representation, assumes 9 bits.
	 * @return int[], 9 bits binary representation.
	 */
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
	
	/**
	 * @param bitboard int[], to be rotated
	 * @return int[], bitboard rotated.
	 */
	static int[] step_rotate(int[] bitboard) {
	    int[] new_bitboard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	    int col = 1, j, i;
	    int[] positions = null;
	    for (int r = 0; r < bitboard.length; r++) {
	        positions = bit(bitboard[r]);
	        for (j = 0; j < positions.length; j++) {
	            if(positions[j] != 0) {
	                i = lut_positions.get(positions[j]);
	                new_bitboard[i] ^= col;
	            }
	        }
	        col <<=1;
	    }
	    return new_bitboard;
	}
	
	/**
	 * @param black_bitboard int[], Black bitboard
	 * @param white_bitboard int[], White bitboard
	 * @param row int, End row of last action
	 * @param col int, End column of last action
	 * @return int[], new White Bitboard, without captured pawns, if any.
	 */
	static int[] black_tries_capture_white_pawn(int[] black_bitboard, int[] white_bitboard, int row, int col) {
	    int binary_column = (1 << (8 - col));
	    if (row >= 2) {
	        if ((white_bitboard[row - 1] & binary_column) != 0) {
	            if ((black_bitboard[row - 2] & binary_column) != 0 || 
	            		(Utils.camps_bitboard[row - 2] & binary_column) != 0 || 
	            			(Utils.castle_bitboard[row - 2] & binary_column) != 0) {
	                white_bitboard[row - 1] ^= binary_column;
	            }
	        }
	    }
	    if (col <= 6) {
	        if ((white_bitboard[row] & (binary_column>>1)) != 0) {
	            if ((black_bitboard[row] & (binary_column>>2)) != 0 || 
	            		(Utils.camps_bitboard[row] & (binary_column>>2)) != 0 ||
	            			(Utils.castle_bitboard[row] & (binary_column>>2)) != 0) {
	                white_bitboard[row] ^= (binary_column >> 1);
	            }
	        }
	    }
	    if (row <= 6) {
	        if ((white_bitboard[row+1] & binary_column) != 0) {
	            if ((black_bitboard[row+2] & binary_column) != 0 || 
	            		(Utils.camps_bitboard[row+2] & binary_column) != 0 || 
	            			(Utils.castle_bitboard[row+2] & binary_column) != 0) {
	                white_bitboard[row + 1] ^= binary_column;
	            }
	        }
	    }
	    if (col >= 2) {
	        if ((white_bitboard[row] & (binary_column<<1)) != 0) {
	            if ((black_bitboard[row] & (binary_column<<2)) != 0 || 
	            		(Utils.camps_bitboard[row] & (binary_column<<2)) != 0|| 
	            			(Utils.castle_bitboard[row] & (binary_column<<2)) != 0) {
	                white_bitboard[row] ^= (binary_column << 1);
	            }
	        }
	    }
	    return white_bitboard;
	}
	
	/**
	 * @param black_bitboard int[],Black bitboard
	 * @param king_bitboard int[], King bitboard
	 * @param row int, End row of last action
	 * @param col int, End column of last action
	 * @return int[], new King Bitboard, without king, if captured.
	 */
	static int[] black_tries_capture_king(int[] black_bitboard, int[] king_bitboard, int row, int col) {
		int king_row;
		for(king_row = 0; king_row < 9; king_row++) {
			if(king_bitboard[king_row] != 0) {
				break;
			}
		}
	    int king_col = lut_positions.get(king_bitboard[king_row]);
	    
	    //king is at edges or the moved pawn does not attack the king
	    if ((king_row == 0 || king_row ==  8 || king_col == 0 || king_col == 8) || 
	    		!((row == king_row && col == king_col+1) || (row == king_row && col == king_col-1) || 
	    				(row == king_row +1 && col == king_col) || (row == king_row-1 && col == king_col))) {
	        return king_bitboard;
	    }
	    
	    
	
	    int king_bin_col = 1 << (8 - king_col);
	    if (king_row == 4 && king_col == 4) {
	        if ((black_bitboard[king_row - 1] & 16) != 0 && (black_bitboard[king_row + 1] & 16) != 0 
	        		&& (black_bitboard[king_row] & 32) != 0 && (black_bitboard[king_row] & 8) != 0) {
	            king_bitboard[king_row] = 0;
	        }
	    }
	    else if (king_row == 3 && king_col == 4) {
	        if ((black_bitboard[king_row - 1] & 16) != 0 && 
	        		(black_bitboard[king_row] & 32) != 0 && 
	        			(black_bitboard[king_row] & 8) != 0) {
	            king_bitboard[king_row] = 0;
	        }
	    }
	    else if (king_row == 4 && king_col == 5) {
	        if ((black_bitboard[king_row - 1] & 8) != 0 && (black_bitboard[king_row + 1] & 8) != 0
	                && (black_bitboard[king_row] & 4) != 0) {
	            king_bitboard[king_row] = 0;
	        }
	    }
	    else if (king_row == 5 && king_col == 4) {
	        if ((black_bitboard[king_row + 1] & 16) != 0 && (black_bitboard[king_row] & 32) != 0 
	        		&& (black_bitboard[king_row] & 8) != 0) {
	            king_bitboard[king_row] = 0;
	        }
	    }
	    else if (king_row == 4 && king_col == 3) {
	        if ((black_bitboard[king_row - 1] & 32) != 0 && (black_bitboard[king_row + 1] & 32) != 0
	                && (black_bitboard[king_row] & 64) != 0) {
	            king_bitboard[king_row] = 0;
	        }
	    }
	    else if (king_row == row) {
	        int other_col = 2 * king_col - col;
	        int other_col_bin = 1 << (8 - other_col);
	        if ((black_bitboard[king_row] & other_col_bin) != 0 || 
	        		(Utils.camps_bitboard[king_row] & other_col_bin) != 0) {
	            king_bitboard[king_row] = 0;
	        
	        }
	    }
	    else {
	        int other_row = 2 * king_row - row;
	        if ((black_bitboard[other_row] & king_bin_col) != 0 ||
	        		(Utils.camps_bitboard[other_row] & king_bin_col) != 0) {
	            king_bitboard[king_row] = 0;
	        }
	    }
	    return king_bitboard;
	}
	
	/**
	 * @param white_bitboard int[], White bitboard plus king
	 * @param black_bitboard int[], Black bitboard
	 * @param row int, End row of last action
	 * @param col int, End column of last action
	 * @return int[], new Black Bitboard, without captured pawns, if any.
	 */
	static int[] white_tries_capture_black_pawn(int[] white_bitboard, int[] black_bitboard, int row, int col) {
	    int binary_column = (1 << (8 - col));
	    //upwards capture
	    if (row >= 2) {
	    	//check if there is a black pawn above
	        if ((black_bitboard[row-1] & binary_column) != 0 ) {
	        	//check if the black pawn is on a camp
	        	if ((black_bitboard[row-1] & Utils.camps_bitboard[row-1]&(binary_column)) == 0) {
	        		//not on a camp, can use pawns, camp or castle to capture
		        	if ((white_bitboard[row-2] & binary_column) != 0 || 
		            		(Utils.camps_bitboard[row-2] & binary_column) != 0||
		            			(Utils.castle_bitboard[row-2] & binary_column) != 0) {
		                black_bitboard[row - 1] ^= binary_column;
		                
		            }
	        	} else {
	        		//black pawn on a camp, only pawns can capture it
	        		if ((white_bitboard[row-2] & binary_column) != 0) {
		                black_bitboard[row - 1] ^= binary_column;
	        		}
	        	}
	        }
	    }
	    //right capture
	    if (col <= 6) {
	    	//check if there is a black pawn to the right
	        if ((black_bitboard[row] & (binary_column>>1)) != 0) {
	        	//check if the black pawn is on a camp
	        	if (((black_bitboard[row]&Utils.camps_bitboard[row])&(binary_column>>1)) == 0) {
	        		//not on a camp, can use pawns, camp or castle to capture
		            if ((white_bitboard[row] & (binary_column>>2)) != 0 || 
		            		(Utils.camps_bitboard[row] & (binary_column>>2)) != 0
		                    || (Utils.castle_bitboard[row] & (binary_column>>2)) != 0) {
		                black_bitboard[row] ^= (binary_column >> 1);
		                
		            }
	        	} else {
	        		//black pawn on a camp, only pawns can capture it
	        		if ((white_bitboard[row] & (binary_column>>2)) != 0) {
		                black_bitboard[row] ^= (binary_column >> 1);		                
		            }
	        	}
	        }
	    }
	    //downwards capture
	    if (row <= 6) {
	    	//check if there is a black below
	        if ((black_bitboard[row+1] & binary_column) != 0) {
	        	//check if the black pawn is on a camp
	        	if (((black_bitboard[row+1]&Utils.camps_bitboard[row+1])&binary_column) == 0) {
	        		//not on a camp, can use pawns, camp or castle to capture
		            if ((white_bitboard[row+2] & binary_column) != 0 || 
		            		(Utils.camps_bitboard[row+2] & binary_column) != 0 || 
		            			(Utils.castle_bitboard[row+2] & binary_column) != 0) {
		                black_bitboard[row + 1] ^= binary_column;
		                
		            }
	        	} else {
	        		//black pawn on a camp, only pawns can capture it
	        		if ((white_bitboard[row+2] & binary_column) != 0) {
		                black_bitboard[row + 1] ^= binary_column;  
		            }
	        	}
	        }
	    }
	    //left capture
	    if (col >= 2) {
	    	//check if there is a black pawn to the left
	        if ((black_bitboard[row] & (binary_column<<1)) != 0 ) {
	        	//check if the black pawn is on a camp
	        	if (((black_bitboard[row]&Utils.camps_bitboard[row])&(binary_column<<1)) == 0) {
	        		//not on a camp, can use pawns, camp or castle to capture
		            if ((white_bitboard[row] & (binary_column<<2)) != 0|| 
		            		(Utils.camps_bitboard[row] & (binary_column<<2)) != 0|| 
		            			(Utils.castle_bitboard[row] & (binary_column<<2)) != 0) {
		                black_bitboard[row] ^= (binary_column << 1);
		                
		            }
	        	} else {
	        		//black pawn on a camp, only pawns can capture it
	        		if ((white_bitboard[row] & (binary_column<<2)) != 0) {
		                black_bitboard[row] ^= (binary_column << 1);
		                
		            }
	        	}
	        }
	    }
	    
	    return black_bitboard;
	}
}
