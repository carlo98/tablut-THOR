package thor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

class king_escape_test {

	@BeforeEach
	void setUp() throws Exception {
	}

@Test 
	
	void escape_king_up() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {256, 0, 72, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {0, 4, 16, 0, 0, 0, 0, 0, 0};
		int[] king_bitboard = {0, 0, 0, 128, 0, 0, 0, 0, 0};
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
		
		int[] k_bitboard_expected = {128, 0, 0, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
	@Test
	void escape_king_right_left() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {32, 0, 0, 68, 112, 16, 16, 0, 0};
		int[] black_bitboard = {56, 4, 0, 257, 385, 257, 0, 4, 33};
		int[] king_bitboard = {0, 0, 0, 0, 16, 0, 0, 0, 0};
		bs.setBlack_bitboard(black_bitboard);
		bs.setWhite_bitboard(white_bitboard);
		bs.setKing_bitboard(king_bitboard);
		bs.setTurn(Turn.WHITE);
		String c = "WHITE";
		Game game = new Game(c);
		Minmax minmax = new Minmax(game);
		minmax.setMax_depth(3);
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		action = minmax.makeDecision(10, bs, true);
		System.out.println("Right_left value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 16, 0, 0, 0, 0, 0, 0};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}

	@Test
	void escape_king_down() throws IOException {
		BitState bs = new BitStateWhitePlayer();
		int[] white_bitboard = {256, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] black_bitboard = {64, 32, 0, 0, 0, 0, 257, 0, 160};
		int[] king_bitboard = {0, 0, 0, 0, 0, 0, 64, 0, 0};
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
		System.out.println("Down value:"+ minmax.getBest_value());
		System.out.println("action = " + action.get(0) + " "+ action.get(1) + " "+ action.get(2) + " "
				+ action.get(3) + " " + action.get(4));
		BitState new_bs = bs.produceState(action);
		
		int[] k_bitboard_expected = {0, 0, 0, 0, 0, 0, 0, 0, 64};
		assertArrayEquals(k_bitboard_expected,new_bs.getKing_bitboard());
	}
}
