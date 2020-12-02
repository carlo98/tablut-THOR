package thor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class BitState{
	private int[] white_bitboard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	private int[] black_bitboard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	private int[] king_bitboard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	private Turn turn = null;
	private int[][] lut_white = {{-Utils.MAX_VAL_HEURISTIC, 50, 100, 150, 200, 250, 340, 390, 420, 450},  // Remaining white
			{Utils.MAX_VAL_HEURISTIC, -30, -60, -90, -120, -150, -180, -210, -240, -270, -300, -330, -400, -420, -440, -460, -480},  // Remaining black
			{-600, -200, -100, -30, -20, 40, 50, 80, 100},  // Open diagonal blocks
			{0, 200, 500, 1000, Utils.MAX_VAL_HEURISTIC},  // Aggressive king, number of path open to escapes
            {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 115, 120, 125, 130, 135, 140, 145, 150, 155, 160, 165, 170, 175, 180, 185, 190, 195}  // Blocks occupied by white
			};
	
	private int[][] lut_black = {{-80, -70, -60, -50, -40, -30,-20, -10,0} //wings in camps
	};
	
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
		return white_bitboard.clone();
	}

	int[] getBlack_bitboard() {
		return black_bitboard.clone();
	}

	int[] getKing_bitboard() {
		return king_bitboard.clone();
	}

	Turn getTurn() {
		return turn;
	}
	
	//used for testing
	public BitState() { 
	}
	
	public void setWhite_bitboard(int[] white_bitboard) {
		this.white_bitboard = white_bitboard;
	}

	public void setBlack_bitboard(int[] black_bitboard) {
		this.black_bitboard = black_bitboard;
	}

	public void setKing_bitboard(int[] king_bitboard) {
		this.king_bitboard = king_bitboard;
	}

	public void setTurn(Turn turn) {
		this.turn = turn;
	}

	public BitState(BitState s, List<Integer> action) {
		
		int k = action.get(0);
		int start_row = action.get(1);
		int start_col = action.get(2);
		int end_row = action.get(3);
		int end_col = action.get(4);
		int[] tmp_bitboard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
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
	        }
	        for(int i = 0; i < this.king_bitboard.length; i++) {
        		tmp_bitboard[i] = this.white_bitboard[i] + this.king_bitboard[i];
	        }
        	this.black_bitboard = Utils.white_tries_capture_black_pawn(tmp_bitboard, this.black_bitboard, end_row, end_col);
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
        if (!Arrays.stream(this.king_bitboard).anyMatch(i -> i != 0)) {
            return -1;
        }
        for(int i = 0; i < this.king_bitboard.length; i++) {
    		tmp_bitboard[i] = (Utils.escapes_bitboard[i] & this.king_bitboard[i]);
        }
        if (Arrays.stream(tmp_bitboard).anyMatch(i -> i != 0)) {
            return 1;
        }
        return 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(black_bitboard);
		result = prime * result + Arrays.hashCode(king_bitboard);
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
		if (!Arrays.equals(white_bitboard, other.white_bitboard))
			return false;
		return true;
	}

	public double compute_heuristic(String color) {
		int color_mult=1;
		int white_cnt = 1, black_cnt = 0;
        int curr_mask, remaining_whites_cond, remaining_blacks_cond, ak_cond, blocks_cond_black, blocks_cond_white, blocks_open=8, blocks_occupied=0;
        int wings_cond;
        int victory_cond = this.check_victory();
        int h_partial;
        
        if (color.equalsIgnoreCase("WHITE")) {
        	color_mult = 1;
        } else {
        	color_mult = -1;
        }
        
        if (victory_cond == -1)  // King captured
            return color_mult*(-Utils.MAX_VAL_HEURISTIC);
        else if (victory_cond == 1)  // King escaped
            return color_mult*(Utils.MAX_VAL_HEURISTIC);
        
        
        for(int i = 0; i < this.black_bitboard.length; i++) {
        	blocks_open -= Integer.bitCount(this.black_bitboard[i] & Utils.blocks_bitboard[i]);
        }
        blocks_cond_black = lut_white[2][blocks_open];

        for(int i = 0; i < this.white_bitboard.length; i++) {
        	blocks_occupied += Integer.bitCount(this.white_bitboard[i] & Utils.blocksWhite_bitboard[i]);
        }
        blocks_cond_white = lut_white[4][blocks_occupied];
        
        /*remaining pieces*/
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

        remaining_whites_cond =  lut_white[0][white_cnt];
        remaining_blacks_cond = lut_white[1][black_cnt];
        
        /*aggressive king*/
        ak_cond = lut_white[3][this.open_king_paths()];
        
        /*camp wings*/
        wings_cond = lut_black[0][locked_wings()];
        
        /*partial heuristic*/
        h_partial = remaining_whites_cond + remaining_blacks_cond + ak_cond + blocks_cond_black + 
        		blocks_cond_white +wings_cond;
        
        
        return color_mult * h_partial;
	}
	
	int locked_wings() {
		int locked_wings = 0;
		for(int i = 0; i < this.black_bitboard.length; i++) {
        	locked_wings += Integer.bitCount(this.black_bitboard[i] & Utils.wings_bitboard[i]);
        }
		return locked_wings;
	}
	
	int open_king_paths() {
        //king coordinates
		int king_row, king_col, king_bin_col, left_mask, open_paths;
		int right_mask = 511;
		List<Integer> above_the_column = new ArrayList<>();
		List<Integer> below_the_column = new ArrayList<>();
		for(king_row = 0; king_row < 9; king_row++)
			if(this.king_bitboard[king_row] != 0) {
				break;
			}
		king_bin_col = this.king_bitboard[king_row];
	    king_col = Utils.lut_positions.get(king_bin_col);

        //check for pawns/camps left and right
        left_mask = (king_bin_col << 1);
        for (int col = 0; col <= king_col; col++) {
            right_mask >>= 1;
            if (col <= king_col - 2){
                left_mask ^= king_bin_col;
                left_mask <<= 1;
            }
        }

        //check for pawns/camps up and down
        for (int row = 0; row <= king_col; row++) {
            if (row != king_row && row < king_row) {
                above_the_column.addAll(Arrays.stream(Utils.bit(Utils.camps_bitboard[row])).boxed().collect(Collectors.toList()));
                above_the_column.addAll(Arrays.stream(Utils.bit(this.white_bitboard[row])).boxed().collect(Collectors.toList()));
                above_the_column.addAll(Arrays.stream(Utils.bit(this.black_bitboard[row])).boxed().collect(Collectors.toList()));
            }
            else if (row != king_row && row > king_row) {
            	below_the_column.addAll(Arrays.stream(Utils.bit(Utils.camps_bitboard[row])).boxed().collect(Collectors.toList()));
            	below_the_column.addAll(Arrays.stream(Utils.bit(this.white_bitboard[row])).boxed().collect(Collectors.toList()));
            	below_the_column.addAll(Arrays.stream(Utils.bit(this.black_bitboard[row])).boxed().collect(Collectors.toList()));
            }
        }
        open_paths = 4;

        if ((king_row == 3 || king_row == 4 || king_row == 5) ||
                ((right_mask & this.white_bitboard[king_row]) + (right_mask & this.black_bitboard[king_row])
                 + (right_mask & Utils.camps_bitboard[king_row]) != 0)) {
            open_paths -= 1;
        }
        if ((king_row == 3 || king_row == 4 || king_row == 5) ||
                (left_mask & this.white_bitboard[king_row]) + (left_mask & this.black_bitboard[king_row])
                + (left_mask & Utils.camps_bitboard[king_row]) != 0) {
            open_paths -= 1;
        }
        if ((king_row == 3 || king_row == 4 || king_row == 5) || above_the_column.contains(king_bin_col)) {
            open_paths -= 1;
        }
        if ((king_row == 3 || king_row == 4 || king_row == 5) || below_the_column.contains(king_bin_col)) {
            open_paths -= 1;
        }
        return open_paths;
	}
	
	int locked_back_camps() {
        int locked_camps = 0;
        int hor_map = 0b000111000;
        if ((this.black_bitboard[0] & hor_map) == 0b000111000) {
            locked_camps += 1;
        }
        if ((this.black_bitboard[8] & hor_map) == 0b000111000) {
            locked_camps += 1;
        }
        List<Integer> row4 = Arrays.stream(Utils.bit(this.black_bitboard[3])).boxed().collect(Collectors.toList());
        List<Integer> row5 = Arrays.stream(Utils.bit(this.black_bitboard[4])).boxed().collect(Collectors.toList());
        List<Integer> row6 = Arrays.stream(Utils.bit(this.black_bitboard[5])).boxed().collect(Collectors.toList());
        if (row4.contains(256) && row5.contains(256) && row6.contains(256)) {
            locked_camps += 1;
        }
        if (row4.contains(2) && row5.contains(2) && row6.contains(2)) {
            locked_camps += 1;
        }
        return locked_camps;
	}
}
