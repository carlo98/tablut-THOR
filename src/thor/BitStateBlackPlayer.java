package thor;

import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class BitStateBlackPlayer extends BitState {

	private int[] weights = {20, 0, 0, 40, 30, 100};
	
	public BitStateBlackPlayer() {
		super();
	}

	public BitStateBlackPlayer(BitState s, List<Integer> action) {
		super(s, action);
	}

	public BitStateBlackPlayer(State state) {
		super(state);
	}

	@Override
	public double compute_heuristic() {
		int blocks_occupied_by_black = 0;
		int white_cnt = 0, black_cnt = 0;
        int curr_mask, available_actions =0, ak_cond, camps_cond;
        int blocks_cond, wings_cond;
		double h_partial, h_return;
        //double threshold = 350;

        
        /*look up tables*/
        int [] camps_lut= {0,-15,-30,-45,-60};
        int [] wings_lut= {10, 0, -15, -30, -45, -60, -75, -90, -105};
        int [] blocks_lut = {0, 5, 10, 30, 40, 60, 70, 80, 100};
        int [] ak_lut = {0, -100, -1000, -1000, -1000};
        //int [] difference_lut = {-60,-40,-20,-5,0,5,20,40,80};
        
        //check for victory position
        int victory_cond = this.check_victory();
        if (victory_cond == -1)  // king captured and black player -> Win
            return Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1)  // King escaped and black player -> Lose
            return -Utils.MAX_VAL_HEURISTIC;
        
      //diagonal blocks halts the escape for the king, from there, the blockade strategy can be applied
        for(int i = 0; i < this.black_bitboard.length; i++) {
        	blocks_occupied_by_black += Integer.bitCount(this.black_bitboard[i] & Utils.blocks_bitboard[i]);
        }
       
        blocks_cond = blocks_occupied_by_black;
        //System.out.println("blocks = "+ blocks_cond);
        //backline camps should be opened first, to have more available pawns
        
        
        camps_cond = this.locked_back_camps();
        wings_cond = this.locked_wings();
        //System.out.println("wings = "+ wings_cond);
        
        //difference in pieces is a good indicator of advantage
        for (int r = 0; r < this.black_bitboard.length; r++) {
            for (int c = 0; c < this.black_bitboard.length; c++) {
                curr_mask = (1 << (8 - c));
                if ((this.white_bitboard[r] & curr_mask) != 0) {
                    white_cnt += 1;
                }
                if ((this.black_bitboard[r] & curr_mask) != 0) {
                    black_cnt += 1;
                }
            }
        }
        //System.out.println("whites = "+ white_cnt);
        //System.out.println("blacks = "+ black_cnt);
        // difference_cond = (black_cnt-16) - (white_cnt-8);
        
        //it is necessary to avoid the king having any number of open paths
        ak_cond = this.open_king_paths();
        //System.out.println("ak = "+ ak_cond);
        
        h_partial = wings_lut[wings_cond]
        		-50*white_cnt + 50*black_cnt + ak_lut[ak_cond];
        
        //System.out.println("partial = "+ h_partial);
        
		if (white_cnt <= 3) {
			available_actions=count_king_actions();
        	h_return = h_partial -5*(available_actions);
		} else {
        	h_return = h_partial + blocks_lut[blocks_cond];
        	
        }
        //System.out.println("partial = "+ h_partial);
        return h_return;
	}
	

	

	@Override
	public BitState produceState(List<Integer> action) {
		return new BitStateBlackPlayer(this, action);
	}
	
	public int count_king_actions() {
		
		int cnt_actions  = 0;
		int new_pos_mask = 0;
	
		int r = 0;
	    while (king_bitboard[r] == 0) // Searching king row
	    	r += 1;
	    
	    int tmp_r = 8 - r;
	    int c = 0;
	    int tmp_c = 8 - c;
	    int curr_pos_mask = (1 << tmp_c);
	    
	    while ((king_bitboard[r] & curr_pos_mask) != curr_pos_mask) {  // Searching king column
	                c += 1;
	                tmp_c = (8 - c);
	                curr_pos_mask = (1 << tmp_c);
	    }
	    
        
	    Game g = new Game("BLACK");
        int poss_actions_mask = (~white_bitboard[r] & g.possible_actions_hor[r][c]);  // Horizontal actions
        poss_actions_mask &= (~black_bitboard[r]);
        
        int i = 1;
        
        if (c != 0) {
        	new_pos_mask = curr_pos_mask << i;
            while (i <= c && (poss_actions_mask & new_pos_mask) != 0) {  // Actions to the left, checkers cannot jump
            	cnt_actions++;
                i += 1;
                if (i <= c)
                	new_pos_mask = new_pos_mask << 1;
            }
            
        }
        i = 1;
        
        if (c != 8) {
        	new_pos_mask = (curr_pos_mask >> i);  // Actions to the right, checkers cannot jump
            while (i <= tmp_c && (poss_actions_mask & new_pos_mask) != 0) {
            	
            	cnt_actions++;
            	i += 1;
            	if (i <= tmp_c)
            		new_pos_mask = new_pos_mask >> 1;
            }
            
        }
        int white_column = Utils.build_column(white_bitboard, curr_pos_mask);  // Building column given position
        int black_column = Utils.build_column(black_bitboard, curr_pos_mask);
        curr_pos_mask = (1 << tmp_r);  // Vertical actions
        poss_actions_mask = (~white_column & g.possible_actions_ver[r][c]);
        poss_actions_mask &= (~black_column);
        i = 1;
        
        if (r != 0) {
	        new_pos_mask = curr_pos_mask << i;
	        while (i <= r && (poss_actions_mask & new_pos_mask) != 0) {  // Actions up, checkers cannot jump
	        	
	        	cnt_actions++;
	            i += 1;
	            if (i <= r)
	            	new_pos_mask = new_pos_mask << 1;
	        }
        
        }
        i = 1;
        
        if (r != 8) {
        	new_pos_mask = curr_pos_mask >> i;  // Actions down, checkers cannot jump
            while (i <= tmp_r && (poss_actions_mask & new_pos_mask) != 0) {
            	
            	cnt_actions++;
                i += 1;
                if (i <= tmp_r)
                	new_pos_mask = new_pos_mask >> 1;
            }
            
        }
		return cnt_actions;

	}

	
	
	public int locked_wings() {
		int locked_wings = 0;
		for(int i = 0; i < this.black_bitboard.length; i++) {
        	locked_wings += Integer.bitCount(this.black_bitboard[i] & Utils.wings_bitboard[i]);
        }
		return locked_wings;
	}
}
