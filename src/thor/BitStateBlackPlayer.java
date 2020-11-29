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
	
	public BitStateBlackPlayer(BitState s) {
		super(s, 0);
	}

	@Override
	public double compute_heuristic() {
		int blocks_occupied_by_black = 0;
		int white_cnt = 0, black_cnt = 0;
        int curr_mask, difference_cond, ak_cond, camps_cond;
        int blocks_cond;
		double h_partial, h_return;
        double threshold = 500;
        
        /*look up tables*/
        int [] camps_lut= {0,-15,-30,-45,-60};
        int [] blocks_lut = {0, 10, 20, 30, 40, 50, 60, 70, 80};
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
        
        //backline camps should be opened first, to have more available pawns
        
        camps_cond = this.locked_back_camps();
        
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
        difference_cond = (black_cnt-16) - (white_cnt-8);
        
        //it is necessary to avoid the king having any number of open paths
        ak_cond = this.open_king_paths();
        
        h_partial = blocks_lut[blocks_cond]  + camps_lut[camps_cond] 
        		-40*white_cnt + 30*black_cnt + ak_lut[ak_cond];
        
        
		if (h_partial <= threshold) 
        	h_return = h_partial;
        else
        	h_return = h_partial;
        return h_return;
	}
	

	@Override
	public BitState produceState(List<Integer> action) {
		return new BitStateBlackPlayer(this, action);
	}
}
