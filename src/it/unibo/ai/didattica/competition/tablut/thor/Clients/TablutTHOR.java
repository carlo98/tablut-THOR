package it.unibo.ai.didattica.competition.tablut.thor.Clients;

import it.unibo.ai.didattica.competition.tablut.thor.BitState;
import it.unibo.ai.didattica.competition.tablut.thor.Game;
import it.unibo.ai.didattica.competition.tablut.thor.Minmax;
import it.unibo.ai.didattica.competition.tablut.thor.StateDictEntry;
import it.unibo.ai.didattica.competition.tablut.thor.Utils;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.io.IOException;
import java.net.UnknownHostException;
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
		Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table= new Hashtable<Integer, Hashtable<Integer, StateDictEntry>>();
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

					action = Utils.action_to_server_format(minmax.makeDecision(max_time, new BitState(this.getCurrentState().clone()), game));
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
