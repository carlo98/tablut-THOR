package tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import thor.BitState;
import thor.Game;
import thor.Minmax;

class bitstate_test {

	@BeforeEach
	void setUp() throws Exception {
		
	}

	@Test

	void capture_king_left_camp() throws IOException {
		BitState bs = new BitState();
		int[] white_bitboard = { 256, 0, 0, 0, 0, 0, 0, 0, 0 };
		int[] black_bitboard = { 0, 4, 0, 0, 0, 0, 0, 0, 0 };
		int[] king_bitboard = { 0, 0, 0, 2, 0, 0, 0, 0, 0 };
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		String c = "BLACK";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("action = " + action.get(0) + " " + action.get(1) + " " + action.get(2) + " " + action.get(3)
				+ " " + action.get(4));
		BitState new_bs = new BitState(bs, action);

		int[] k_bitboard_expected = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		assertArrayEquals(k_bitboard_expected, new_bs.getKing_bitboard());
	}

	@Test
	void capture_king_below_camp() throws IOException {
		BitState bs = new BitState();
		int[] white_bitboard = { 256, 0, 0, 0, 0, 0, 0, 0, 0 };
		int[] black_bitboard = { 0, 2, 0, 0, 0, 0, 0, 0, 0 };
		int[] king_bitboard = { 0, 0, 0, 2, 0, 0, 0, 0, 0 };
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		String c = "BLACK";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("action = " + action.get(0) + " " + action.get(1) + " " + action.get(2) + " " + action.get(3)
				+ " " + action.get(4));
		BitState new_bs = new BitState(bs, action);

		int[] k_bitboard_expected = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		assertArrayEquals(k_bitboard_expected, new_bs.getKing_bitboard());
	}
	
	/*
	@Test
	
	void white_capture_left_with_camp() {
		BitState bs = new BitState();
		int[] white_bitboard = {0, 2, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {5, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		System.out.println(bs.getBlack_bitboard()[0]);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		List<Integer> action = Arrays.asList(0,1,7,0,7);
		BitState new_bs = new BitState(bs,action);
		System.out.println(new_bs.getBlack_bitboard()[0]);
		int[] black_bitboard_expected = {1, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(black_bitboard_expected,new_bs.getBlack_bitboard());
	}

	@Test
	void white_capture_left() {
		BitState bs = new BitState();
		int[] white_bitboard = {0, 4, 1, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 2, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		List<Integer> action = Arrays.asList(0,2,8,1,8);
		BitState new_bs = new BitState(bs,action);
		int[] black_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(black_bitboard_expected,new_bs.getBlack_bitboard());
	}
	
	@Test
	void white_capture_up() {
		BitState bs = new BitState();
		int[] white_bitboard = {2, 0, 1, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 2, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		List<Integer> action = Arrays.asList(0,2,8,2,7);
		BitState new_bs = new BitState(bs,action);
		int[] black_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(black_bitboard_expected,new_bs.getBlack_bitboard());
	}
	
	@Test
	void white_capture_down() {
		BitState bs = new BitState();
		int[] white_bitboard = {4, 0, 2, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 2, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		List<Integer> action = Arrays.asList(0,0,6,0,7);
		BitState new_bs = new BitState(bs,action);
		int[] black_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(black_bitboard_expected,new_bs.getBlack_bitboard());
	}
	
	@Test
	void white_capture_double() {
		BitState bs = new BitState();
		int[] white_bitboard = {0, 0, 17, 4, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 0, 10, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 0, 0, 0, 4, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		List<Integer> action = Arrays.asList(0,3,6,2,6);
		BitState new_bs = new BitState(bs,action);
		int[] black_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(black_bitboard_expected,new_bs.getBlack_bitboard());
	}
	@Test
	void black_capture_right() {
		BitState bs = new BitState();
		int[] white_bitboard = {2, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {1, 4, 0, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.BLACK);
		List<Integer> action = Arrays.asList(0,1,6,0,6);
		BitState new_bs = new BitState(bs,action);
		int[] white_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		assertTrue(Arrays.equals(new_bs.getWhite_bitboard(), white_bitboard_expected));
	}
	*/
}
