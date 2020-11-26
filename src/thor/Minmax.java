package thor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 * @author Carlo Cena, Giacomo Zamprogno
 *
 */

public final class Minmax {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, StateDictEntry>> state_hash_table;
    private int max_depth;
    private final Game game;
    private BitState currentState;
    private List<List<Integer>> all_actions;
    private List<Future<Double>> choosen_action;

    public Minmax(Game game) {
    	this.state_hash_table= new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, StateDictEntry>>();
		for (int i=0; i < 25; i++)
			this.state_hash_table.put(i, new ConcurrentHashMap<Integer, StateDictEntry>());
        this.game = game;
        this.max_depth = 3;
    }
    
    private class MinMax_thread implements Callable<Double> {
    	private List<Integer> action;
    	private double alpha;
    	
    	public MinMax_thread(List<Integer> action, double alpha) {
			super();
			this.action = action;
			this.alpha = alpha;
		}

		@Override
        public Double call() throws Exception {
			//print for testing
			
            double value = minValue(new BitState(currentState, this.action), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, 
                		max_depth, state_hash_table);
            /*System.out.println("Analizing action = "+ action.get(1) + " "+ action.get(2) + " "
					+ action.get(3) + " " + action.get(4) + "\t result = "+ value);*/
            return value;
        }
    }
    
    public List<Integer> makeDecision(long max_time, BitState state, Boolean flag) throws IOException {
        long start_time = System.currentTimeMillis()/1000;
        List<Integer> to_be_returned = Arrays.asList(0, 0, 0, 0, 0);
        int index_best = 0;
        int cont = 0;
    	if (flag) {
	    	this.updateState_hash_table(state);
	    	this.currentState = state;
	    	this.all_actions = this.game.produce_actions(currentState);
    	}
    	int step = Runtime.getRuntime().availableProcessors() + 10;
    	this.choosen_action = new ArrayList<>(all_actions.size());
        double[] values = new double[all_actions.size()];
        for(int i = 0; i< values.length; i++)
        	values[i] = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < all_actions.size(); i++) {
        	choosen_action.add(executorService.submit(new MinMax_thread(all_actions.get(i), values[index_best])));
        	
        	cont += 1;
        	if (((i%step == 0) && i != 0) || i==all_actions.size()-1) {
	        	for (int k=i-cont+1;k<=i;k++) {
	        		cont = 0;
			           try {
			            if(max_time-(System.currentTimeMillis()/1000 - start_time) < 0) {
			            	to_be_returned = null;
			            	break;
			            }
			            values[k] = choosen_action.get(k).get(max_time-(System.currentTimeMillis()/1000 - start_time), TimeUnit.SECONDS);
			            if (values[k] > values[index_best]) {
			            	index_best = k;
			            }
			        } catch (TimeoutException e) {
			            to_be_returned = null;
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			    }
        	}
        }
        for (int j = 0; j < all_actions.size(); j++) {
            if (!choosen_action.get(j).isDone()) {
            	choosen_action.get(j).cancel(true);
            }
        }
        if (to_be_returned != null) {
        	to_be_returned = all_actions.get(index_best);
        }
        System.out.println("Depth:" + Integer.toString(this.max_depth));
        return to_be_returned;
    }

