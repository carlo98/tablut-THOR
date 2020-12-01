package thor;

import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class BitStateWhitePlayer extends BitState {
	
	private int[][] lut = {{-Utils.MAX_VAL_HEURISTIC, 5, 10, 30, 90, 190, 270, 350, 400, 450},  // Remaining white
			{Utils.MAX_VAL_HEURISTIC, -5, -10, -20, -30, -50, -70, -100, -160, -220, -280, -340, -400, -460, -520, -580, -640},  // Remaining black
			{-600, -200, -100, -30, -20, 40, 50, 80, 100},  // Open diagonal blocks
			{0, 200, 500, 1000, 4000},  // Aggressive king, number of path open to escapes
                                        {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120}  // Blocks occupied by white
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
        int curr_mask, remaining_whites_cond, remaining_blacks_cond, ak_cond, blocks_cond_black, blocks_cond_white, blocks_open=8, blocks_occupied=0;
        int victory_cond = this.check_victory();
        
        if (victory_cond == -1)  // King captured
            return -Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1)  // King escaped
            return Utils.MAX_VAL_HEURISTIC;
        
        for(int i = 0; i < this.black_bitboard.length; i++) {
        	blocks_open -= Integer.bitCount(this.black_bitboard[i] & Utils.blocks_bitboard[i]);
        }
        blocks_cond_black = lut[2][blocks_open];

        for(int i = 0; i < this.white_bitboard.length; i++) {
        	blocks_occupied += Integer.bitCount(this.white_bitboard[i] & Utils.blocks_bitboard[i]);
        }
        blocks_cond_white = lut[4][blocks_occupied];
        
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
        return  remaining_whites_cond + remaining_blacks_cond + ak_cond + blocks_cond_black + 2*blocks_cond_white;
	}
	
	@Override
	public BitState produceState(List<Integer> action) {
		return new BitStateWhitePlayer(this, action);
	}
}
