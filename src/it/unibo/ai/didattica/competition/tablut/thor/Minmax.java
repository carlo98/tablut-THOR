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

public final class Minmax implements Callable<Action> {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table;
    private final State.Turn color;
    private final Game game;
    private final HeuristicTHOR heuristic;

    private static final Random rand = new Random();
    private State currentState;

    private static Action result;
    private static List<Action> possibleActions;


    public Minmax(Hashtable<Integer, Hashtable<Integer, StateDictEntry>> state_hash_table, Game game) {
        this.state_hash_table = state_hash_table;
        this.heuristic = new HeuristicTHOR(color);
        this.game = game;
    }
    
    public Action makeDecision(int max_time, State state, Game game) throws IOException {

        Future<Action> choosen_action = executorService.submit(this);
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
    public Action call() throws Exception {

        double v = Double.NEGATIVE_INFINITY;

        List<Action> azioni = this.game.produce_actions(currentState);
        Collections.shuffle(azioni);

        result = azioni.get(0);
        possibleActions.add(azioni.get(0));

        for (Action action : azioni) {

            double value = minValue(this.checkMove(currentState.clone(), action), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0);

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

    /***max***/
    public double maxValue(State state, double alpha, double beta, int depth) throws Exception{

        if(Thread.interrupted()){
            gestisciTerminazione();

            return 0;
        }

        if (state.getTurn() == State.Turn.BLACKWIN || state.getTurn() == State.Turn.WHITEWIN || depth >= currDepthLimit)
            return evaluate(state, player, depth);

        double value = Double.NEGATIVE_INFINITY;

        for (Action action : u.getSuccessors(state)) {
            value = Math.max(value, minValue(this.checkMove(state.clone(), action), alpha, beta, depth + 1));
            if (value >= beta)
                return value;
            alpha = Math.max(alpha, value);
        }
        return value;
    }

    private void gestisciTerminazione() {
        Thread.currentThread().stop();
    }

    /***min***/
    public double minValue(State state, double alpha, double beta, int depth) throws Exception{

        if(Thread.interrupted()){
            gestisciTerminazione();
            return 0;
        }

        if (state.getTurn() == State.Turn.BLACKWIN || state.getTurn() == State.Turn.WHITEWIN || depth >= currDepthLimit)
            return evaluate(state, player, depth);

        double value = Double.POSITIVE_INFINITY;

        for (Action action : u.getSuccessors(state)) {
            value = Math.min(value, maxValue(this.checkMove(state.clone(), action), alpha, beta, depth + 1));
            if (value <= alpha)
                return value;
            beta = Math.min(beta, value);
        }
        return value;
    }


}