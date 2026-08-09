package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.items.Shockwave;
import com.smabedi.hollowknight.models.entities.items.VfxType;
import com.smabedi.hollowknight.models.entities.knight.Knight;

/**
 * The primary boss entity.
 * Implements a complex, hierarchical finite state machine (Phase -> Move -> SubState)
 * to govern combat behaviors, utilizing a weighted pseudo-random decision matrix
 * that actively adapts to the player's proximity and recent damage outputs.
 */
public class FalseKnight extends Enemy {
    private final EventCallback eventCallback;
    public boolean facingRight = false;
    public boolean isActive = false;
    private BossPhase currentPhase = BossPhase.PHASE_1;
    private BossMove currentMove = BossMove.IDLE;
    private BossMove lastMove = BossMove.IDLE;
    private BossSubState currentSubState = BossSubState.NONE;
    private float moveTimer = 0f;
    private float stunTimer = 0f;
    private float subStateTimer = 0f;
    private boolean actionExecuted = false;
    private int recentDamageCount = 0;
    private float damageTimer = 0f;
    private long runLoopId = -1;

    public FalseKnight(World world, float x, float y, EventCallback eventCallback) {
        super(world, x, y, Constants.FalseKnight.HP, eventCallback, EnemyType.FALSE_KNIGHT);
        this.eventCallback = eventCallback;
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

    /**
     * The primary operational loop. Validates arena locks before executing
     * memory degradation routines for sustained damage and delegating to the active state phase.
     */
    @Override
    public void update(float dt, Knight player) {
        if (dead) return;

        if (!isActive) {
            b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
            return;
        }

        // Gradually decay the boss's memory of recent damage to reset dynamic defensive behaviors
        if (damageTimer > 0) {
            damageTimer -= dt;
            if (damageTimer <= 0) recentDamageCount = 0;
        }

        if (currentPhase == BossPhase.STUNNED) {
            handleStunPhase(dt);
            return;
        }

        if (currentMove == BossMove.IDLE) {
            facingRight = player.b2body.getPosition().x > b2body.getPosition().x;
            subStateTimer = 0f;
        }

        moveTimer -= dt;
        if (currentMove == BossMove.IDLE && moveTimer <= 0) {
            decideNextMove(player);
        } else if (currentMove != BossMove.IDLE) {
            executeCurrentMove(dt, player);
        }
    }

    /**
     * Resolves the vulnerable stun sequence. Modulates Box2D physics to halt momentum
     * and manages the transition logic to elevate the combat phase to PHASE_2 upon recovery.
     */
    private void handleStunPhase(float dt) {
        stunTimer -= dt;
        b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

        if (currentSubState == BossSubState.STUN_HIT) {
            subStateTimer -= dt;
            if (subStateTimer <= 0) currentSubState = BossSubState.NONE;
        }

        if (stunTimer <= 0 && currentSubState != BossSubState.RECOVERY) {
            currentSubState = BossSubState.RECOVERY;
            subStateTimer = 1.5f;
        } else if (currentSubState == BossSubState.RECOVERY) {
            subStateTimer -= dt;
            if (subStateTimer <= 0) {
                currentPhase = BossPhase.PHASE_2;
                currentMove = BossMove.IDLE;
                currentSubState = BossSubState.NONE;
                moveTimer = 0.5f;
            }
        }
    }

    /**
     * Executes the weighted pseudo-random decision matrix to select the next combat maneuver.
     * Implements a safety net to prevent infinite while-loops during identical subsequent rolls.
     */
    private void decideNextMove(Knight player) {
        float distance = Math.abs(player.b2body.getPosition().x - b2body.getPosition().x);
        boolean tookRapidDamage = (recentDamageCount >= 3 && damageTimer > 0);

        BossMove nextMove = BossMove.IDLE;
        int safetyNet = 0;

        do {
            if (tookRapidDamage && distance < 4f) {
                nextMove = BossMove.DEFENSIVE_LEAP;
            } else if (distance > 5f) {
                nextMove = MathUtils.randomBoolean(0.6f) ? BossMove.CHARGE : BossMove.OFFENSIVE_LEAP;
            } else {
                if (currentPhase == BossPhase.PHASE_2) {
                    float rand = MathUtils.random();
                    if (rand < 0.3f) nextMove = BossMove.POWER_SLAM;
                    else if (rand < 0.85f) nextMove = BossMove.MACE_SLAM;
                    else nextMove = BossMove.DEFENSIVE_LEAP;
                } else {
                    nextMove = MathUtils.randomBoolean(0.85f) ? BossMove.MACE_SLAM : BossMove.DEFENSIVE_LEAP;
                }
            }
            safetyNet++;
        } while (nextMove == lastMove && safetyNet < 5);

        if (nextMove == BossMove.DEFENSIVE_LEAP) recentDamageCount = 0;

        currentMove = nextMove;
        lastMove = currentMove;

        currentSubState = BossSubState.WIND_UP;
        subStateTimer = (currentPhase == BossPhase.PHASE_2) ? 0.3f : 0.6f;
        actionExecuted = false;
    }

    /**
     * Evaluates and steps the active micro-state (wind-up, execution, recovery) of the current combat maneuver.
     * Enforces tight synchronization between the Box2D engine and the auditory feedback system.
     */
    private void executeCurrentMove(float dt, Knight player) {
        float direction = facingRight ? 1f : -1f;

        switch (currentMove) {
            case MACE_SLAM:
            case POWER_SLAM:
                if (currentSubState == BossSubState.WIND_UP) {
                    subStateTimer -= dt;
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                    if (subStateTimer <= 0) {
                        // Directly overwrite the linear velocity to bypass the boss's massive Box2D weight density
                        b2body.setLinearVelocity(direction * 3f, 6f);
                        currentSubState = BossSubState.ACTIVE;
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_ROAR, 0.9f, 1.1f);
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_SWING, 0.9f, 1.1f);
                    }
                } else if (currentSubState == BossSubState.ACTIVE) {
                    if (isGrounded() && b2body.getLinearVelocity().y <= 0.1f) {
                        currentSubState = BossSubState.ATTACK;
                        subStateTimer = 0.3f;

                        if (!actionExecuted) {
                            if (currentMove == BossMove.POWER_SLAM) {
                                AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_POWER_STRIKE);
                            } else {
                                AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_STRIKE, 0.9f, 1.1f);
                            }

                            executeMaceHitbox(player, direction);

                            // Instantiate dynamic physical projectiles traversing outward from the impact epicenter
                            if (currentMove == BossMove.POWER_SLAM) {
                                Vector2 pos = b2body.getPosition();
                                new Shockwave(world, pos.x, pos.y, 1);
                                new Shockwave(world, pos.x, pos.y, -1);
                            }
                            actionExecuted = true;
                        }
                    }
                } else if (currentSubState == BossSubState.ATTACK) {
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        currentSubState = BossSubState.RECOVERY;
                        subStateTimer = (currentPhase == BossPhase.PHASE_2) ? 0.4f : 0.8f;
                    }
                } else if (currentSubState == BossSubState.RECOVERY) {
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        currentMove = BossMove.IDLE;
                        moveTimer = (currentPhase == BossPhase.PHASE_2) ? 0.5f : 1f;
                    }
                }
                break;

            case OFFENSIVE_LEAP:
            case DEFENSIVE_LEAP:
                if (currentSubState == BossSubState.WIND_UP) {
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        float leapDir = currentMove == BossMove.OFFENSIVE_LEAP ? direction : -direction;
                        b2body.setLinearVelocity(leapDir * 3f, 6f);
                        currentSubState = BossSubState.ACTIVE;
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_JUMP, 0.9f, 1.1f);
                    }
                } else if (currentSubState == BossSubState.ACTIVE) {
                    if (isGrounded() && b2body.getLinearVelocity().y <= 0.1f) {
                        currentSubState = BossSubState.RECOVERY;
                        subStateTimer = (currentPhase == BossPhase.PHASE_2) ? 0.25f : 0.5f;
                        eventCallback.addCameraTrauma(0.75f);
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_LAND, 0.85f, 1.15f);
                    }
                } else if (currentSubState == BossSubState.RECOVERY) {
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        currentMove = BossMove.IDLE;
                        moveTimer = (currentPhase == BossPhase.PHASE_2) ? 0.2f : 0.5f;
                    }
                }
                break;

            case CHARGE:
                if (currentSubState == BossSubState.WIND_UP) {
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        currentSubState = BossSubState.ACTIVE;
                        subStateTimer = 2.5f;

                        if (runLoopId == -1) {
                            runLoopId = AudioManager.loopSpatialSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 30f);
                        }
                    }
                } else if (currentSubState == BossSubState.ACTIVE) {
                    b2body.setLinearVelocity(direction * (currentPhase == BossPhase.PHASE_2 ? 7f : 4f), b2body.getLinearVelocity().y);
                    subStateTimer -= dt;

                    if (runLoopId != -1) {
                        AudioManager.updateSpatialSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, runLoopId, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 30f);
                    }

                    float dist = Math.abs(player.b2body.getPosition().x - b2body.getPosition().x);
                    eventCallback.setCameraTrauma(0.5f);

                    // Interrupt the charge loop if the target is reached, the timeout expires, or the boss collides with environmental geometry
                    if (dist < 2f || subStateTimer <= 0 || Math.abs(b2body.getLinearVelocity().x) < 0.5f) {
                        currentSubState = BossSubState.RECOVERY;
                        subStateTimer = (currentPhase == BossPhase.PHASE_2) ? 0.3f : 0.6f;
                        if (runLoopId != -1) {
                            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, runLoopId);
                            runLoopId = -1;
                        }
                    }
                } else if (currentSubState == BossSubState.RECOVERY) {
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        currentMove = BossMove.IDLE;
                        moveTimer = 1f;
                    }
                }
                break;
        }
    }

    public BossPhase getCurrentPhase() {
        return currentPhase;
    }

    public BossMove getCurrentMove() {
        return currentMove;
    }

    public BossSubState getCurrentSubState() {
        return currentSubState;
    }

    /**
     * Executes an instantaneous downward raycast to determine physical grounding status.
     * Prevents false-positives by terminating the query immediately upon locating valid terrain.
     */
    private boolean isGrounded() {
        Vector2 pos = b2body.getPosition();
        final boolean[] grounded = {false};

        world.rayCast((fixture, _, _, _) -> {
            if ("ground".equals(fixture.getUserData())) {
                grounded[0] = true;
                return 0;
            }
            return 1;
        }, pos, new Vector2(pos.x, pos.y - Constants.FalseKnight.HEIGHT_HALVED_SCALED - 0.2f));

        return grounded[0];
    }

    @Override
    public void takeDamage(int amount) {
        if (dead || !isActive) return;

        hp -= amount;
        recentDamageCount++;
        damageTimer = 2f;

        System.out.println("False Knight hit! HP: " + hp);

        if (currentPhase == BossPhase.STUNNED) {
            currentSubState = BossSubState.STUN_HIT;
            subStateTimer = 0.2f;
            AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_STUN_HIT, 0.9f, 1.15f);
        } else {
            AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_ARMOR_HIT, 0.8f, 1.2f);
        }

        if (hp <= 0) {
            die();
        } else if (hp <= maxHp / 2 && currentPhase == BossPhase.PHASE_1) {
            currentPhase = BossPhase.STUNNED;
            stunTimer = 4f;
            currentMove = BossMove.IDLE;
            currentSubState = BossSubState.NONE;
            actionExecuted = false;

            AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_OPEN_ARMOR_HIT);
            if (runLoopId != -1) {
                AudioManager.stopSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, runLoopId);
                runLoopId = -1;
            }
            System.out.println("False Knight Stunned! Armor is open!");
        }
    }

    /**
     * Validates spatial intersections during heavy attack maneuvers utilizing a Box2D Axis-Aligned Bounding Box (AABB) query.
     */
    private void executeMaceHitbox(Knight player, float direction) {
        Vector2 center = b2body.getWorldCenter();

        float slamReachX = 3f;
        float slamHeightY = 1.5f;

        float lowerX = direction == 1f ? center.x : center.x - slamReachX;
        float upperX = direction == 1f ? center.x + slamReachX : center.x;
        float lowerY = center.y - slamHeightY;
        float upperY = center.y + slamHeightY;

        world.QueryAABB(fixture -> {
            if ("knight".equals(fixture.getUserData())) {
                player.takeDamage(1, direction);
                Vector2 playerPos = player.b2body.getPosition();
                eventCallback.spawnStaticVfx(VfxType.DAMAGE, playerPos.x, playerPos.y, 0, 0, player.facingRight, true);
            }
            return true;
        }, lowerX, lowerY, upperX, upperY);

        eventCallback.addCameraTrauma(0.75f);
        if (currentMove == BossMove.POWER_SLAM) {
            eventCallback.addCameraTrauma(0.75f);
        }
    }

    @Override
    public void die() {
        super.die();
        AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_OPEN_ARMOR_HIT);
        AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_ROAR);

        if (runLoopId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, runLoopId);
            runLoopId = -1;
        }

        eventCallback.onBossDeath();
    }
}
