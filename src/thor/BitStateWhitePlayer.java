package thor;

import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class BitStateWhitePlayer extends BitState {
	
	private int[][] lut = {{-Utils.MAX_VAL_HEURISTIC, 5, 10, 30, 90, 190, 270, 350, 400, 450},  // Remaining white
							{Utils.MAX_VAL_HEURISTIC, -5, -10, -30, -90, -150, -170, -190, -220, -240, -260, -280, -300, -320, -340, -360, -540},  // Remaining black
							{-600, -200, -100, -30, -20, 40, 50, 80, 100},  // Open diagonal blocks
							{0, 200, 400, 1000, 4000}  // Aggressive king, number of path open to escapes
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
	
	public BitStateWhitePlayer(BitState s) {
		super(s, 0);
	}
	
	@Override
	public double compute_heuristic() {
		int white_cnt = 1, black_cnt = 0;
        int curr_mask, remaining_whites_cond, remaining_blacks_cond, ak_cond, blocks_cond, blocks_open=8;
        int victory_cond = this.check_victory();
        
        if (victory_cond == -1)  // King captured
            return -Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1)  // King escaped
            return Utils.MAX_VAL_HEURISTIC;
        
        for(int i = 0; i < this.black_bitboard.length; i++) {
        	blocks_open -= Integer.bitCount(this.black_bitboard[i] & Utils.blocks_bitboard[i]);
        }
        blocks_cond = lut[2][blocks_open];
        
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
