package thor;

import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class BitStateWhitePlayer extends BitState {
	
	private int[] weights = {20, 0, 0, 40, 30, 100};
	
	public BitStateWhitePlayer(State state) {
		super(state);
		
	}

	public BitStateWhitePlayer() {
		super();
	}

	public BitStateWhitePlayer(BitState s, List<Integer> action) {
		super(s, action);
		
	}
	
	@Override
	public double compute_heuristic() {
		int white_cnt = 0, black_cnt = 0;
        int curr_mask, remaining_whites_cond, remaining_blacks_cond, ak_cond;
        int victory_cond = this.check_victory();
        
        if (victory_cond == -1)  // King captured and white player -> Lose
            return -Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1)  // King escaped and white player -> Win
            return Utils.MAX_VAL_HEURISTIC;
        
        
        //TODO add open blocks heuristic to focus there
        
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

        remaining_whites_cond =  weights[3] * white_cnt;
        remaining_blacks_cond = -1* weights[4] * black_cnt;

        ak_cond = weights[5] * this.open_king_paths();
        return  remaining_whites_cond + remaining_blacks_cond + ak_cond;
	}
	
	@Override
	public BitState produceState(List<Integer> action) {
		return new BitStateWhitePlayer(this, action);
	}
}
