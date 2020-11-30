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
		BitStateBlackPlayer bs = new BitStateBlackPlayer();
		int[] white_bitboard = {0, 0, 16, 16, 108, 16, 16, 0, 0};
		int[] black_bitboard = {56, 16, 0, 257, 387, 257, 0, 16, 56};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		//int[] white_bitboard = {0, 0, 16, 0, 0, 16, 0, 0, 0};
		//int[] black_bitboard = {56, 16, 0, 257, 387, 257, 0, 16, 56};
		//int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		System.out.println(bs.locked_wings());
		/*
		String c = "BLACK";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("case: game, action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState bs_1 = bs.produceState(action);
		System.out.println("last position h =" + bs_1.compute_heuristic());
		*/

	}

}