    public double maxValue(BitState bitState, double alpha, double beta, int depth, int max_depth, 
    		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, StateDictEntry>> state_hash_table) throws Exception{
    	if (Thread.interrupted()) {
        	Thread.currentThread().interrupt();
        	return 0.0;
        }
        int tmp_victory = bitState.check_victory();
        if (tmp_victory == -1 && this.game.getColor().equalsIgnoreCase("BLACK"))  // king captured and black player -> Win
        	return Utils.MAX_VAL_HEURISTIC;
        else if (tmp_victory == -1 && this.game.getColor().equalsIgnoreCase("WHITE"))  // King captured and white player -> Lose
        	return -Utils.MAX_VAL_HEURISTIC;
        else if (tmp_victory == 1 && this.game.getColor().equalsIgnoreCase("BLACK"))  // King escaped and black player -> Lose
        	return -Utils.MAX_VAL_HEURISTIC;
        else if (tmp_victory == 1 && this.game.getColor().equalsIgnoreCase("WHITE"))  // King escaped and white player -> Win
        	return Utils.MAX_VAL_HEURISTIC;
        /*int state_hash = bitState.hashCode();
        int index_checkers = Utils.MAX_NUM_CHECKERS-Utils.cont_pieces(bitState);
        StateDictEntry hash_result = null;
        if (state_hash_table.get(index_checkers).contains(state_hash))
        	hash_result = state_hash_table.get(index_checkers).get(state_hash);
        List<List<Integer>> all_actions = null;
        if (hash_result != null) {
        	if (hash_result.getUsed() == 1)
        	    return -Utils.DRAW_POINTS;
        	all_actions = hash_result.getActions();
        }*/
        if (cutoff_test(depth, max_depth)) { // If reached maximum depth or total time
        	//if (hash_result != null && hash_result.getValue() != Double.MAX_VALUE)
        	//	return hash_result.getValue();  // If state previously evaluated don't recompute heuristic
        	double value = bitState.compute_heuristic(this.game.getWeights(), this.game.getColor());  // If state not previously evaluated
        	//add_to_hash(state_hash_table, state_hash, value, all_actions, index_checkers, max_depth, depth);  // Add state and value to hash table
        	return value;
        }

        /*int state_hash = bitState.hashCode();
        int index_checkers = Utils.MAX_NUM_CHECKERS-Utils.cont_pieces(bitState);
        StateDictEntry hash_result = null;
        if (state_hash_table.get(index_checkers).contains(state_hash)) {
        	hash_result = state_hash_table.get(index_checkers).get(state_hash);
        	if (hash_result.getMax_depth()-hash_result.getCurrent_depth() == max_depth-depth)
        		return hash_result.getValue();
        }*/
        double v = Double.NEGATIVE_INFINITY;
        /*if (all_actions == null) {
            all_actions = this.game.produce_actions(bitState);
            if (hash_result != null) {
                add_to_hash(state_hash_table, state_hash, hash_result.getValue(), all_actions, index_checkers, max_depth, depth);
            }
            else {
            	add_to_hash(state_hash_table, state_hash, Double.MAX_VALUE, all_actions, index_checkers, max_depth, depth);
            }
        }*/
        List<List<Integer>> all_actions = this.game.produce_actions(bitState);
        if (all_actions.size() == 0)
            return -Utils.MAX_VAL_HEURISTIC;
        for (List<Integer> action : all_actions) {
            v = Math.max(v, minValue(new BitState(bitState, action), alpha, beta, depth + 1, max_depth, state_hash_table));
            if (v >= beta) {
            	//add_to_hash(state_hash_table, state_hash, v, all_actions, index_checkers, max_depth, depth);
                return v;
            }
            alpha = Math.max(alpha, v);
        }
        //add_to_hash(state_hash_table, state_hash, v, all_actions, index_checkers, max_depth, depth);
        return v;
    }

