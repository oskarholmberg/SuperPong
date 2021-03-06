package com.oskarholmberg.fitzwilliam.handlers;

/**
 * Created by erik on 08/05/16.
 */
public class PlayerInput {

    public static boolean[] keys;
    public static boolean[] pkeys;
    public static int x, y;
    public static boolean pdown, down;

    public static final int NUM_KEYS = 6, BUTTON_W = 0, BUTTON_Y = 1, BUTTON_RIGHT = 2, BUTTON_LEFT = 3, BUTTON_E = 4, BUTTON_K = 5;

    static{
        keys = new boolean[NUM_KEYS];
        pkeys = new boolean[NUM_KEYS];
    }

    public static void update(){
        for (int i = 0; i < NUM_KEYS; i++) {
            pkeys[i] = keys[i];
        }
    }

    public static boolean isDown() { return down; }
    public static boolean isPressed() {
        return down;
    }
    public static boolean isReleased() { return !down && pdown; }

    public static void setKey(int i, boolean b){ keys[i] = b;  }

    public static boolean isDown(int i){
        return keys[i];
    }

    public static boolean isPressed(int i){
        return keys[i] && !pkeys[i];
    }
}
