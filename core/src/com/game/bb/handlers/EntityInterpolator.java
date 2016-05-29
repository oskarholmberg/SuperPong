package com.game.bb.handlers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.game.bb.entities.EnemyEntity;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;


public class EntityInterpolator {
    private EnemyEntity entity;
    private Body body;
    private Array<EntityPacket> entityStates;
    private Vector2 targetPos, currentPos, interpolatedPos;
    private long lastUpdateTime;

    public EntityInterpolator(EnemyEntity entity){
        this.entity=entity;
        targetPos = new Vector2();
        interpolatedPos = new Vector2();
    }

    public void init(){
        body = entity.getBody();
        currentPos = body.getPosition();
        lastUpdateTime = TimeUtils.millis();
        entityStates = new Array<EntityPacket>();
    }

    public float getAlpha(){
        long now = TimeUtils.millis();
        float alpha = (now - lastUpdateTime) / 50f;
        lastUpdateTime = now;
        System.out.println(alpha);
        return MathUtils.clamp(alpha, 0f, 1.0f);
    }



    public void addEntityPacket(EntityPacket pkt){
        entityStates.add(pkt);
    }

    public void updateEntityState(){
        if (entityStates.size > 0) {
            if ((entityStates.peek().time + 10) >= TimeUtils.millis()) {
                EntityPacket pkt = entityStates.pop();
                currentPos.set(body.getPosition());
                targetPos.set(pkt.xp, pkt.yp);
                interpolatedPos.set(currentPos).lerp(targetPos, getAlpha());
                body.setTransform(interpolatedPos, 0);
                body.setLinearVelocity(pkt.xf, pkt.yf);
            }
        }
    }

    public Vector2 getPlayerPosition(PlayerMovementPacket pkt){
        currentPos.set(body.getPosition());
        targetPos.set(pkt.xp, pkt.yp);
        interpolatedPos.set(currentPos).lerp(targetPos, getAlpha());
        return interpolatedPos;
    }
}
