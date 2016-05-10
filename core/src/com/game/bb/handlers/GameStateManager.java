package com.game.bb.handlers;

import com.game.bb.main.Game;
import com.game.bb.gamestates.GameState;
import com.game.bb.net.GameClient;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by erik on 06/05/16.
 */
public class GameStateManager {
    private Game game;
    private Stack<GameState> states;
    private int PLAY = 123123;
    private ArrayList<String> actions;
    private ArrayList<String> opponentActions;


    public GameStateManager(Game game, String ipAddress, int port) {
        this.game = game;
        states = new Stack<GameState>();
        actions = new ArrayList<String>();
        opponentActions = new ArrayList<String>();
        new GameClient(this, ipAddress, port).start();
        pushState(PLAY);
    }

    public Game game() {
        return game;
    }

    public void update(float dt) {
        states.peek().update(dt);
    }

    public void render() {
        states.peek().render();
    }

    private GameState getState(int state) {
        if (state == PLAY)
            return new com.game.bb.gamestates.Play(this);
        return null;
    }

    public void setState(int state) {
        popState();
        pushState(state);
    }

    public void pushState(int state) {
        states.push(getState(state));
    }

    public void popState() {
        states.pop().dispose();
    }


    public synchronized void addAction(String action) {
        actions.add(action);
        notifyAll();
    }

    public synchronized String getAction(){
        if(actions.isEmpty()){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return actions.remove(0);
    }

    public synchronized void addOpponentAction(String action){
        opponentActions.add(action);
        notifyAll();
    }

    public synchronized String getOpponentAction() {
        if (!opponentActions.isEmpty()) {
            return opponentActions.remove(0);
        } else {
            return "NO_ACTION";
        }
    }
}

