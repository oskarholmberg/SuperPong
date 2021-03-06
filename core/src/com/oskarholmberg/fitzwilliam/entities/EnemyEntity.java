package com.oskarholmberg.fitzwilliam.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.oskarholmberg.fitzwilliam.handlers.EntityInterpolator;
import com.oskarholmberg.fitzwilliam.handlers.SpriteAnimation;
import com.oskarholmberg.fitzwilliam.net.packets.EntityPacket;

/**
 * Created by erik on 21/05/16.
 */
public abstract class EnemyEntity implements Disposable, Pool.Poolable{

    protected Body body;
    protected int id;
    protected SpriteAnimation animation;
    protected float textureOffset = 0, textureWidth, textureHeight;
    protected EntityInterpolator interpolator;

    public EnemyEntity(){
        interpolator = new EntityInterpolator(this);
    }

    public void addEntityPacket(EntityPacket pkt){
        interpolator.addEntityPacket(pkt);
    }

    public void setId(int id){
        this.id = id;
    }

    public void initInterpolator(){
        interpolator.init();
    }

    public abstract void setAnimation(String color);
    public abstract void render(SpriteBatch sb);
    public abstract void update(float dt);

    public Body getBody(){
        return body;
    }

    public int getId(){
        return id;
    }

}
