package com.smabedi.hollowknight.models.entities.knight;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;

/**
 * The core data model representing the player character.
 * Encapsulates the Box2D physics body, health, soul, and ability state timers.
 * Leaves input processing to the PlayerController and animation handling to the KnightRenderer.
 */
public class Knight {
    private final EventCallback eventCallback;
    public World world;
    public Body b2body;
    public boolean facingRight = true;
    public int health;
    public boolean isDead = false;
    public boolean isGrounded = false;
    public boolean canDoubleJump = false;
    public boolean isJumping = false;
    public boolean isDoubleJumping = false;
    public float pogoDurationTimer = 0f;
    public boolean canDash = true;
    public boolean isDashing = false;
    public float dashTimer = 0f;
    public float dashCooldownTimer = 0f;
    public boolean isTouchingLeftWall = false;
    public boolean isTouchingRightWall = false;
    public float iFrameTimer = 0f;
    public int soul;
    public boolean isFocusing = false;
    public float focusTimer = 0f;
    public float wraithsTimer = 0f;
    public int wraithsTicksFired = 0;
    public float spritCastTimer = 0f;
    public float attackCooldownTimer = 0f;
    public float attackDurationTimer = 0f;
    public boolean isGodMode = false;
    public boolean emergencyHealArmed = false;
    public boolean isNoclip = false;
    public boolean hasSharpShadow = false;
    public boolean hasVoidHeart = false;
    public KnightState currentState = KnightState.IDLE;
    public KnightState previousState = KnightState.IDLE;
    public boolean isAttackingUp = false;
    public boolean isAttackingDown = false;
    public long walkLoopId = -1;
    public long wallSlideLoopId = -1;

    public Knight(World world, float startX, float startY, int currentHealth, int currentSoul, EventCallback eventCallback) {
        this.world = world;
        this.health = currentHealth;
        this.soul = currentSoul;
        this.eventCallback = eventCallback;
        defineKnight(startX, startY);
    }

    /**
     * Decrements active cooldowns and updates the current state classification.
     * Manages audio loop toggling based on state resolution.
     */
    public void update(float dt) {
        if (iFrameTimer > 0) iFrameTimer -= dt;
        if (attackDurationTimer > 0) attackDurationTimer -= dt;
        if (spritCastTimer > 0) spritCastTimer -= dt;
        if (wraithsTimer > 0) wraithsTimer -= dt;

        previousState = currentState;
        currentState = getState();

        if (currentState == KnightState.WALKING && !isDead) {
            if (walkLoopId == -1) {
                walkLoopId = AudioManager.loopSfx(Constants.Paths.Sounds.SFX_RUN);
            }
        } else if (walkLoopId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_RUN, walkLoopId);
            walkLoopId = -1;
        }

