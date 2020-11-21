package it.unibo.ai.didattica.competition.tablut.thor;

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 *
 * @author Carlo Cena, Giacomo Zamprogno
 *
 */

public final class Minmax implements Callable<List<Integer>> {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table;
    private int max_depth;
    private final Game game;

    private static final Random rand = new Random();
    private BitState currentState;

    private static List<Integer> result;
    private static List<List<Integer>> possibleActions;


    public Minmax(Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table, Game game) {
        this.state_hash_table = state_hash_table;
        this.game = game;
        this.max_depth = 2;
    }
    
    public List<Integer> makeDecision(int max_time, BitState state, Game game) throws IOException {

        Future<List<Integer>> choosen_action = executorService.submit(this);
        result = null;
        possibleActions.clear();

        try {
            result = choosen_action.get(max_time, TimeUnit.SECONDS);
            System.out.println("Choosen action: {" + result.toString() + "}");
        } catch (TimeoutException e) {
        	result = null;
            System.out.println("Max depth:" + Integer.toString(this.max_depth));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Integer> call() throws Exception {

        double v = Double.NEGATIVE_INFINITY;

        List<List<Integer>> all_actions = this.game.produce_actions(currentState);
        Collections.shuffle(all_actions);

        result = all_actions.get(0);
        possibleActions.add(all_actions.get(0));

        for (List<Integer> action : all_actions) {

            double value = minValue(new BitState(currentState, action), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, 
            		this.max_depth, this.state_hash_table);

            try {
                if (Thread.interrupted()) 
                        throw new InterruptedException();
                } 
        	catch (InterruptedException e) {
                    return null;
                    }

            if (value > v) {
            	result = action;
                possibleActions.clear();
                possibleActions.add(action);
                v = value;
            }

            else if(value == v){
            	possibleActions.add(action);
            	v = value;
                }
        }
        return possibleActions.get(rand.nextInt(possibleActions.size()));
    }

    public double maxValue(BitState bitState, double alpha, double beta, int depth, int max_depth, 
    		Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table) throws Exception{

    	try {
            if (Thread.interrupted()) 
                    throw new InterruptedException();
            } 
    	catch (InterruptedException e) {
                return 0;
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
        int state_hash = bitState.hashCode();
        int index_checkers = Utils.MAX_NUM_CHECKERS-Utils.cont_pieces(bitState);
        StateDictEntry hash_result = state_hash_table.get(index_checkers).get(state_hash);
        List<List<Integer>> all_actions = null;
        if (hash_result != null) {
        	if (hash_result.getUsed() == 1)
        	    return -Utils.DRAW_POINTS;
        	if (hash_result.getActions() == null)
        		all_actions = hash_result.getActions();
        }
        if (cutoff_test(depth, max_depth)) { // If reached maximum depth or total time
        	if (hash_result != null && hash_result.getValue() != Double.MAX_VALUE)
        		return hash_result.getValue();  // If state previously evaluated don't recompute heuristic
        	double value = bitState.compute_heuristic(this.game.getWeights(), this.game.getColor());  // If state not previously evaluated
        	add_to_hash(state_hash_table, state_hash, value, null, index_checkers, max_depth, depth);  // Add state and value to hash table
        	return value;
        }

        double v = Double.NEGATIVE_INFINITY;
        if (all_actions == null) {
            all_actions = this.game.produce_actions(bitState);
            if (hash_result != null)
                add_to_hash(state_hash_table, state_hash, hash_result.getValue(), all_actions, index_checkers, max_depth, depth);
        }
        if (all_actions.size() == 0)
            return -Utils.MAX_VAL_HEURISTIC;
        for (List<Integer> action : all_actions) {
            v = Math.max(v, minValue(new BitState(bitState, action), alpha, beta, depth + 1, max_depth, state_hash_table));
            if (v >= beta)
                return v;
            alpha = Math.max(alpha, v);
        }
        return v;
    }

    public double minValue(BitState bitState, double alpha, double beta, int depth, int max_depth, 
    		Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table) throws Exception{
    	
    	try {
            if (Thread.interrupted()) 
                    throw new InterruptedException();
            } 
    	catch (InterruptedException e) {
                return 0;
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
        int state_hash = bitState.hashCode();
        int index_checkers = Utils.MAX_NUM_CHECKERS-Utils.cont_pieces(bitState);
        StateDictEntry hash_result = state_hash_table.get(index_checkers).get(state_hash);
        List<List<Integer>> all_actions = null;
        if (hash_result != null) {
        	if (hash_result.getUsed() == 1)
        	    return -Utils.DRAW_POINTS;
        	if (hash_result.getActions() == null)
        		all_actions = hash_result.getActions();
        }
        if (cutoff_test(depth, max_depth)) { // If reached maximum depth or total time
        	if (hash_result != null && hash_result.getValue() != Double.MAX_VALUE)
        		return hash_result.getValue();  // If state previously evaluated don't recompute heuristic
        	double value = bitState.compute_heuristic(this.game.getWeights(), this.game.getColor());  // If state not previously evaluated
        	add_to_hash(state_hash_table, state_hash, value, null, index_checkers, max_depth, depth);  // Add state and value to hash table
        	return value;
        }

        double v = Double.POSITIVE_INFINITY;
        if (all_actions == null) {
            all_actions = this.game.produce_actions(bitState);
            if (hash_result != null)
                add_to_hash(state_hash_table, state_hash, hash_result.getValue(), all_actions, index_checkers, max_depth, depth);
        }
        if (all_actions.size() == 0)
            return Utils.MAX_VAL_HEURISTIC;
        for (List<Integer> action : this.game.produce_actions(bitState)) {
            v = Math.min(v, maxValue(new BitState(bitState, action), alpha, beta, depth + 1, max_depth, state_hash_table));
            if (v <= alpha)
                return v;
            beta = Math.min(beta, v);
        }
        return v;
    }

    Boolean cutoff_test(int depth, int max_depth) {
        if (depth >= max_depth)
            return true;
        return false;
    }
    
    void add_to_hash(Hashtable<Integer, Hashtable<Integer, StateDictEntry>> table, int state_hash, double value, List<List<Integer>> all_actions, int index_checkers, int max_depth, int current_depth) {
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