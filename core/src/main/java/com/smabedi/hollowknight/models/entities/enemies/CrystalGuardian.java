package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class CrystalGuardian extends Enemy {
    private GuardianState currentState;
    private float stateTimer;
    private boolean facingRight;
    private final boolean originalFacingRight; // To remember which way to look when returning

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

        switch (currentState) {
            case IDLE:
                // Stand perfectly still
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

                // Cast a long ray to look for the player
                Vector2 visionEnd = new Vector2(
                    center.x + (direction * Constants.Enemy.CrystalGuardian.VISION_RANGE),
                    center.y
                );
                final boolean[] spotted = {false};

                world.rayCast((fixture, _, _, fraction) -> {
                    Object data = fixture.getUserData();

                    if ("ground".equals(data) || "spikes".equals(data)) {
                        return fraction; // clip ray at obstacle
                    }
                    if ("knight".equals(data)) {
                        spotted[0] = true;
                        return fraction; // clip ray at player
                    }

                    return -1; // ignore everything else
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
                    // Preserving your exact fireLaser call (with whatever inputs you originally had, e.g., dt, player, etc.)
                    fireLaser(player, center, direction);
                    System.out.println("Crystal Guardian: ENRAGED!");
                    currentState = GuardianState.FIRING_LASER;
                    // Set countdown to the length of the laser animation (e.g., 0.6 seconds)
                    stateTimer = 1f;
                }
                break;

            case FIRING_LASER:
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y); // Stand still!
                stateTimer -= dt; // Countdown while firing
                if (stateTimer <= 0) {
                    currentState = GuardianState.ENRAGED;
                    // Reset timer to whatever ENRAGED needs (or 0 if it counts up)
                    stateTimer = Constants.Enemy.CrystalGuardian.ENRAGE_DURATION;
                }
                break;

            case ENRAGED:
                // High-speed charge
                b2body.setLinearVelocity(
                    direction * Constants.Enemy.CrystalGuardian.CHARGE_SPEED,
                    b2body.getLinearVelocity().y
                );

                // Prevent falling off ledges or getting stuck on walls during the charge
                if (isPathBlocked(center, direction)) {
                    facingRight = !facingRight; // Turn around if it hits a wall while enraged
                }

                stateTimer -= dt;
                if (stateTimer <= 0) {
                    currentState = GuardianState.RETURNING;
                }
                break;

            case RETURNING:
                float originalX = startX;
                float distanceToStart = originalX - center.x;

                // If we are close enough to the start, snap to it and go IDLE
                if (Math.abs(distanceToStart) < 0.025f) {
                    b2body.setTransform(originalX, b2body.getPosition().y, 0);
                    facingRight = originalFacingRight;
                    currentState = GuardianState.IDLE;
                } else {
                    // Walk back to the starting position
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

    private void fireLaser(Knight player, Vector2 center, float direction) {
        // Fire an instant raycast that damages the player if they haven't moved out of the way!
        Vector2 laserEnd = new Vector2(
            center.x + (direction * Constants.Enemy.CrystalGuardian.VISION_RANGE),
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
    }

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
    public void respawn() {
        super.respawn();
        this.currentState = GuardianState.IDLE;
        this.facingRight = originalFacingRight;
    }

    public GuardianState getCurrentState() {
        return currentState;
    }

    public boolean isFacingRight() {
        return facingRight;
    }
}
