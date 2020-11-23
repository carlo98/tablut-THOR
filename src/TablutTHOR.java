import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import thor.BitState;
import thor.Game;
import thor.Minmax;
import thor.StateDictEntry;
import thor.Utils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

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
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		List<Integer> best_action = action.stream().collect(Collectors.toList());
		Game game = new Game(this.weights, this.color);
		Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table= new Hashtable<Integer, Hashtable<Integer, StateDictEntry>>();
		for (int i=0; i < 25; i++)
			state_hash_table.put(i, new Hashtable<Integer, StateDictEntry>());
		Minmax minmax = new Minmax(state_hash_table, game);
		BitState bitState = null;

		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {

			try{
				this.read();
				bitState = new BitState(this.getCurrentState().clone());
				minmax.updateState_hash_table(bitState);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (this.getCurrentState().getTurn().toString().equalsIgnoreCase(String.valueOf(this.color.charAt(0)))) {
					while (action != null) {
						action = minmax.makeDecision(max_time, bitState, game);
						minmax.setMax_depth(minmax.getMax_depth() + 1);
						if (action != null) 
							best_action = action.stream().collect(Collectors.toList());
					}
					this.write(Utils.action_to_server_format(best_action));
				} else {
					System.out.println("Not my turn.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
