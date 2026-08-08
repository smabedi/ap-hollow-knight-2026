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
    public boolean facingRight = false;
    private final EventCallback eventCallback;
    private long runLoopId = -1;
    public boolean isActive = false;

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

    @Override
    public void update(float dt, Knight player) {
        if (dead) return;

        // Do nothing until the arena is locked
        if (!isActive) {
            b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
            return;
        }

        // Manage rapid-damage memory
        if (damageTimer > 0) {
            damageTimer -= dt;
            if (damageTimer <= 0) recentDamageCount = 0;
        }

        // 1. Stun Phase Logic
        if (currentPhase == BossPhase.STUNNED) {
            handleStunPhase(dt);
            return;
        }

        // 2. Face the player if idling
        if (currentMove == BossMove.IDLE) {
            facingRight = player.b2body.getPosition().x > b2body.getPosition().x;
            subStateTimer = 0f;
        }

        // 3. Action Logic
        moveTimer -= dt;
        if (currentMove == BossMove.IDLE && moveTimer <= 0) {
            decideNextMove(player);
        } else if (currentMove != BossMove.IDLE) {
            executeCurrentMove(dt, player);
        }
    }

    private void handleStunPhase(float dt) {
        stunTimer -= dt;
        b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);

        if (currentSubState == BossSubState.STUN_HIT) {
            subStateTimer -= dt;
            if (subStateTimer <= 0) currentSubState = BossSubState.NONE; // Return to stun idle
        }

        if (stunTimer <= 0 && currentSubState != BossSubState.RECOVERY) {
            currentSubState = BossSubState.RECOVERY; // Trigger stun_recover animation
            subStateTimer = 1.5f; // Wait for the recover animation to finish
//            AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_ROAR);
        } else if (currentSubState == BossSubState.RECOVERY) {
            subStateTimer -= dt;
            if (subStateTimer <= 0) {
                currentPhase = BossPhase.PHASE_2;
                currentMove = BossMove.IDLE;
                currentSubState = BossSubState.NONE;
                // Faster recovery after charging
                moveTimer = 0.5f;
            }
        }
    }

    private void decideNextMove(Knight player) {
        float distance = Math.abs(player.b2body.getPosition().x - b2body.getPosition().x);
        boolean tookRapidDamage = (recentDamageCount >= 3 && damageTimer > 0);

        BossMove nextMove = BossMove.IDLE;
        int safetyNet = 0; // Prevents an infinite loop if conditions force a specific move

        // DO-WHILE LOOP: Force a re-roll if it picks the last move
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
        lastMove = currentMove; // PROPERLY ASSIGN lastMove!

        currentSubState = BossSubState.WIND_UP;
        subStateTimer = (currentPhase == BossPhase.PHASE_2) ? 0.3f : 0.6f;
        actionExecuted = false;
    }

    private void executeCurrentMove(float dt, Knight player) {
        float direction = facingRight ? 1f : -1f;

        switch (currentMove) {
            case MACE_SLAM:
            case POWER_SLAM:
                if (currentSubState == BossSubState.WIND_UP) {
                    subStateTimer -= dt;
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
                    if (subStateTimer <= 0) {
                        // FIX: Use setLinearVelocity to ignore the boss's massive weight!
                        b2body.setLinearVelocity(direction * 3f, 6f);
                        currentSubState = BossSubState.ACTIVE;
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_ROAR, 0.9f, 1.1f); // Swing liftoff
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_SWING, 0.9f, 1.1f); // Swing liftoff
                    }
                } else if (currentSubState == BossSubState.ACTIVE) {
                    // Waiting to hit the ground. Calling the METHOD isGrounded() now!
                    if (isGrounded() && b2body.getLinearVelocity().y <= 0.1f) {
                        currentSubState = BossSubState.ATTACK; // Smashing the ground
                        subStateTimer = 0.3f; // duration of the smash hit frame
                        if (!actionExecuted) {
                            // Audio: Differentiate the impacts
                            if (currentMove == BossMove.POWER_SLAM) {
                                AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_POWER_STRIKE);
                            } else {
                                AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_STRIKE, 0.9f, 1.1f);
                            }

                            executeMaceHitbox(player, direction); // Executes the QAABB!

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
                        // Halve the breathing room between attacks in Phase 2
                        moveTimer = (currentPhase == BossPhase.PHASE_2) ? 0.5f : 1f;
                    }
                }
                break;

            case OFFENSIVE_LEAP:
            case DEFENSIVE_LEAP:
                if (currentSubState == BossSubState.WIND_UP) {
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y); // Plant feet before leaping
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        float leapDir = currentMove == BossMove.OFFENSIVE_LEAP ? direction : -direction;
                        b2body.setLinearVelocity(leapDir * 3f, 6f);
                        currentSubState = BossSubState.ACTIVE;
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_JUMP, 0.9f, 1.1f); // Jump liftoff
                    }
                } else if (currentSubState == BossSubState.ACTIVE) {
                    // Use the method here too!
                    if (isGrounded() && b2body.getLinearVelocity().y <= 0.1f) {
                        currentSubState = BossSubState.RECOVERY;
                        subStateTimer = (currentPhase == BossPhase.PHASE_2) ? 0.25f : 0.5f;
                        eventCallback.addCameraTrauma(0.75f);
                        AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_FK_LAND, 0.85f, 1.15f); // Heavy Landing
                    }
                } else if (currentSubState == BossSubState.RECOVERY) {
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y); // Plant feet upon landing
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        currentMove = BossMove.IDLE;
                        // Almost instant recovery after leaping in Phase 2
                        moveTimer = (currentPhase == BossPhase.PHASE_2) ? 0.2f : 0.5f;
                    }
                }
                break;

            case CHARGE:
                if (currentSubState == BossSubState.WIND_UP) {
                    b2body.setLinearVelocity(0, b2body.getLinearVelocity().y); // Plant feet before running
                    subStateTimer -= dt;
                    if (subStateTimer <= 0) {
                        currentSubState = BossSubState.ACTIVE;
                        subStateTimer = 2.5f; // ADDED: Hard timeout so he doesn't run forever!

                        // Audio: Start the looping charge sound
                        if (runLoopId == -1) {
                            runLoopId = AudioManager.loopSpatialSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 30f);
                        }
                    }
                } else if (currentSubState == BossSubState.ACTIVE) {
                    // Almost double the running speed in Phase 2
                    b2body.setLinearVelocity(direction * (currentPhase == BossPhase.PHASE_2 ? 7f : 4f), b2body.getLinearVelocity().y);
                    subStateTimer -= dt; // Decrement timeout

                    // Audio: Update the spatial panning while he runs!
                    if (runLoopId != -1) {
                        AudioManager.updateSpatialSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, runLoopId, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 30f);
                    }

                    float dist = Math.abs(player.b2body.getPosition().x - b2body.getPosition().x);
                    eventCallback.setCameraTrauma(0.5f);

                    // ADDED: Stop if he reaches you, OR if the timer runs out, OR if he hits a wall (x velocity drops)
                    if (dist < 2f || subStateTimer <= 0 || Math.abs(b2body.getLinearVelocity().x) < 0.5f) {
                        currentSubState = BossSubState.RECOVERY;
                        subStateTimer = (currentPhase == BossPhase.PHASE_2) ? 0.3f : 0.6f;
                        // Audio: End the loop upon crashing or stopping
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

    private boolean isGrounded() {
        Vector2 pos = b2body.getPosition();
        final boolean[] grounded = {false};

        // Shoot a raycast from the boss's center to slightly below its feet
        world.rayCast((fixture, _, _, _) -> {
            if ("ground".equals(fixture.getUserData())) {
                grounded[0] = true;
                return 0; // Terminate query early, we found the ground
            }
            return 1; // Continue checking
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

        // Audio: Differentiate between armor hits and the exposed maggot
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
            // Stun Phase Shift (50% HP)
            currentPhase = BossPhase.STUNNED;
            stunTimer = 4f;
            currentMove = BossMove.IDLE;
            currentSubState = BossSubState.NONE;
            actionExecuted = false;

            // Audio: Armor breaks open! Stop his run loop instantly if he was charging.
            AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_OPEN_ARMOR_HIT);
            if (runLoopId != -1) {
                AudioManager.stopSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, runLoopId);
                runLoopId = -1;
            }
            System.out.println("False Knight Stunned! Armor is open!");
        }
    }

    private void executeMaceHitbox(Knight player, float direction) {
        Vector2 center = b2body.getWorldCenter();

        // Define the area of the slam in front of the boss
        float slamReachX = 3f;
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
                Vector2 playerPos = player.b2body.getPosition();
                eventCallback.spawnStaticVfx(VfxType.DAMAGE, playerPos.x, playerPos.y, 0, 0, player.facingRight, true);
            }
            return true;
        }, lowerX, lowerY, upperX, upperY);

        eventCallback.addCameraTrauma(0.75f);
        if (currentMove == BossMove.POWER_SLAM) {
            eventCallback.addCameraTrauma(0.75f); // Harder shake for Power Slam
        }
    }

    @Override
    public void die() {
        super.die();
        AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_OPEN_ARMOR_HIT);
        AudioManager.playSfx(Constants.Paths.Sounds.SFX_FK_ROAR);

        // Safety kill-switch for the run loop
        if (runLoopId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_FK_RUN_LOOP, runLoopId);
            runLoopId = -1;
        }

        eventCallback.onBossDeath();
    }
}
