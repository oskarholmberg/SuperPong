package com.oskarholmberg.fitzwilliam.handlers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.oskarholmberg.fitzwilliam.entities.PowerUp;
import com.oskarholmberg.fitzwilliam.handlers.pools.Pooler;
import com.oskarholmberg.fitzwilliam.net.packets.TCPEventPacket;
import com.oskarholmberg.fitzwilliam.gamestates.PlayState;

/**
 * Created by erik on 25/05/16.
 */
public class PowerupHandler {
    private float ammoAccum = 20f, tiltAccum = 20f, shieldAccum = 20f, tiltDirection = 1f, ghostAccum = 20f;
    private static final float AMMO_DUR = 10f, TILT_DUR = 10f, SHIELD_DUR = 10f, GHOST_DUR = 5f;
    private float rotationAngle = 0f;
    private boolean shielded = false, ghosted = false, tilted = false;
    private IntMap<PowerUp> powerups;
    private int xOffset = 25, yOffset = 30;
    private PlayState ps;
    private SpriteAnimation shield = new SpriteAnimation(Assets.getAnimation("shield"), 0.2f);

    public PowerupHandler(PlayState ps){
        this.ps=ps;
        powerups = new IntMap<PowerUp>();
    }

    public boolean unlimitedAmmo(){
        if (ammoAccum < AMMO_DUR){
            return true;
        }
        return false;
    }

    public void addPower(int id, PowerUp powerUp){
        powerups.put(id, powerUp);
    }

    public boolean containsPower(int id){
        return powerups.containsKey(id);
    }

    public void removePower(int id){
        PowerUp powerUp = powerups.remove(id);
        ps.world.destroyBody(powerUp.getBody());
        powerUp.dispose();
    }

    public void applyPowerup(int powerupType){
        switch (powerupType) {
            case B2DVars.POWERTYPE_AMMO:
                ammoAccum = 0f;
                break;
            case B2DVars.POWERTYPE_TILTSCREEN:
                tiltAccum = 0f;
                tilted = true;
                break;
            case B2DVars.POWERTYPE_SHIELD:
                shielded = true;
                TCPEventPacket pkt = Pooler.tcpEventPacket();
                pkt.id = B2DVars.MY_ID;
                pkt.action = B2DVars.NET_APPLY_ANTIPOWER;
                pkt.misc = B2DVars.POWERTYPE_SHIELD;
                pkt.miscString = "applyShield";
                PlayState.playState.client.sendTCP(pkt);
                Pooler.free(pkt);
                shieldAccum = 0f;
                break;
            case B2DVars.POWERTYPE_GHOST:
                ghostAccum = 0f;
                ghosted = true;
                PlayState.playState.player.setGhost(true);
                break;
        }
    }

    public void removeShield(){
        TCPEventPacket pkt = Pooler.tcpEventPacket();
        pkt.id = B2DVars.MY_ID;
        pkt.action = B2DVars.NET_APPLY_ANTIPOWER;
        pkt.misc = B2DVars.POWERTYPE_SHIELD;
        pkt.miscString = "removeShield";
        PlayState.playState.client.sendTCP(pkt);
        Pooler.free(pkt);
        shielded = false;
    }

    public void removeGhost(){
        ghosted = false;
        PlayState.playState.player.setGhost(false);
    }

    public boolean isShielded(){
        return shielded;
    }
    public boolean isGhosted(){
        return ghosted;
    }
    public float getRotationAngle(){ return rotationAngle; }

    public void tiltScreen(float dt){
        if (tiltAccum < TILT_DUR){
            tiltAccum += dt;
            if (rotationAngle > 20f){
                tiltDirection = -1f;
            } else if (rotationAngle < -20f){
                tiltDirection = 1f;
            }
            PlayState.playState.cam.rotate(1f * tiltDirection);
            rotationAngle+=1f*tiltDirection;
            if (tiltAccum >= TILT_DUR) {
                PlayState.playState.cam.rotate(-rotationAngle);
                rotationAngle = 0;
                float camX = PlayState.playState.player.getPosition().x * B2DVars.PPM;
                if ((camX + PlayState.playState.cam.viewportWidth / 2) > PlayState.playState.map.getMapWidth()){
                    PlayState.playState.cam.position.x = PlayState.playState.map.getMapWidth()
                            - PlayState.playState.cam.viewportWidth / 2;
                } else if ((camX - PlayState.playState.cam.viewportWidth / 2) < 0){
                    PlayState.playState.cam.position.x = 0 + PlayState.playState.cam.viewportWidth / 2;
                } else {
                    PlayState.playState.cam.position.x = camX;
                }
                tilted = false;
            }
        }
    }

    private void powerTaken(){
        if (ps.contactListener.powerTaken() && ps.contactListener.getLastPowerTaken() != null){
            PowerUp powerUp = powerups.remove(ps.contactListener.getLastPowerTaken().getId());
            int powerType = powerUp.getPowerType();
            ps.world.destroyBody(powerUp.getBody());
            powerUp.dispose();

            TCPEventPacket pkt = Pooler.tcpEventPacket();
            pkt.action = B2DVars.NET_DESTROY_BODY;
            pkt.id = powerUp.getId();
            ps.client.sendTCP(pkt);
            Pooler.free(pkt);

            switch (powerType) {
                case B2DVars.POWERTYPE_AMMO:
                    applyPowerup(powerType);
                    break;
                case B2DVars.POWERTYPE_TILTSCREEN:
                    TCPEventPacket pkt2 = Pooler.tcpEventPacket();
                    pkt2.action = B2DVars.NET_APPLY_ANTIPOWER;
                    pkt2.misc = powerType;
                    ps.client.sendTCP(pkt2);
                    Pooler.free(pkt2);
                    break;
                case B2DVars.POWERTYPE_SHIELD:
                    applyPowerup(powerType);
                    break;
                case B2DVars.POWERTYPE_GHOST:
                    applyPowerup(powerType);
            }
        }
    }

    public void update(float dt){
        powerTaken();
        shield.update(dt);
        for (IntMap.Keys it = powerups.keys(); it.hasNext;){
            powerups.get(it.next()).update(dt);
        }
        if (ghostAccum < GHOST_DUR){
            ghostAccum += dt;
            if (ghostAccum > GHOST_DUR){
                ghosted = false;
                PlayState.playState.player.setGhost(false);
            }
        }
        if (shieldAccum < SHIELD_DUR){
            shieldAccum += dt;
            if (shieldAccum > SHIELD_DUR){
                removeShield();
            }
        }
        if (ammoAccum < AMMO_DUR){
            ammoAccum += dt;
        }
        if (tilted){
            tiltScreen(dt);
        }
    }

    public void render(SpriteBatch sb){
        for (IntMap.Keys it = powerups.keys(); it.hasNext;){
            powerups.get(it.next()).render(sb);
        }
        if(shielded){
            sb.begin();
            Vector2 pos = PlayState.playState.player.getPosition();
            sb.draw(shield.getFrame(), pos.x* B2DVars.PPM-xOffset, pos.y* B2DVars.PPM-yOffset, 50, 60);
            sb.end();
        }
    }

}
