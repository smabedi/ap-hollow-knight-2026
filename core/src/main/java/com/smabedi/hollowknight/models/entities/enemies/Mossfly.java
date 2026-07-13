package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class Mossfly extends Enemy {
    private boolean isHidden = true; // Starts disguised as a bush

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
            // When it dies, turn gravity back on so the corpse falls to the ground!
            b2body.setGravityScale(1f);
            return;
        }

        // Handle the 1-second Stun Lock from taking damage
        if (stunTimer > 0) {
            stunTimer -= dt;
            // Apply a braking force so it doesn't float away infinitely from the knockback
            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y * 0.9f);
            return;
        }

        Vector2 playerPos = player.b2body.getPosition();
        Vector2 myPos = b2body.getPosition();
        float distanceToPlayer = myPos.dst(playerPos);

        // STATE 1: Disguised as a bush
        if (isHidden) {
            // Keep X velocity 0, but let Y velocity act naturally (falling to ground)
            b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

            // If the player gets too close, break the disguise!
            float aggroRadius = Constants.Enemy.Mossfly.AGGRO_RADIUS;
            if (distanceToPlayer <= aggroRadius) {
                isHidden = false;
                System.out.println("Mossfly aggro triggered! Commencing chase.");
                // START THE LOOP: Triggered exactly when aggro breaks
                loopSoundId = AudioManager.loopSpatialSfx(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, myPos, playerPos, 15f);
            }
        }
        // STATE 2: Actively chasing the Knight
        else {
            // UPDATE THE LOOP: Adjust volume/pan dynamically as it chases the Knight
            if (loopSoundId != -1) {
                AudioManager.updateSpatialSfx(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, loopSoundId, myPos, playerPos, 15f);
            }
            // Calculate the exact vector pointing from the Mossfly to the Player
            Vector2 direction = new Vector2(playerPos.x - myPos.x, playerPos.y - myPos.y);

            // Normalize the vector (makes its length exactly 1) so speed is consistent
            direction.nor();

            // Apply the movement speed
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
        // Reset its state back to a hidden bush when it respawns
        this.isHidden = true;
        this.b2body.setGravityScale(1f); // Reset gravity so it falls back down
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_MOSSFLY_FLY, loopSoundId);
            loopSoundId = -1;
        }
    }

    public boolean isHidden() {
        return isHidden;
    }
}