        if (currentState == KnightState.WALL_SLIDING && !isDead) {
            if (wallSlideLoopId == -1) {
                wallSlideLoopId = AudioManager.loopSfx(Constants.Paths.Sounds.SFX_WALL_SLIDE);
            }
        } else if (wallSlideLoopId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_WALL_SLIDE, wallSlideLoopId);
            wallSlideLoopId = -1;
        }
    }

    /**
     * Determines the most appropriate descriptive state for the Knight
     * based on vertical momentum, grounding, and active spell timers.
     */
    private KnightState getState() {
        if (attackDurationTimer > 0) {
            if (isAttackingDown) return KnightState.ATTACKING_DOWN;
            if (isAttackingUp) return KnightState.ATTACKING_UP;
            return KnightState.ATTACKING_SIDE;
        }

        if (spritCastTimer > 0) return hasVoidHeart ? KnightState.CASTING_VOID_SPIRIT : KnightState.CASTING_SPIRIT;
        if (wraithsTimer > 0) return hasVoidHeart ? KnightState.CASTING_VOID_WRAITHS : KnightState.CASTING_WRAITHS;

        if (isFocusing) return KnightState.FOCUSING;
        if (isDashing) return hasSharpShadow ? KnightState.SHADOW_DASHING : KnightState.DASHING;

        if (!isGrounded && b2body.getLinearVelocity().y <= 0 && (isTouchingLeftWall || isTouchingRightWall)) {
            return KnightState.WALL_SLIDING;
        }

        if (!isGrounded) {
            if (b2body.getLinearVelocity().y < 0) {
                isDoubleJumping = false;
                return KnightState.FALLING;
            } else if (isDoubleJumping) {
                return KnightState.DOUBLE_JUMPING;
            } else if (b2body.getLinearVelocity().y > 0) {
                return KnightState.JUMPING;
            }
        }

        if (b2body.getLinearVelocity().x != 0) return KnightState.WALKING;
        return KnightState.IDLE;
    }

    /**
     * Configures the primary Box2D rigid body alongside environmental sensors
     * used for wall sliding and grounded evaluations.
     */
    private void defineKnight(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        b2body = world.createBody(bodyDef);
        b2body.setFixedRotation(true);

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(Constants.Knight.WIDTH_HALVED_SCALED, Constants.Knight.HEIGHT_HALVED_SCALED);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = bodyShape;
        fixtureDef.friction = Constants.Knight.FRICTION;
        fixtureDef.density = Constants.Knight.DENSITY;

        b2body.createFixture(fixtureDef).setUserData("knight");

        PolygonShape footSensor = new PolygonShape();
        footSensor.setAsBox(
            Constants.Knight.WIDTH_HALVED_SCALED * 0.8f,
            Constants.Knight.SENSOR_WIDTH,
            new Vector2(0, -Constants.Knight.HEIGHT_HALVED_SCALED),
            0
        );
        fixtureDef.shape = footSensor;
        fixtureDef.isSensor = true;
        b2body.createFixture(fixtureDef).setUserData("foot_sensor");

        PolygonShape leftSensor = new PolygonShape();
        leftSensor.setAsBox(Constants.Knight.SENSOR_WIDTH, Constants.Knight.HEIGHT_HALVED_SCALED,
            new Vector2(-Constants.Knight.WIDTH_HALVED_SCALED, 0), 0);
        FixtureDef leftDef = new FixtureDef();
        leftDef.shape = leftSensor;
        leftDef.isSensor = true;
        b2body.createFixture(leftDef).setUserData("left_sensor");

        PolygonShape rightSensor = new PolygonShape();
        rightSensor.setAsBox(Constants.Knight.SENSOR_WIDTH, Constants.Knight.HEIGHT_HALVED_SCALED,
            new Vector2(Constants.Knight.WIDTH_HALVED_SCALED, 0), 0);
        FixtureDef rightDef = new FixtureDef();
        rightDef.shape = rightSensor;
        rightDef.isSensor = true;
        b2body.createFixture(rightDef).setUserData("right_sensor");

        bodyShape.dispose();
        footSensor.dispose();
        leftSensor.dispose();
        rightSensor.dispose();
    }

    /**
     * Grants soul resources to the Knight, capped at a maximum limit.
     */
    public void addSoul(int amount) {
        if (soul < Constants.Knight.MAX_SOUL) {
            AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_SOUL_PICKUP, 0.9f, 1.1f);
        } else {
            AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_SOUL_FULL, 0.9f, 1.1f);
        }

        soul += amount;
        if (soul > Constants.Knight.MAX_SOUL) {
            soul = Constants.Knight.MAX_SOUL;
        }
        System.out.println("Soul gained! Current Soul: " + soul);
    }

    /**
     * Recovers health masks for the Knight, triggering auditory and visual feedback.
     */
    public void heal(int amount) {
        health += amount;
        if (health > Constants.Knight.MAX_HEALTH) {
            health = Constants.Knight.MAX_HEALTH;
        }
        eventCallback.addCameraTrauma(0.5f);
        AudioManager.playSfx(Constants.Paths.Sounds.SFX_FOCUS);
        System.out.println("Healed! HP: " + health);
    }

    /**
     * Resolves incoming damage calculations, overriding with developer cheats or
     * evaluating lethal thresholds to flag death states.
     */
    public void takeDamage(int amount, float knockbackDirX) {
        if (isGodMode || isNoclip || iFrameTimer > 0 || isDead) return;

        if (health - amount <= 0 && emergencyHealArmed) {
            amount = health - 1;
            emergencyHealArmed = false;
            System.out.println("Emergency Heal prevented death! Taking knockback.");
        }

        eventCallback.setCameraTrauma(0.75f);
        health -= amount;

        isFocusing = false;
        focusTimer = 0f;

        if (health <= 0) {
            System.out.println("Knight has died!");

            if (walkLoopId != -1) {
                AudioManager.stopSfx(Constants.Paths.Sounds.SFX_RUN, walkLoopId);
                walkLoopId = -1;
            }

            if (wallSlideLoopId != -1) {
                AudioManager.stopSfx(Constants.Paths.Sounds.SFX_WALL_SLIDE, wallSlideLoopId);
                wallSlideLoopId = -1;
            }

            health = Constants.Knight.MAX_HEALTH;
            isDead = false;

            eventCallback.onPlayerDeath();
        } else {
            iFrameTimer = Constants.Knight.I_FRAME_DURATION;
            AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_DAMAGE, 0.85f, 1.15f);
            b2body.setLinearVelocity(0, 0);
            b2body.applyLinearImpulse(
                new Vector2(knockbackDirX * Constants.Knight.KNOCKBACK_FORCE_X, Constants.Knight.KNOCKBACK_FORCE_Y),
                b2body.getWorldCenter(),
                true
            );
            System.out.println("Knight took damage! HP: " + health);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCasting() {
        return spritCastTimer > 0 || wraithsTimer > 0;
    }
}
