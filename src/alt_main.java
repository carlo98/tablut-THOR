import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import thor.BitState;
import thor.BitStateBlackPlayer;
import thor.Game;
import thor.Minmax;

public class alt_main {

	public static void main(String[] args) throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {0, 0, 16, 16, 108, 16, 16, 0, 0};
		int[] black_bitboard = {56, 16, 0, 257, 387, 257, 0, 16, 56};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		//int[] white_bitboard = {0, 0, 128, 16, 108, 16, 16, 0, 0};
		//int[] black_bitboard = {56, 16, 0, 257, 387, 257, 0, 16, 56};
		//int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		
		System.out.println(bs.compute_heuristic());
		List<Integer> action =  Arrays.asList(0,2,4,2,1);
		BitState bs1 = bs.produceState(action);
		System.out.println(bs1.compute_heuristic());
		System.out.println(bs1.getTurn().toString());
		action = Arrays.asList(0,0,3,0,2);
		BitStateBlackPlayer bs2 = (BitStateBlackPlayer) bs1.produceState(action);
		System.out.println(bs2.compute_heuristic());
		

	}

}
