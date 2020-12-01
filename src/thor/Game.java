package thor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class Game {
	private int[][] possible_actions_hor = new int[9][9];
	private int[][] possible_actions_ver = new int[9][9];
	private String color;
	
	String getColor() {
		return this.color;
	}

	public Game(int[] weights, String color) {
		super();
		this.color = color;
		this.possible_actions_hor[0][0] = 0b011000000;
		this.possible_actions_hor[0][1] = 0b101000000;
		this.possible_actions_hor[0][2] = 0b110000000;
		this.possible_actions_hor[0][3] = 0b111011111;
		this.possible_actions_hor[0][4] = 0b111101111;
		this.possible_actions_hor[0][5] = 0b111110111;
		this.possible_actions_hor[0][6] = 0b000000011;
		this.possible_actions_hor[0][7] = 0b000000101;
		this.possible_actions_hor[0][8] = 0b000000110;
		this.possible_actions_hor[1][0] = 0b011100000;
		this.possible_actions_hor[1][1] = 0b101100000;
		this.possible_actions_hor[1][2] = 0b110100000;
		this.possible_actions_hor[1][3] = 0b111000000;
		this.possible_actions_hor[1][4] = 0b111101111;
		this.possible_actions_hor[1][5] = 0b000000111;
		this.possible_actions_hor[1][6] = 0b000001011;
		this.possible_actions_hor[1][7] = 0b000001101;
		this.possible_actions_hor[1][8] = 0b000001110;
		this.possible_actions_hor[2][0] = 0b011111111;
		this.possible_actions_hor[2][1] = 0b101111111;
		this.possible_actions_hor[2][2] = 0b110111111;
		this.possible_actions_hor[2][3] = 0b111011111;
		this.possible_actions_hor[2][4] = 0b111101111;
		this.possible_actions_hor[2][5] = 0b111110111;
		this.possible_actions_hor[2][6] = 0b111111011;
		this.possible_actions_hor[2][7] = 0b111111101;
		this.possible_actions_hor[2][8] = 0b111111110;
		this.possible_actions_hor[3][0] = 0b011111110;
		this.possible_actions_hor[3][1] = 0b001111110;
		this.possible_actions_hor[3][2] = 0b010111110;
		this.possible_actions_hor[3][3] = 0b011011110;
		this.possible_actions_hor[3][4] = 0b011101110;
		this.possible_actions_hor[3][5] = 0b011110110;
		this.possible_actions_hor[3][6] = 0b011111010;
		this.possible_actions_hor[3][7] = 0b011111100;
		this.possible_actions_hor[3][8] = 0b011111110;
		this.possible_actions_hor[4][0] = 0b011100000;
		this.possible_actions_hor[4][1] = 0b101100000;
		this.possible_actions_hor[4][2] = 0b000100000;
		this.possible_actions_hor[4][3] = 0b001000000;
		this.possible_actions_hor[4][4] = 0b001101100;
		this.possible_actions_hor[4][5] = 0b000000100;
		this.possible_actions_hor[4][6] = 0b000001000;
		this.possible_actions_hor[4][7] = 0b000001101;
		this.possible_actions_hor[4][8] = 0b000001110;
		this.possible_actions_hor[5] = this.possible_actions_hor[3];
		this.possible_actions_hor[6] = this.possible_actions_hor[2];
		this.possible_actions_hor[7] = this.possible_actions_hor[1];
		this.possible_actions_hor[8] = this.possible_actions_hor[0];
		
		for(int i = 0; i < this.possible_actions_hor.length ; i++)
		{
			for(int j = 0; j < this.possible_actions_hor[0].length; j++)
			{
				this.possible_actions_ver[j][i] = this.possible_actions_hor[i][j];
			}
		}
	}
	
	public List<List<Integer>> produce_actions(BitState state){
		List<List<Integer>> all_actions = new ArrayList<>();
		int new_pos_mask = 0;
		if (state.getTurn() == Turn.WHITE) {
			int r = 0;
		    while (state.getKing_bitboard()[r] == 0) // Searching king row
		    	r += 1;
		    int tmp_r = 8 - r;
		    int c = 0;
		    int tmp_c = 8 - c;
		    int curr_pos_mask = (1 << tmp_c);
		    while ((state.getKing_bitboard()[r] & curr_pos_mask) != curr_pos_mask) {  // Searching king column
		                c += 1;
		                tmp_c = (8 - c);
		                curr_pos_mask = (1 << tmp_c);
		    }
	        // First look for actions that lead to escapes
	        int poss_actions_mask = (~state.getWhite_bitboard()[r] & this.possible_actions_hor[r][c]);  // Horizontal actions
	        poss_actions_mask &= (~state.getBlack_bitboard()[r]);
	        int i = 1;
	        List<List<Integer>> tmp_list = new ArrayList<>();
	        if (c != 0) {
	        	new_pos_mask = curr_pos_mask << i;
	            while (i <= c && (poss_actions_mask & new_pos_mask) != 0) {  // Actions to the left, checkers cannot jump
	            	tmp_list.add(new ArrayList<Integer>(Arrays.asList(1, r, c, r, c-i)));
	                i += 1;
	                if (i <= c)
	                	new_pos_mask = new_pos_mask << 1;
	            }
	            Collections.reverse(tmp_list);
	            all_actions.addAll(tmp_list);
	        }
	        i = 1;
	        tmp_list.clear();
	        if (c != 8) {
	        	new_pos_mask = (curr_pos_mask >> i);  // Actions to the right, checkers cannot jump
	            while (i <= tmp_c && (poss_actions_mask & new_pos_mask) != 0) {
	            	tmp_list.add(new ArrayList<Integer>(Arrays.asList(1, r, c, r, c+i)));
	            	i += 1;
	            	if (i <= tmp_c)
	            		new_pos_mask = new_pos_mask >> 1;
	            }
	            Collections.reverse(tmp_list);
	            all_actions.addAll(tmp_list);
	        }
	        int white_column = Utils.build_column(state.getWhite_bitboard(), curr_pos_mask);  // Building column given position
	        int black_column = Utils.build_column(state.getBlack_bitboard(), curr_pos_mask);
	        curr_pos_mask = (1 << tmp_r);  // Vertical actions
	        poss_actions_mask = (~white_column & this.possible_actions_ver[r][c]);
	        poss_actions_mask &= (~black_column);
	        i = 1;
	        tmp_list.clear();
	        if (r != 0) {
		        new_pos_mask = curr_pos_mask << i;
		        while (i <= r && (poss_actions_mask & new_pos_mask) != 0) {  // Actions up, checkers cannot jump
		        	tmp_list.add(new ArrayList<Integer>(Arrays.asList(1, r, c, r-i, c)));
		            i += 1;
		            if (i <= r)
		            	new_pos_mask = new_pos_mask << 1;
		        }
	        Collections.reverse(tmp_list);
            all_actions.addAll(tmp_list);
	        }
	        i = 1;
	        tmp_list.clear();
	        if (r != 8) {
	        	new_pos_mask = curr_pos_mask >> i;  // Actions down, checkers cannot jump
	            while (i <= tmp_r && (poss_actions_mask & new_pos_mask) != 0) {
	            	tmp_list.add(new ArrayList<Integer>(Arrays.asList(1, r, c, r+i, c)));
	                i += 1;
	                if (i <= tmp_r)
	                	new_pos_mask = new_pos_mask >> 1;
	            }
	            Collections.reverse(tmp_list);
	            all_actions.addAll(tmp_list);
	        }

	        for (r=0; r < state.getWhite_bitboard().length; r++) {  // Searching white pawns
	        	if (state.getWhite_bitboard()[r] != 0) {
	        		tmp_r = 8 - r;
	                for (c=0; c < state.getWhite_bitboard().length; c++) {
	                	curr_pos_mask = (1 << (8 - c));  // Horizontal moves
	                    // If current position is occupied by a white pawn
	                    if ((state.getWhite_bitboard()[r] & curr_pos_mask) == curr_pos_mask) {
	                    	tmp_c = 8 - c;
	                        poss_actions_mask = (~state.getWhite_bitboard()[r] & this.possible_actions_hor[r][c]);
	                        poss_actions_mask &= (~state.getKing_bitboard()[r]);
	                        poss_actions_mask &= (~state.getBlack_bitboard()[r]);
	                        i = 1;
	                        if (c != 0) {
	                        	new_pos_mask = curr_pos_mask << i;  // Actions to the left, checkers cannot jump
	                            while (i <= c && (poss_actions_mask & new_pos_mask) != 0) {
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r, c-i)));
	                                i += 1;
	                                if (i <= c)
	                                	new_pos_mask = new_pos_mask << 1;
	                            }
	                        }
	                        i = 1;
	                        if (c != 8) {
	                        	new_pos_mask = curr_pos_mask >> i;  // Actions to the right, checkers cannot jump
	                            while (i <= tmp_c && (poss_actions_mask & new_pos_mask) != 0) {
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r, c+i)));
	                                i += 1;
	                                if (i <= tmp_c)
	                                	new_pos_mask = new_pos_mask >> 1;
	                            }
	                        }
	                        white_column = Utils.build_column(state.getWhite_bitboard(), curr_pos_mask);  // Building column given position
	                        black_column = Utils.build_column(state.getBlack_bitboard(), curr_pos_mask);
	                        int king_column = Utils.build_column(state.getKing_bitboard(), curr_pos_mask);
	                        curr_pos_mask = (1 << tmp_r);  // Vertical actions
	                        poss_actions_mask = (~white_column & this.possible_actions_ver[r][c]);
	                        poss_actions_mask &= (~black_column);
	                        poss_actions_mask &= (~king_column);
	                        i = 1;
	                        if (r != 0) {
	                        	new_pos_mask = curr_pos_mask << i;  // Actions up, checkers cannot jump
	                            while (i <= r && (poss_actions_mask & new_pos_mask) != 0){
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r-i, c)));
	                                i += 1;
	                                if (i <= r)
	                                	new_pos_mask = new_pos_mask << 1;
	                            }
	                        }
	                        i = 1;
	                        if (r != 8) {
	                        	new_pos_mask = curr_pos_mask >> i;  // Actions down, checkers cannot jump
	                            while (i <= tmp_r && (poss_actions_mask & new_pos_mask) != 0){
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r+i, c)));
	                                i += 1;
	                                if (i <= tmp_r)
	                                	new_pos_mask = new_pos_mask >> 1;
	                            }
	                        }
	                    }
	                }
	        	}
	        }
		}

		else if (state.getTurn() == Turn.BLACK) {
			int r = 0;
		    int c = 0;
		    int tmp_c = 0;
		    int tmp_r = 0;
	        for (r=0; r < state.getBlack_bitboard().length; r++) {  // Searching black pawns
	        	if (state.getBlack_bitboard()[r] != 0) {
	        		tmp_r = 8 - r;
	                for (c=0; c < state.getBlack_bitboard().length; c++) {
	                	int curr_pos_mask = (1 << (8 - c));  // Horizontal moves
	                    // If current position is occupied by a white pawn
	                    if ((state.getBlack_bitboard()[r] & curr_pos_mask) == curr_pos_mask) {
	                    	tmp_c = 8 - c;
	                        int poss_actions_mask = (~state.getWhite_bitboard()[r] & this.possible_actions_hor[r][c]);
	                        poss_actions_mask &= (~state.getKing_bitboard()[r]);
	                        poss_actions_mask &= (~state.getBlack_bitboard()[r]);
	                        int i = 1;
	                        if (c != 0) {
	                        	new_pos_mask = curr_pos_mask << i;  // Actions to the left, checkers cannot jump
	                            while (i <= c && (poss_actions_mask & new_pos_mask) != 0) {
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r, c-i)));
	                                i += 1;
	                                if (i <= c)
	                                	new_pos_mask = new_pos_mask << 1;
	                            }
	                        }
	                        i = 1;
	                        if (c != 8) {
	                        	new_pos_mask = curr_pos_mask >> i;  // Actions to the right, checkers cannot jump
	                            while (i <= tmp_c && (poss_actions_mask & new_pos_mask) != 0) {
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r, c+i)));
	                                i += 1;
	                                if (i <= tmp_c)
	                                	new_pos_mask = new_pos_mask >> 1;
	                            }
	                        }
	                        int white_column = Utils.build_column(state.getWhite_bitboard(), curr_pos_mask);  // Building column given position
	                        int black_column = Utils.build_column(state.getBlack_bitboard(), curr_pos_mask);
	                        int king_column = Utils.build_column(state.getKing_bitboard(), curr_pos_mask);
	                        curr_pos_mask = 1 << tmp_r;  // Vertical actions
	                        poss_actions_mask = (~white_column & this.possible_actions_ver[r][c]);
	                        poss_actions_mask &= (~black_column);
	                        poss_actions_mask &= (~king_column);
	                        i = 1;
	                        if (r != 0) {
	                        	new_pos_mask = curr_pos_mask << i;  // Actions up, checkers cannot jump
	                            while (i <= r && (poss_actions_mask & new_pos_mask) != 0){
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r-i, c)));
	                                i += 1;
	                                if (i <= r)
	                                	new_pos_mask = new_pos_mask << 1;
	                            }
	                        }
	                        i = 1;
	                        if (r != 8) {
	                        	new_pos_mask = curr_pos_mask >> i;  // Actions down, checkers cannot jump
	                            while (i <= tmp_r && (poss_actions_mask & new_pos_mask) != 0){
	                            	all_actions.add(new ArrayList<Integer>(Arrays.asList(0, r, c, r+i, c)));
	                                i += 1;
	                                if (i <= tmp_r)
	                                	new_pos_mask = new_pos_mask >> 1;
	                            }
	                        }
	                    }
	                }
	        	}
	        }
		}
		return all_actions;
	}
	
}
