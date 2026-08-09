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
 * Advanced stationary enemy entity.
 * Executes a state machine that transitions between passive monitoring,
 * long-range laser deployment, enraged pursuit, and origin resetting.
 */
public class CrystalGuardian extends Enemy {
    private final boolean originalFacingRight;
    private GuardianState currentState;
    private float stateTimer;
    private boolean facingRight;

    public CrystalGuardian(World world, float x, float y, boolean startsFacingRight, EventCallback eventCallback) {
        super(world, x, y, Constants.Enemy.CrystalGuardian.HP, eventCallback, EnemyType.CRYSTAL_GUARDIAN);
        this.facingRight = startsFacingRight;
        this.originalFacingRight = startsFacingRight;
        this.currentState = GuardianState.IDLE;
        define();
    }

    @Override
    public void defineShape() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            Constants.Enemy.CrystalGuardian.WIDTH_HALVED_SCALED,
            Constants.Enemy.CrystalGuardian.HEIGHT_HALVED_SCALED
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = Constants.Enemy.CrystalGuardian.FRICTION;

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void update(float dt, Knight player) {
        checkRespawn(player);

        if (dead) {
            b2body.setLinearVelocity(b2body.getLinearVelocity().x * 0.9f, b2body.getLinearVelocity().y);
            return;
        }

        if (stunTimer > 0) {
            stunTimer -= dt;
            return;
        }

        Vector2 center = b2body.getWorldCenter();
        float direction = facingRight ? 1f : -1f;

        boolean isMoving = (currentState == GuardianState.ENRAGED || currentState == GuardianState.RETURNING);

        if (isMoving) {
            if (loopSoundId == -1) {
                loopSoundId = AudioManager.loopSpatialSfx(Constants.Paths.Sounds.SFX_GUARDIAN_RUNNING_LOOP, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 20f);
            } else {
                AudioManager.updateSpatialSfx(Constants.Paths.Sounds.SFX_GUARDIAN_RUNNING_LOOP, loopSoundId, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 20f);
            }
        } else {
            if (loopSoundId != -1) {
                AudioManager.stopSfx(Constants.Paths.Sounds.SFX_GUARDIAN_RUNNING_LOOP, loopSoundId);
                loopSoundId = -1;
            }
        }

        switch (currentState) {
            case IDLE:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

                Vector2 visionEnd = new Vector2(
                    center.x + (direction * Constants.Enemy.CrystalGuardian.VISION_RANGE),
                    center.y
                );
                final boolean[] spotted = {false};

                world.rayCast((fixture, _, _, fraction) -> {
                    Object data = fixture.getUserData();

                    if ("ground".equals(data) || "spikes".equals(data)) {
                        return fraction;
                    }
                    if ("knight".equals(data)) {
                        spotted[0] = true;
                        return fraction;
                    }

                    return -1;
                }, center, visionEnd);

                if (spotted[0]) {
                    System.out.println("Crystal Guardian: Target Acquired. Prepping Laser.");
                    currentState = GuardianState.PREPPING_LASER;
                    stateTimer = Constants.Enemy.CrystalGuardian.LASER_TELEGRAPH_TIME;
                }
                break;


            case PREPPING_LASER:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    fireLaser(player, center, direction);
                    System.out.println("Crystal Guardian: ENRAGED!");
                    currentState = GuardianState.FIRING_LASER;
                    stateTimer = 1f;
                }
                break;

            case FIRING_LASER:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                stateTimer -= dt;
                if (stateTimer <= 0) {
                    currentState = GuardianState.ENRAGED;
                    stateTimer = Constants.Enemy.CrystalGuardian.ENRAGE_DURATION;
                }
                break;

            case ENRAGED:
                if (isPathBlocked(center, direction)) {
                    currentState = GuardianState.RETURNING;
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                } else {
                    b2body.setLinearVelocity(
                        direction * Constants.Enemy.CrystalGuardian.CHARGE_SPEED,
                        b2body.getLinearVelocity().y
                    );
                }

                stateTimer -= dt;
                if (stateTimer <= 0) {
                    currentState = GuardianState.RETURNING;
                }
                break;

            case RETURNING:
                float originalX = startX;
                float distanceToStart = originalX - center.x;

                if (Math.abs(distanceToStart) < 0.025f) {
                    b2body.setTransform(originalX, b2body.getPosition().y, 0);
                    facingRight = originalFacingRight;
                    currentState = GuardianState.IDLE;
                } else {
                    facingRight = distanceToStart > 0;
                    float returnDir = facingRight ? 1f : -1f;
                    b2body.setLinearVelocity(
                        returnDir * Constants.Enemy.CrystalGuardian.RETURN_SPEED,
                        b2body.getLinearVelocity().y
                    );
                }
                break;
        }
    }

    /**
     * Executes an instantaneous raycast representing a high-energy laser burst.
     */
    private void fireLaser(Knight player, Vector2 center, float direction) {
        Vector2 laserEnd = new Vector2(
            center.x + (direction * Constants.Enemy.CrystalGuardian.LASER_RANGE),
            center.y
        );

        world.rayCast((fixture, _, _, fraction) -> {
            Object data = fixture.getUserData();

            if ("ground".equals(data) || "spikes".equals(data)) {
                return fraction;
            }
            if ("knight".equals(data)) {
                player.takeDamage(1, direction);
                return fraction;
            }

            return -1;
        }, center, laserEnd);

        AudioManager.playSpatialSfx(Constants.Paths.Sounds.SFX_LASER_BURST, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 20f);
    }

    /**
     * Projects diagnostic raycasts to evaluate adjacent terrain geometry and prevent logic deadlocks.
     */
    private boolean isPathBlocked(Vector2 center, float direction) {
        Vector2 wallRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Enemy.CrystalGuardian.WIDTH_HALVED_SCALED),
            center.y
        );
        Vector2 ledgeRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Enemy.CrystalGuardian.WIDTH_HALVED_SCALED),
            center.y - (1.5f * Constants.Enemy.CrystalGuardian.HEIGHT_HALVED_SCALED)
        );

        final boolean[] blocked = {false};
        final boolean[] hasGround = {false};

        world.rayCast((fixture, _, _, _) -> {
            Object data = fixture.getUserData();
            if ("ground".equals(data) || "spikes".equals(data)) blocked[0] = true;
            return 1;
        }, center, wallRayEnd);

        world.rayCast((fixture, _, _, _) -> {
            if ("ground".equals(fixture.getUserData())) hasGround[0] = true;
            return 1;
        }, center, ledgeRayEnd);

        return blocked[0] || !hasGround[0];
    }

    @Override
    public void die() {
        super.die();
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_GUARDIAN_RUNNING_LOOP, loopSoundId);
            loopSoundId = -1;
        }
    }

    @Override
    public void respawn() {
        super.respawn();
        this.currentState = GuardianState.IDLE;
        this.facingRight = originalFacingRight;
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_GUARDIAN_RUNNING_LOOP, loopSoundId);
            loopSoundId = -1;
        }
    }

    public GuardianState getCurrentState() {
        return currentState;
    }

    public boolean isFacingRight() {
        return facingRight;
    }
}
