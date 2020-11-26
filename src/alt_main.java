/*import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import thor.BitState;
import thor.Game;
import thor.Minmax;

public class alt_main {

	public static void main(String[] args) throws IOException {
		BitState bs = new BitState();
		int[] white_bitboard = {2, 0, 0, 0, 12, 2, 80, 0, 0};
		int[] black_bitboard = {17, 67, 36, 8, 288, 65, 2, 4, 48};
		int[] king_bitboard = {0, 0, 0, 4, 0, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 0, 0, 40, 20, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(20, bs, true);
		System.out.println("case: game, action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState bs_1 = new BitState(bs,action);
		action = Arrays.asList(1,4,4,2,4);
		BitState bs_2 = new BitState(bs_1, action);
		System.out.println("last position h =" + bs_1.compute_heuristic(w, c));
		

	}

}*/
