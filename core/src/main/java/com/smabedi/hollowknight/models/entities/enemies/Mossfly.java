package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.knight.Knight;

/**
 * Aerial stealth entity.
 * Initially remains immobilized and camouflaged within the environment.
 * Evaluates proximity heuristics to dynamically initiate an active tracking pursuit.
 */
public class Mossfly extends Enemy {
    private boolean isHidden = true;

    public Mossfly(World world, float x, float y, EventCallback eventCallback) {
        super(world, x, y, Constants.Enemy.Mossfly.HP, eventCallback, EnemyType.MOSSFLY);
        define();
    }

    @Override
    public void defineShape() {
        CircleShape shape = new CircleShape();
        shape.setRadius(Constants.Enemy.Mossfly.RADIUS_HALVED_SCALED);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = Constants.Enemy.Mossfly.FRICTION;

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void update(float dt, Knight player) {
        checkRespawn(player);

        if (dead) {
            b2body.setGravityScale(1f);

            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y);

            return;
        }

        if (stunTimer > 0) {
            stunTimer -= dt;
            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y * 0.9f);
            return;
        }

        Vector2 playerPos = player.b2body.getPosition();
        Vector2 myPos = b2body.getPosition();
        float distanceToPlayer = myPos.dst(playerPos);

        if (isHidden) {
            b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

            float aggroRadius = Constants.Enemy.Mossfly.AGGRO_RADIUS;
            if (distanceToPlayer <= aggroRadius) {
                isHidden = false;
                System.out.println("Mossfly aggro triggered! Commencing chase.");
                loopSoundId = AudioManager.loopSpatialSfx(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, myPos, playerPos, 15f);
            }
        }
        else {
            if (loopSoundId != -1) {
                AudioManager.updateSpatialSfx(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, loopSoundId, myPos, playerPos, 15f);
            }
            Vector2 direction = new Vector2(playerPos.x - myPos.x, playerPos.y - myPos.y);

            direction.nor();

            float chaseSpeed = Constants.Enemy.Mossfly.SPEED;
            b2body.setLinearVelocity(direction.x * chaseSpeed, direction.y * chaseSpeed);
        }
    }

    @Override
    public void die() {
        super.die();
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, loopSoundId);
            loopSoundId = -1;
        }
    }

    @Override
    public void respawn() {
        super.respawn();
        this.isHidden = true;
        this.b2body.setGravityScale(1f);
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, loopSoundId);
            loopSoundId = -1;
        }
    }

    public boolean isHidden() {
        return isHidden;
    }
}
