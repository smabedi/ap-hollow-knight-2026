package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.knight.Knight;

/**
 * Mid-tier ground entity.
 * Executes persistent line-of-sight raycasting to dynamically interrupt its standard
 * patrol algorithm and transition into an aggressive, high-speed charge vector.
 */
public class HuskHornhead extends Enemy {
    private HornheadState currentState;
    private float stateTimer;
    private boolean movingRight = true;

    public HuskHornhead(World world, float x, float y, EventCallback eventCallback) {
        super(world, x, y, Constants.Enemy.HuskHornhead.HP, eventCallback, EnemyType.HUSK_HORNHEAD);
        this.currentState = HornheadState.WALKING;
        this.stateTimer = Constants.Enemy.HuskHornhead.WALK_DURATION;
        define();
    }

    @Override
    public void defineShape() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(Constants.Enemy.HuskHornhead.WIDTH_HALVED_SCALED, Constants.Enemy.HuskHornhead.HEIGHT_HALVED_SCALED);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = Constants.Enemy.HuskHornhead.FRICTION;

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void update(float dt, Knight player) {
        checkRespawn(player);

        if (dead) {
            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y);
            return;
        }

        if (stunTimer > 0) {
            stunTimer -= dt;
            return;
        }

        Vector2 center = b2body.getWorldCenter();
        float direction = movingRight ? 1f : -1f;

        // Perform environmental safety evaluations to detect upcoming drop-offs or solid barricades
        Vector2 wallRayEnd = new Vector2(
            center.x + (direction * 2f * Constants.Enemy.HuskHornhead.WIDTH_HALVED_SCALED),
            center.y
        );
        final boolean[] hitWall = {false};
        world.rayCast((fixture, _, _, _) -> {
            if (hitEnd(fixture)) hitWall[0] = true;
            return 1;
        }, center, wallRayEnd);

        Vector2 ledgeRayEnd = new Vector2(
            center.x + (direction * 2f * Constants.Enemy.HuskHornhead.WIDTH_HALVED_SCALED),
            center.y - (2f * Constants.Enemy.HuskHornhead.HEIGHT_HALVED_SCALED)
        );
        final boolean[] hitGround = {false};
        world.rayCast((fixture, _, _, _) -> {
            if (hitEnd(fixture)) hitGround[0] = true;
            return 1;
        }, center, ledgeRayEnd);

        boolean pathBlocked = hitWall[0] || !hitGround[0];

        // Process optical recognition vector prior to initiating aggressive maneuvers
        if (currentState != HornheadState.CHARGING) {
            Vector2 visionEnd = new Vector2(
                center.x + (direction * Constants.Enemy.HuskHornhead.VISION_RANGE),
                center.y
            );

            final boolean[] sawPlayer = {false};

            // Evaluate line-of-sight using fractional callback returns to accurately detect ray obstructions
            world.rayCast((fixture, _, _, fraction) -> {
                Object userData = fixture.getUserData();

                if ("ground".equals(userData) || "spikes".equals(userData)) {
                    sawPlayer[0] = false;
                    return fraction;
                }
                if ("knight".equals(userData)) {
                    sawPlayer[0] = true;
                    return fraction;
                }

                return 1;
            }, center, visionEnd);

            if (sawPlayer[0]) {
                System.out.println("Husk Hornhead spotted the Knight! CHARGING!");
                currentState = HornheadState.CHARGING;
            }
        }

        boolean isMoving = (currentState == HornheadState.WALKING || currentState == HornheadState.CHARGING);

        if (isMoving) {
            if (loopSoundId == -1) {
                loopSoundId = AudioManager.loopSpatialSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 15f);
            } else {
                AudioManager.updateSpatialSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, loopSoundId, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 15f);
            }
        } else {
            if (loopSoundId != -1) {
                AudioManager.stopSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, loopSoundId);
                loopSoundId = -1;
            }
        }

        switch (currentState) {
            case WALKING:
                if (pathBlocked) {
                    movingRight = !movingRight;
                }
                b2body.setLinearVelocity(
                    direction * Constants.Enemy.HuskHornhead.WALK_SPEED,
                    b2body.getLinearVelocity().y
                );

                stateTimer -= dt;
                if (stateTimer <= 0) {
                    currentState = HornheadState.RESTING;
                    stateTimer = Constants.Enemy.HuskHornhead.REST_DURATION;
                }
                break;

            case RESTING:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    currentState = HornheadState.WALKING;
                    stateTimer = Constants.Enemy.HuskHornhead.WALK_DURATION;
                }
                break;

            case CHARGING:
                if (pathBlocked) {
                    System.out.println("Husk Hornhead crashed! Ending charge.");
                    currentState = HornheadState.RESTING;
                    stateTimer = Constants.Enemy.HuskHornhead.REST_DURATION;
                    movingRight = !movingRight;
                } else {
                    b2body.setLinearVelocity(
                        direction * Constants.Enemy.HuskHornhead.CHARGE_SPEED,
                        b2body.getLinearVelocity().y
                    );
                }
                break;
        }
    }

    @Override
    public void die() {
        super.die();
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, loopSoundId);
            loopSoundId = -1;
        }
    }

    @Override
    public void respawn() {
        super.respawn();
        this.currentState = HornheadState.WALKING;
        this.stateTimer = Constants.Enemy.HuskHornhead.WALK_DURATION;
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, loopSoundId);
            loopSoundId = -1;
        }
    }

    public HornheadState getCurrentState() {
        return currentState;
    }
}
