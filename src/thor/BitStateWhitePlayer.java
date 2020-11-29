package thor;

import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class BitStateWhitePlayer extends BitState {
	
	private int[][] lut = {{-Utils.MAX_VAL_HEURISTIC, 10, 50, 110, 1800, 250, 300, 350, 400, 450},  // Remaining white
							{Utils.MAX_VAL_HEURISTIC, -5, -10, -40, -80, -120, -150, -180, -200, -220, -240, -260, -280, -300, -320, -340, -360},  // Remaining black
							{-300, -200, -180, -100, 70, 80, 100, 450, 500},  // Open diagonal blocks
							{0, 200, 320, 2000, 4000}  // Aggressive king, number of path open to escapes
							};
	
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
		int white_cnt = 1, black_cnt = 0;
        int curr_mask, remaining_whites_cond, remaining_blacks_cond, ak_cond, blocks_cond, blocks_occupied_by_black=8;
        int victory_cond = this.check_victory();
        
        if (victory_cond == -1)  // King captured
            return -Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1)  // King escaped
            return Utils.MAX_VAL_HEURISTIC;
        
        for(int i = 0; i < this.black_bitboard.length; i++) {
        	blocks_occupied_by_black -= Integer.bitCount(this.black_bitboard[i] & Utils.blocks_bitboard[i]);
        }
        blocks_cond = lut[2][blocks_occupied_by_black];
        
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

        remaining_whites_cond =  lut[0][white_cnt];
        remaining_blacks_cond = lut[1][black_cnt];

        ak_cond = lut[3][this.open_king_paths()];
        return  remaining_whites_cond + remaining_blacks_cond + ak_cond + blocks_cond;
	}
	
	@Override
	public BitState produceState(List<Integer> action) {
		return new BitStateWhitePlayer(this, action);
	}
}
