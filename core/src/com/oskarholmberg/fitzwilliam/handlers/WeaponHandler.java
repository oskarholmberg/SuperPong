package com.oskarholmberg.fitzwilliam.handlers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.oskarholmberg.fitzwilliam.entities.Bullet;
import com.oskarholmberg.fitzwilliam.entities.Grenade;
import com.oskarholmberg.fitzwilliam.entities.Sprite;
import com.oskarholmberg.fitzwilliam.handlers.pools.Pooler;
import com.oskarholmberg.fitzwilliam.net.packets.EntityCluster;
import com.oskarholmberg.fitzwilliam.net.packets.EntityPacket;
import com.oskarholmberg.fitzwilliam.gamestates.PlayState;
import com.oskarholmberg.fitzwilliam.net.packets.TCPEventPacket;


public class WeaponHandler {
    private int amountBullets;
    private int amountGrenades;
    private int entityPktSequence = 1;
    private float bulletRefresh = 0f, grenadeRefresh = 0f, sendWeaponEventsTimer = 0f;
    private HUD hud;
    private PlayState ps;
    private IntMap<Grenade> activeGrenades;
    private IntMap<Bullet> activeBullets;

    public WeaponHandler(HUD hud, PlayState playState){
        this.hud=hud;
        this.ps =playState;
        amountBullets = B2DVars.AMOUNT_BULLET;
        amountGrenades = B2DVars.AMOUNT_GRENADE;
        activeBullets = new IntMap<Bullet>();
        activeGrenades = new IntMap<Grenade>();
    }

    public void refresh(){
        amountBullets = B2DVars.AMOUNT_BULLET;
        amountGrenades = B2DVars.AMOUNT_GRENADE;
        hud.setAmountBulletsLeft(amountBullets);
        hud.setAmountGrenadesLeft(amountGrenades);
    }

    public boolean isMyWeapon(int id){
        if (activeGrenades.containsKey(id))
            return true;
        if (activeBullets.containsKey(id))
            return true;
        return false;
    }

    public void removeGrenade(int id){
        if (activeGrenades.containsKey(id)) {
            Grenade grenade = activeGrenades.remove(id);
            ps.world.destroyBody(grenade.getBody());
            grenade.dispose();
        } else if (activeBullets.containsKey(id)){
            Bullet bullet = activeBullets.remove(id);
            ps.world.destroyBody(bullet.getBody());
            bullet.dispose();
        }
    }

    public void shoot(){
        if (amountBullets == 0) {
            Assets.getSound("emptyClip").play();
        } else {
            int id = Tools.newEntityId();
            Vector2 pos = ps.player.getPosition();
            Bullet bullet = new Bullet(ps.world, pos.x, pos.y, ps.lastJumpDirection, false, id);
            activeBullets.put(id, bullet);
            amountBullets--;
            hud.setAmountBulletsLeft(amountBullets);
            TCPEventPacket pkt = Pooler.tcpEventPacket();
            pkt.id = bullet.getId();
            pkt.action = B2DVars.NET_NEW_ENTITY;
            pkt.miscString = "bullet";
            pkt.color = B2DVars.MY_COLOR;
            pkt.pos = bullet.getBody().getPosition();
            pkt.force = bullet.getBody().getLinearVelocity();
            ps.client.sendTCP(pkt);
            Pooler.free(pkt);
        }
    }

    public void throwGrenade() {
        if (amountGrenades > 0) {
            int id = Tools.newEntityId();
            Vector2 pos = ps.player.getPosition();
            Grenade spg = new Grenade(ps.world, pos.x, pos.y, ps.lastJumpDirection, false, id);
            activeGrenades.put(id, spg);
            amountGrenades--;
            hud.setAmountGrenadesLeft(amountGrenades);
            TCPEventPacket pkt = Pooler.tcpEventPacket();
            pkt.id = spg.getId();
            pkt.action = B2DVars.NET_NEW_ENTITY;
            pkt.miscString = "grenade";
            pkt.color = B2DVars.MY_COLOR;
            pkt.pos = spg.getBody().getPosition();
            pkt.force = spg.getBody().getLinearVelocity();
            ps.client.sendTCP(pkt);
            Pooler.free(pkt);
        }
    }

