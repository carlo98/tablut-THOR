package it.unibo.ai.didattica.competition.tablut.thor;

import java.util.Arrays;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class BitState{
	private int[] white_bitboard = new int[9];
	private int[] black_bitboard = new int[9];
	private int[] king_bitboard = new int[9];
	private Turn turn = null;

	public BitState(State state) {
		this.turn = state.getTurn();
	    for (int row = 0; row < state.getBoard().length; row++) {
	    	for (int col = 0; col < state.getBoard().length; col++) {
	    		this.white_bitboard[row] <<= 1;
	    		this.black_bitboard[row] <<= 1;
	    		this.king_bitboard[row] <<= 1;
	            if (state.getPawn(row, col).equalsPawn(Pawn.WHITE.toString()))
	            	this.white_bitboard[row] ^= 1;
	            else if (state.getPawn(row, col).equalsPawn(Pawn.BLACK.toString()))
	            	this.black_bitboard[row] ^= 1;
	            else if (state.getPawn(row, col).equalsPawn(Pawn.KING.toString()))
	            	this.king_bitboard[row] ^= 1;
	    	}
	    }
	}
	
	int[] getWhite_bitboard() {
		return white_bitboard;
	}

	int[] getBlack_bitboard() {
		return black_bitboard;
	}

	int[] getKing_bitboard() {
		return king_bitboard;
	}

	Turn getTurn() {
		return turn;
	}

	public BitState(BitState s, List<Integer> action) {
		int k = action.get(0);
		int start_row = action.get(1);
		int start_col = action.get(2);
		int end_row = action.get(3);
		int end_col = action.get(4);
		int[] tmp_bitboard = new int[9];
		this.white_bitboard = s.getWhite_bitboard();
		this.black_bitboard = s.getBlack_bitboard();
		this.king_bitboard = s.getKing_bitboard();

	    if (s.turn == Turn.WHITE) {
	    	this.turn = Turn.BLACK;
	        if (k == 0) {
	        	this.white_bitboard[start_row] -= (1 << (8 - start_col));
	        	this.white_bitboard[end_row] += (1 << (8 - end_col));
	        }
	        else {
	        	this.king_bitboard[start_row] -= (1 << (8 - start_col));
	        	this.king_bitboard[end_row] += (1 << (8 - end_col));
	        	for(int i = 0; i < this.king_bitboard.length; i++)
	        		tmp_bitboard[i] = this.white_bitboard[i] + this.king_bitboard[i];
	        	this.black_bitboard = Utils.white_tries_capture_black_pawn(tmp_bitboard, this.black_bitboard, end_row, end_col);
	        }
	    }
	    else {
	    	this.turn = Turn.WHITE;
	    	this.black_bitboard[start_row] -= (1 << (8 - start_col));
	    	this.black_bitboard[end_row] += (1 << (8 - end_col));
	    	this.white_bitboard = Utils.black_tries_capture_white_pawn(this.black_bitboard, this.white_bitboard, end_row, end_col);
	    	this.king_bitboard = Utils.black_tries_capture_king(this.black_bitboard, this.king_bitboard, end_row, end_col);
	    }
	}
	
	public int check_victory() {
		int[] tmp_bitboard = new int[9];
        if (!Arrays.stream(this.king_bitboard).anyMatch(i -> i == 1))
            return -1;
        for(int i = 0; i < this.king_bitboard.length; i++)
    		tmp_bitboard[i] = Utils.escapes_bitboard[i] & this.king_bitboard[i];
        if (Arrays.stream(tmp_bitboard).anyMatch(i -> i == 1))
            return 1;
        return 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(black_bitboard);
		result = prime * result + Arrays.hashCode(king_bitboard);
		result = prime * result + ((turn == null) ? 0 : turn.hashCode());
		result = prime * result + Arrays.hashCode(white_bitboard);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitState other = (BitState) obj;
		if (!Arrays.equals(black_bitboard, other.black_bitboard))
			return false;
		if (!Arrays.equals(king_bitboard, other.king_bitboard))
			return false;
		if (turn != other.turn)
			return false;
		if (!Arrays.equals(white_bitboard, other.white_bitboard))
			return false;
		return true;
	}

	public double compute_heuristic(int[] weights, String color) {
		int[] tmp_bitboard = new int[9];
        int victory_cond = this.check_victory();
        if (victory_cond == -1 && color == "BLACK")  // king captured and black player -> Win
            return Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == -1 && color == "WHITE")  // King captured and white player -> Lose
            return -Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1 && color == "BLACK")  // King escaped and black player -> Lose
            return -Utils.MAX_VAL_HEURISTIC;
        else if (victory_cond == 1 && color == "WHITE")  // King escaped and white player -> Win
            return Utils.MAX_VAL_HEURISTIC;
        
        for(int i = 0; i < this.king_bitboard.length; i++)
    		tmp_bitboard[i] = this.black_bitboard[i] & Utils.blocks_bitboard[i];
        int blocks_occupied_by_black = 
        for(int i = 0; i < this.king_bitboard.length; i++)
    		tmp_bitboard[i] = Utils.escapes_bitboard[i] & this.king_bitboard[i];
        int blocks_occupied_by_white = count_nonzero(self.white_bitboard & blocks_bitboard) + np.count_nonzero(self.king_bitboard & blocks_bitboard);
        coeff_min_black = (-1) ** (color == "WHITE")
        coeff_min_white = (-1) ** (color == "BLACK")
        blocks_cond = coeff_min_black * weights[0] * blocks_occupied_by_black \
                      + coeff_min_white * weights[1] * blocks_occupied_by_white
        open_blocks_cond = coeff_min_white * weights[2] * (8 - blocks_occupied_by_white - blocks_occupied_by_black)
        "remaining pieces are considered"
        white_cnt = 0
        black_cnt = 0
        for r in range(0, 9):
            for c in range(0, 9):
                curr_mask = 1 << (8 - c)
                if self.white_bitboard[r] & curr_mask != 0:
                    white_cnt += 1
                if self.black_bitboard[r] & curr_mask != 0:
                    black_cnt += 1

        remaining_whites_cond = coeff_min_white * weights[3] * white_cnt
        remaining_blacks_cond = coeff_min_black * weights[4] * black_cnt

        "aggressive king condition"
        ak_cond = coeff_min_white * weights[5] * self.open_king_paths()
        h = blocks_cond + remaining_whites_cond + remaining_blacks_cond + open_blocks_cond + ak_cond
        return h
	}
}
