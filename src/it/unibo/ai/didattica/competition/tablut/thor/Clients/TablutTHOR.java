package it.unibo.ai.didattica.competition.tablut.thor.Clients;

import it.unibo.ai.didattica.competition.tablut.thor.Game;
import it.unibo.ai.didattica.competition.tablut.thor.Minmax;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Hashtable;

/**
 * 
 * @author Carlo Cena, Giacomo Zamprogno
 *
 */
public class TablutTHOR extends TablutClient {

	private final int max_time;
	private String color;
	private static final String PLAYER_NAME = "THOR";
	private int[] weights = {1, 1, 1, 1, 1, 1};

	
	public TablutTHOR(String color, int max_time, String host) throws UnknownHostException, IOException {
		super(color, PLAYER_NAME , max_time, host);
		this.max_time = max_time;
		this.color = color;
	}


	/***main***/
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		TablutClient client = new TablutTHOR(args[0].toLowerCase(), Math.round(Float.parseFloat(args[1])) - 1, args[2]);
		client.run();
	}

	@Override
	public void run() {
		System.out.println("Player " + this.getPlayer().toString());
		Action action;
		Game game = new Game(this.weights, this.color);
		Hashtable<Integer, Hashtable<Integer, Float>> state_hash_table= new Hashtable<Integer, Hashtable<Integer, Float>>();
		Minmax minmax = new Minmax(state_hash_table, game);

		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {

			try{
				this.read();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (this.getCurrentState().getTurn().toString().equals(this.color)) {

					action = minmax.makeDecision(max_time, this.getCurrentState().clone(), game);
					this.write(action);

				} else {
					System.out.println("Game ended.");
					System.exit(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
