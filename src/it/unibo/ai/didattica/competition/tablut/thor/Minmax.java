package it.unibo.ai.didattica.competition.tablut.thor;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    private final State.Turn color;
    private final Game game;
    private final HeuristicTHOR heuristic;

    private static final Random rand = new Random();
    private BitState currentState;

    private static List<Integer> result;
    private static List<List<Integer>> possibleActions;


    public Minmax(Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table, Game game) {
        this.state_hash_table = state_hash_table;
        this.heuristic = new HeuristicTHOR(color);
        this.game = game;
    }
    
    public List<Integer> makeDecision(int max_time, BitState state, Game game) throws IOException {

        Future<List<Integer>> choosen_action = executorService.submit(this);
        result = null;
        possibleActions.clear();

        try {
            result = choosen_action.get(max_time, TimeUnit.SECONDS);
            System.out.println("Choosen action: {" + result.toString() + "}");
        } catch (TimeoutException e) {

            if(!possibleActions.isEmpty()) {
                result = possibleActions.get(rand.nextInt(possibleActions.size()));
            	System.out.println("Choosen action: {" + result.toString() + "}");
            } else {
            	System.out.println("Action not found.");
            }

            return result;

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

            double value = minValue(new BitState(currentState, action), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0);

            if(Thread.interrupted()){
                gestisciTerminazione();
                return possibleActions.get(rand.nextInt(possibleActions.size()));
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

    public double maxValue(BitState bitState, double alpha, double beta, int depth) throws Exception{

        if(Thread.interrupted()){
            stopThread();
            return 0;
        }

        if (bitState.getTurn() == State.Turn.BLACKWIN || bitState.getTurn() == State.Turn.WHITEWIN || depth >= currDepthLimit)
            return evaluate(bitState, player, depth);

        double value = Double.NEGATIVE_INFINITY;

        for (List<Integer> action : this.game.produce_actions(bitState)) {
            value = Math.max(value, minValue(new BitState(bitState, action), alpha, beta, depth + 1));
            if (value >= beta)
                return value;
            alpha = Math.max(alpha, value);
        }
        return value;
    }

    private void stopThread() {
        Thread.currentThread().stop();
    }

    public double minValue(BitState bitState, double alpha, double beta, int depth) throws Exception{

        if(Thread.interrupted()){
        	stopThread();
            return 0;
        }

        if (bitState.getTurn() == State.Turn.BLACKWIN || bitState.getTurn() == State.Turn.WHITEWIN || depth >= currDepthLimit)
            return evaluate(bitState, player, depth);

        double value = Double.POSITIVE_INFINITY;

        for (List<Integer> action : this.game.produce_actions(bitState)) {
            value = Math.min(value, maxValue(new BitState(bitState, action), alpha, beta, depth + 1));
            if (value <= alpha)
                return value;
            beta = Math.min(beta, value);
        }
        return value;
    }


}