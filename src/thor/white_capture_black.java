package thor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class white_capture_black {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test 
	void capture_up_camp_pawns() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {256, 8, 0, 16, 40, 16, 0, 0, 0};
		int[] black_bitboard = {0, 16, 16, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		String c = "WHITE";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(1);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("Up value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] b_bitboard_expected = {0, 0, 16, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(b_bitboard_expected,new_bs.getBlack_bitboard());
	}

	@Test
	void capture_up_pawns() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {256, 0, 8, 16, 40, 16, 0, 0, 0};
		int[] black_bitboard = {0, 16, 16, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		String c = "WHITE";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(1);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("Up value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] b_bitboard_expected = {0, 16, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(b_bitboard_expected,new_bs.getBlack_bitboard());
	}

	@Test
	void capture_up_king() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {64, 0, 4, 68, 64, 16, 16, 0, 0};
		int[] black_bitboard = {0, 0, 8, 0, 0, 0, 0, 0, 33};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		String c = "WHITE";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(1);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("King capture up value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] b_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 33};
		assertArrayEquals(b_bitboard_expected,new_bs.getBlack_bitboard());
	}

	@Test
	void capture_pawn_camp() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {256, 128, 0, 16, 0, 16, 0, 0, 0};
		int[] black_bitboard = {128, 32, 0, 0, 0, 0, 257, 0, 160};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		String c = "WHITE";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(1);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("Pawn camp value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] b_bitboard_expected = {128, 0, 0, 0, 0, 0, 257, 0, 160};
		assertArrayEquals(b_bitboard_expected,new_bs.getBlack_bitboard());
	}
	
	@Test
	void capture_pawn_throne() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {0, 64, 0, 16, 0, 16, 0, 0, 0};
		int[] black_bitboard = {0, 0, 16, 0, 32, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		String c = "WHITE";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(1);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("Pawn throne value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] b_bitboard_expected = {0, 0, 16, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(b_bitboard_expected,new_bs.getBlack_bitboard());
	}
	
	@Test
	void capture_right_pawn() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {0, 0, 18, 16, 0, 16, 0, 0, 0};
		int[] black_bitboard = {0, 0, 4, 0, 32, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		String c = "WHITE";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(1);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("Pawn right value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] b_bitboard_expected = {0, 0, 0, 0, 32, 0, 0, 0, 0};
		assertArrayEquals(b_bitboard_expected,new_bs.getBlack_bitboard());
	}
	
	@Test
	void capture_left_camp() throws IOException {
		BitState bs = new BitStateBlackPlayer();
		int[] white_bitboard = {0, 0, 0, 0, 0, 128, 16, 32, 0};
		int[] black_bitboard = {32, 0, 272, 80, 131, 1, 72, 0, 56};
		int[] king_bitboard = {0, 0, 0, 0, 0, 0, 32, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		String c = "BLACK";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(1);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("Left camp value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] w_bitboard_expected = {0, 0, 0, 0, 0, 0, 16, 32, 0};
		assertArrayEquals(w_bitboard_expected,new_bs.getWhite_bitboard());
	}
}
