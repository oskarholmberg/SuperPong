package com.oskarholmberg.fitzwilliam.net.packets;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by erik on 18/05/16.
 */
public class PlayerMovementPacket implements Pool.Poolable{
    public float xp, yp, xv, yv;
    public int tex, sound, seq, id;
    public long time;

    @Override
    public void reset() {
        id=0;
    }
}
