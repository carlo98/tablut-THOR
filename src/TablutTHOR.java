import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import thor.BitState;
import thor.Game;
import thor.Minmax;
import thor.Utils;

/**
 * 
 * @author Carlo Cena, Giacomo Zamprogno
 * @implNote Class that performs comunications with server
 *
 */
public class TablutTHOR extends TablutClient {

	private final int max_time;
	private String color;
	private static final String PLAYER_NAME = "THOR";

	/**
	 * @param color String, player color
	 * @param max_time int, max time available to choose actions, in seconds
	 * @param host String, server ip
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TablutTHOR(String color, int max_time, String host) throws UnknownHostException, IOException {
		super(color, PLAYER_NAME, max_time, host);
		this.max_time = max_time - 2;
		this.color = color;
	}

	/*** main ***/
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		TablutClient client = new TablutTHOR(args[0].toLowerCase(), Math.round(Float.parseFloat(args[1])), args[2]);
		client.run();
	}

	@Override
	public void run() {
		System.out.println("Player " + this.getPlayer().toString());
		List<Integer> action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
		List<Integer> best_action = action.stream().collect(Collectors.toList());
		Game game = new Game(this.color);
		Minmax minmax = new Minmax(game);
		BitState bitState = null;
		long start_time;
		Boolean flag = true;

		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {

			try {
				this.read();
				flag = true;
				bitState = new BitState(this.getCurrentState().clone());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			try {
				if (this.getCurrentState().getTurn().toString()
						.equalsIgnoreCase(String.valueOf(this.color.charAt(0)))) {
					start_time = System.currentTimeMillis();
					while (action != null) {
						action = minmax.makeDecision(this.max_time - (System.currentTimeMillis() - start_time) / 1000,
								bitState, flag);
						minmax.setMax_depth(minmax.getMax_depth() + 1);
						flag = false;
						if (action != null) {
							best_action = action.stream().collect(Collectors.toList());
							if (minmax.getBest_value() == Utils.MAX_VAL_HEURISTIC) {
								break;
							}
						}
					}
					this.write(Utils.action_to_server_format(best_action));
				} else {
					System.out.println("Not my turn.");
					best_action = null;
					action = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
					minmax.setMax_depth(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
