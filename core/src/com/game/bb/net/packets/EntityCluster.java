package com.game.bb.net.packets;

import com.badlogic.gdx.utils.Pool;

import java.util.List;

/**
 * Created by erik on 18/05/16.
 */
public class EntityCluster implements Pool.Poolable{
    public int seq, id;
    public EntityPacket[] pkts;
    public long time;

    @Override
    public void reset() {
        time=0;
        seq=0;
        pkts=null;
    }
}
