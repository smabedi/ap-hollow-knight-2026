package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class FalseKnight extends Enemy {
    private BossPhase currentPhase = BossPhase.PHASE_1;
    private BossMove currentMove = BossMove.IDLE;
    private BossMove lastMove = BossMove.IDLE;
    private BossSubState currentSubState = BossSubState.NONE;
    private float moveTimer = 0f;
    private float stunTimer = 0f;
    private float subStateTimer = 0f;
    private boolean actionExecuted = false; // Prevents the 60fps multi-impulse bug
    private int recentDamageCount = 0;
    private float damageTimer = 0f;

    public FalseKnight(World world, float x, float y) {
        super(world, x, y, Constants.FalseKnight.HP);
        define();
    }

    @Override
    public void defineShape() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            Constants.FalseKnight.WIDTH_HALVED_SCALED,
            Constants.FalseKnight.HEIGHT_HALVED_SCALED
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = Constants.FalseKnight.FRICTION;
        fixtureDef.density = Constants.FalseKnight.DENSITY;

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void update(float dt, Knight player) {
        if (dead) return;

        // Decay the recent damage memory
        if (damageTimer > 0) {
            damageTimer -= dt;
            if (damageTimer <= 0) recentDamageCount = 0;
        }

        // --- 1. Stun Phase Interrupt (Ice-Sliding Fix) ---
        if (currentPhase == BossPhase.STUNNED) {
            stunTimer -= dt;

            // Apply heavy dampening to horizontal momentum so he halts naturally
            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y);

            if (stunTimer <= 0) {
                currentPhase = BossPhase.PHASE_2;
                System.out.println("False Knight: Enraged! Phase 2 Initiated!");
                decideNextMove(player);
            }
            return;
        }

        // --- 2. State Machine Execution ---
        moveTimer -= dt;
        if (moveTimer <= 0) {
            decideNextMove(player);
        } else {
            executeCurrentMove(dt, player);
        }
    }

    private void decideNextMove(Knight player) {
        float distToPlayer = Math.abs(player.b2body.getPosition().x - b2body.getPosition().x);
        BossMove nextMove = BossMove.IDLE;

        // 1. Strict Document Rule: Trigger Defensive Leap if taking heavy rapid damage
        if (recentDamageCount >= 3 && lastMove != BossMove.DEFENSIVE_LEAP) {
            nextMove = BossMove.DEFENSIVE_LEAP;
            recentDamageCount = 0; // Reset memory after leaping away
        }

        // 2. Distance & Probability Logic with Anti-Spam
        while (nextMove == BossMove.IDLE || nextMove == lastMove) {
            int rand = MathUtils.random(1, 100);

            if (distToPlayer < 3f) { // CLOSE RANGE
                if (currentPhase == BossPhase.PHASE_2 && rand <= 20) {
                    nextMove = BossMove.POWER_SLAM;
                } else if (rand <= 60) {
                    nextMove = BossMove.MACE_SLAM;
                } else if (rand <= 75) {
                    nextMove = BossMove.DEFENSIVE_LEAP; // Still allow natural retreats
                } else {
                    nextMove = BossMove.CHARGE;
                }
            } else { // LONG RANGE
                if (currentPhase == BossPhase.PHASE_2 && rand <= 20) {
                    nextMove = BossMove.POWER_SLAM;
                } else if (rand <= 50) {
                    nextMove = BossMove.CHARGE;
                } else if (rand <= 85) {
                    nextMove = BossMove.OFFENSIVE_LEAP;
                } else {
                    nextMove = BossMove.MACE_SLAM;
                }
            }
        }

        currentMove = nextMove;
        lastMove = currentMove;
        actionExecuted = false; // Reset the impulse lock

        // Setup initial sub-state logic
        currentSubState = BossSubState.WIND_UP;
        float speedModifier = (currentPhase == BossPhase.PHASE_2) ? 0.7f : 1.0f;

        switch (currentMove) {
            case MACE_SLAM:
                moveTimer = 1.5f * speedModifier;
                subStateTimer = 0.5f * speedModifier; // 0.5s to raise mace
                break;
            case POWER_SLAM:
                moveTimer = 2.5f * speedModifier;
                subStateTimer = 0.8f * speedModifier; // Slower, heavier windup
                break;
            case CHARGE:
                moveTimer = 2.0f * speedModifier;
                subStateTimer = 0.3f * speedModifier; // Brief roar before charge
                break;
            case OFFENSIVE_LEAP:
            case DEFENSIVE_LEAP:
                moveTimer = 1.5f * speedModifier;
                subStateTimer = 0.2f * speedModifier; // Brief crouch before jump
                break;
            default:
                moveTimer = 1f;
                subStateTimer = 0f;
                currentSubState = BossSubState.ACTIVE;
                break;
        }

        System.out.println("False Knight using: " + currentMove.name() + " | Phase: " + currentPhase.name());
    }

    private void executeCurrentMove(float dt, Knight player) {
        float direction = player.b2body.getPosition().x > b2body.getPosition().x ? 1f : -1f;
        subStateTimer -= dt;

        // Sub-state progression logic
        if (subStateTimer <= 0) {
            if (currentSubState == BossSubState.WIND_UP) {
                currentSubState = BossSubState.ACTIVE;
                subStateTimer = getActiveDuration();
            } else if (currentSubState == BossSubState.ACTIVE) {
                currentSubState = BossSubState.RECOVERY;
                subStateTimer = getRecoveryDuration();
            }
        }

        // Halt movement during Wind-Up and Recovery phases
        if (currentSubState == BossSubState.WIND_UP || currentSubState == BossSubState.RECOVERY) {
            b2body.setLinearVelocity(b2body.getLinearVelocity().x * 0.8f, b2body.getLinearVelocity().y);
            return;
        }

        // --- ACTIVE Phase Execution ---
        if (currentSubState == BossSubState.ACTIVE) {
            switch (currentMove) {
                case CHARGE:
                    float chargeSpeed = (currentPhase == BossPhase.PHASE_2) ? 6f : 4f;
                    b2body.setLinearVelocity(direction * chargeSpeed, b2body.getLinearVelocity().y);
                    break;

                case OFFENSIVE_LEAP:
                    if (!actionExecuted && isGrounded()) {
                        b2body.setLinearVelocity(0, 0); // Clear residual velocity
                        b2body.applyLinearImpulse(new Vector2(direction * 5f, 8f), b2body.getWorldCenter(), true);
                        actionExecuted = true;
                    }
                    break;

                case DEFENSIVE_LEAP:
                    if (!actionExecuted && isGrounded()) {
                        b2body.setLinearVelocity(0, 0);
                        b2body.applyLinearImpulse(new Vector2(-direction * 6f, 5f), b2body.getWorldCenter(), true);
                        actionExecuted = true;
                    }
                    break;

                case MACE_SLAM:
                    if (!actionExecuted) {
                        executeMaceHitbox(player, direction);
                        actionExecuted = true;
                    }
                    break;

                case POWER_SLAM:
                    if (!actionExecuted) {
                        // Spawn Shockwave on slam execution
                        Vector2 pos = b2body.getPosition();
                        new Shockwave(world, pos.x, pos.y, 1);
                        new Shockwave(world, pos.x, pos.y, -1);
                        actionExecuted = true;
                        // TODO: Trigger heavy camera shake here
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private float getActiveDuration() {
        return switch (currentMove) {
            case MACE_SLAM -> 0.2f;
            case POWER_SLAM -> 0.3f;
            case CHARGE -> 1.2f;
            default -> 0.1f; // Leaps are instant impulses
        };
    }

    private float getRecoveryDuration() {
        // The rest of the moveTimer handles the remainder, but this provides a structured window
        return 0.5f;
    }

    private boolean isGrounded() {
        // Simple check to ensure impulses only fire from the floor
        return b2body.getLinearVelocity().y == 0;
    }

    @Override
    public void takeDamage(int amount) {
        if (dead) return;

        hp -= amount;
        recentDamageCount++;
        damageTimer = 2.0f; // Boss remembers hits for 2 seconds

        System.out.println("False Knight hit! HP: " + hp);

        // Stun Phase Shift (50% HP)
        if (hp <= maxHp / 2 && currentPhase == BossPhase.PHASE_1) {
            currentPhase = BossPhase.STUNNED;
            stunTimer = 4f;
            currentMove = BossMove.IDLE;
            currentSubState = BossSubState.NONE; // Abort animations
            actionExecuted = false;
            System.out.println("False Knight Stunned! Armor is open!");
        } else if (hp <= 0) {
            die();
        }
    }

    private void executeMaceHitbox(Knight player, float direction) {
        Vector2 center = b2body.getWorldCenter();

        // Define the area of the slam in front of the boss
        float slamReachX = 2.5f;
        float slamHeightY = 1.5f;

        float lowerX = direction == 1f ? center.x : center.x - slamReachX;
        float upperX = direction == 1f ? center.x + slamReachX : center.x;
        float lowerY = center.y - slamHeightY;
        float upperY = center.y + slamHeightY;

        // Instantly check if the player is caught in the slam area
        world.QueryAABB(fixture -> {
            if ("knight".equals(fixture.getUserData())) {
                // Apply 1 damage, pushing the player away based on boss direction
                player.takeDamage(1, direction);
            }
            return true;
        }, lowerX, lowerY, upperX, upperY);
    }
}