    private void refreshAmmo(float dt){
        if (ps.powerHandler.unlimitedAmmo()) {
            amountBullets = B2DVars.AMOUNT_BULLET;
            amountGrenades = B2DVars.AMOUNT_GRENADE;
            hud.setAmountBulletsLeft(amountBullets);
            hud.setAmountGrenadesLeft(amountGrenades);
        } else {
            if (bulletRefresh > 3f && amountBullets == 0) {
                amountBullets = B2DVars.AMOUNT_BULLET;
                bulletRefresh = 0;
                hud.setAmountBulletsLeft(amountBullets);
                Assets.getSound("reload").play();
            } else if (amountBullets == 0) {
                bulletRefresh += dt;
            }
            if (grenadeRefresh > 8f && amountGrenades == 0) {
                amountGrenades = B2DVars.AMOUNT_GRENADE;
                grenadeRefresh = 0f;
                hud.setAmountGrenadesLeft(amountGrenades);
            } else if (amountGrenades == 0) {
                grenadeRefresh += dt;
            }
        }
    }

    private void bulletsHittingWall() {
        for (int id : ps.contactListener.getIdsToRemove()) {
            if (activeBullets.containsKey(id)) {
                TCPEventPacket pkt = Pooler.tcpEventPacket();
                pkt.id = id;
                pkt.action = B2DVars.NET_DESTROY_BODY;
                ps.client.sendTCP(pkt);
                Pooler.free(pkt);
                Sprite bullet = activeBullets.remove(id);
                ps.world.destroyBody(bullet.getBody());
                bullet.dispose();
            }
        }
        ps.contactListener.clearIdList();
    }

    private void checkGrenadeTimer(float dt) {
        for (IntMap.Keys it = activeGrenades.keys(); it.hasNext; ) {
            int id = it.next();
            if (activeGrenades.get(id) != null && activeGrenades.get(id).lifeTimeReached(dt)) {
                TCPEventPacket pkt = Pooler.tcpEventPacket(); // grab it from the pool
                pkt.id = id;
                pkt.action = B2DVars.NET_DESTROY_BODY;
                ps.client.sendTCP(pkt);
                Pooler.free(pkt); // return it to the pool
                Sprite grenade = activeGrenades.remove(id);
                grenade.dispose();
                ps.world.destroyBody(grenade.getBody());
            }
        }
    }

    private void sendWeaponEvents(){
        if ((activeBullets.size + activeGrenades.size) > 0) {
            EntityPacket[] packets = new EntityPacket[activeBullets.size + activeGrenades.size];
            int index = 0;
            for (IntMap.Keys it = activeBullets.keys(); it.hasNext; ) {
                int id = it.next();
                EntityPacket pkt = Pooler.entityPacket(); // grab it from the pool
                Body b = activeBullets.get(id).getBody();
                pkt.xp = b.getPosition().x;
                pkt.yp = b.getPosition().y;
                pkt.xf = b.getLinearVelocity().x;
                pkt.yf = b.getLinearVelocity().y;
                pkt.id = id;
                packets[index] = pkt;
                index++;
            }
            for (IntMap.Keys it = activeGrenades.keys(); it.hasNext; ) {
                int id = it.next();
                EntityPacket pkt = Pooler.entityPacket(); // grab it from the pool
                Body b = activeGrenades.get(id).getBody();
                pkt.xp = b.getPosition().x;
                pkt.yp = b.getPosition().y;
                pkt.xf = b.getLinearVelocity().x;
                pkt.yf = b.getLinearVelocity().y;
                pkt.id = id;
                packets[index] = pkt;
                index++;
            }
            EntityCluster cluster = Pooler.entityCluster(); // grab it from the pool
            cluster.seq = entityPktSequence++;
            cluster.pkts = packets;
            cluster.time = TimeUtils.millis();
            cluster.id = B2DVars.MY_ID;
            ps.client.sendUDP(cluster);
            Pooler.free(packets); //return them to the pool
            Pooler.free(cluster);
        }
    }

    public void update(float dt){
        for (IntMap.Keys it = activeGrenades.keys(); it.hasNext; ){
            activeGrenades.get(it.next()).update(dt);
        }
        refreshAmmo(dt);
        checkGrenadeTimer(dt);
        bulletsHittingWall();
        if (sendWeaponEventsTimer > B2DVars.ENTITY_UPDATE_FREQ){
            sendWeaponEventsTimer = 0f;
            sendWeaponEvents();
        } else {
            sendWeaponEventsTimer += dt;
        }
    }

    public void render(SpriteBatch sb){
        for (IntMap.Keys it = activeGrenades.keys(); it.hasNext; ){
            activeGrenades.get(it.next()).render(sb);
        }
        for (IntMap.Keys it = activeBullets.keys(); it.hasNext; ){
            activeBullets.get(it.next()).render(sb);
        }
    }
}
