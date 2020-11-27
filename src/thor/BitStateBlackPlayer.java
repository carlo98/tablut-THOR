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
        int curr_mask, remaining_whites_cond, remaining_blacks_cond, ak_cond;
        double blocks_cond;

        int victory_cond = this.check_victory();
        if (victory_cond == -1)  // king captured and black player -> Win
            return Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1)  // King escaped and black player -> Lose
            return -Utils.MAX_VAL_HEURISTIC;

        for(int i = 0; i < this.black_bitboard.length; i++) {
        	blocks_occupied_by_black += Integer.bitCount(this.black_bitboard[i] & Utils.blocks_bitboard[i]);
        }
     
        blocks_cond = weights[0] * blocks_occupied_by_black;
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

        remaining_whites_cond = -1 * weights[3] * white_cnt;
        remaining_blacks_cond = weights[4] * black_cnt;

        ak_cond = -1* weights[5] * this.open_king_paths();
        return blocks_cond + remaining_whites_cond + remaining_blacks_cond  + ak_cond;
	}
	

	@Override
	public BitState produceState(List<Integer> action) {
		return new BitStateBlackPlayer(this, action);
	}
}
