package com.game.bb.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by erik on 11/05/16.
 */
public class SPBullet extends SPSprite {

    public SPBullet(Body body, boolean harmful) {
        super(body);
        if (harmful) {
            // set enemy color texture
            //setTexture(new Texture("TEXTURENAME"));
        } else {
            // set friendly color texture
        }
    }
}