    public double minValue(BitState bitState, double alpha, double beta, int depth, int max_depth, 
    		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, StateDictEntry>> state_hash_table) throws Exception{
        if (Thread.interrupted()) {
        	Thread.currentThread().interrupt();
        	return 0.0;
        }
        int tmp_victory = bitState.check_victory();
        if (tmp_victory == -1 && this.game.getColor().equalsIgnoreCase("BLACK"))  // king captured and black player -> Win
        	return Utils.MAX_VAL_HEURISTIC;
        else if (tmp_victory == -1 && this.game.getColor().equalsIgnoreCase("WHITE"))  // King captured and white player -> Lose
        	return -Utils.MAX_VAL_HEURISTIC;
        else if (tmp_victory == 1 && this.game.getColor().equalsIgnoreCase("BLACK"))  // King escaped and black player -> Lose
        	return -Utils.MAX_VAL_HEURISTIC;
        else if (tmp_victory == 1 && this.game.getColor().equalsIgnoreCase("WHITE"))  // King escaped and white player -> Win
        	return Utils.MAX_VAL_HEURISTIC;
        /*int state_hash = bitState.hashCode();
        int index_checkers = Utils.MAX_NUM_CHECKERS-Utils.cont_pieces(bitState);
        StateDictEntry hash_result = null;
        if (state_hash_table.get(index_checkers).contains(state_hash))
        	hash_result = state_hash_table.get(index_checkers).get(state_hash);
        List<List<Integer>> all_actions = null;
        if (hash_result != null) {
        	if (hash_result.getUsed() == 1)
        	    return -Utils.DRAW_POINTS;
        	all_actions = hash_result.getActions();
        }*/
        if (cutoff_test(depth, max_depth)) { // If reached maximum depth or total time
        	//if (hash_result != null && hash_result.getValue() != Double.MAX_VALUE)
        	//	return hash_result.getValue();  // If state previously evaluated don't recompute heuristic
        	double value = bitState.compute_heuristic(this.game.getWeights(), this.game.getColor());  // If state not previously evaluated
        	//add_to_hash(state_hash_table, state_hash, value, all_actions, index_checkers, max_depth, depth);  // Add state and value to hash table
        	return value;
        }
        /*int state_hash = bitState.hashCode();
        int index_checkers = Utils.MAX_NUM_CHECKERS-Utils.cont_pieces(bitState);
        StateDictEntry hash_result = null;
        if (state_hash_table.get(index_checkers).contains(state_hash)) {
        	hash_result = state_hash_table.get(index_checkers).get(state_hash);
        	if (hash_result.getMax_depth()-hash_result.getCurrent_depth() == max_depth-depth)
        		return hash_result.getValue();
        }*/
        double v = Double.POSITIVE_INFINITY;
        /*if (all_actions == null) {
            all_actions = this.game.produce_actions(bitState);
            if (hash_result != null) {
                add_to_hash(state_hash_table, state_hash, hash_result.getValue(), all_actions, index_checkers, max_depth, depth);
            }
            else {
            	add_to_hash(state_hash_table, state_hash, Double.MAX_VALUE, all_actions, index_checkers, max_depth, depth);
            }
        }*/
        List<List<Integer>> all_actions = this.game.produce_actions(bitState);
        if (all_actions.size() == 0)
            return Utils.MAX_VAL_HEURISTIC;
        for (List<Integer> action : all_actions) {
            v = Math.min(v, maxValue(new BitState(bitState, action), alpha, beta, depth + 1, max_depth, state_hash_table));
            if (v <= alpha) {
            	//add_to_hash(state_hash_table, state_hash, v, all_actions, index_checkers, max_depth, depth);
                return v;
            }
            beta = Math.min(beta, v);
        }
        //add_to_hash(state_hash_table, state_hash, v, all_actions, index_checkers, max_depth, depth);
        return v;
    }

    Boolean cutoff_test(int depth, int max_depth) {
        if (depth >= max_depth)
            return true;
        return false;
    }
    
    void add_to_hash(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, StateDictEntry>> table, int state_hash, double value, List<List<Integer>> all_actions, int index_checkers, int max_depth, int current_depth) {
    	table.get(index_checkers).put(state_hash, new StateDictEntry(state_hash, value, all_actions, 0, max_depth, current_depth));
    }

	public void updateState_hash_table(BitState bitState) {
		Utils.update_used(this.state_hash_table, bitState, this.game.getWeights(), this.game.getColor());
	}

	public int getMax_depth() {
		return max_depth;
	}

	public void setMax_depth(int max_depth) {
		this.max_depth = max_depth;
	}
}