package thor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class king_capture_tests {

	@BeforeEach
	void setUp() throws Exception {
	}

@Test 
	
	void capture_king_right_camp() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {256, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 4, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 2, 0, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 10, 0, 40, 30, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
	@Test
	void capture_king_below_camp() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {256, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 2, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 2, 0, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 10, 0, 40, 30, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}

	@Test
	void capture_king_left_camp() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {256, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 32, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 64, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 10, 0, 40, 30, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
	
	@Test
	void capture_king_above_camp() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {256, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 0, 1, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 32, 0, 0, 0, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 10, 0, 40, 30, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
	
	@Test
	void capture_king_castle_with_alternative() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {0, 8, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 0, 0, 16, 32, 16, 0, 8, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 10, 0, 40, 30, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("case: castle, action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
	
	@Test
	void capture_king_next_to_castle() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {0, 8, 0, 0, 0, 18, 0, 0, 0};
		int[] black_bitboard = {0, 0, 0, 8, 4, 0, 0, 8, 0};
		int[] king_bitboard = {0, 0, 0, 0, 8, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 10, 0, 40, 30, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("case: castle, action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
	
	@Test
	void game_position() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {0, 32, 0, 68, 96, 16, 16, 0, 0};
		int[] black_bitboard = {56, 4, 0, 257, 385, 257, 0, 4, 33};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		int[] w = {20, 0, 0, 40, 30, 50};
		String c = "BLACK";
		Game game = new Game(w, c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(20, bs, true);
		System.out.println("case: game, action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
}
