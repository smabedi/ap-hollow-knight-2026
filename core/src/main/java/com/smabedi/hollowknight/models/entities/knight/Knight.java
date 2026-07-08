package com.smabedi.hollowknight.models.entities.knight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;

public class Knight {
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
    public boolean isAttackingDown = false;

    public Knight(World world, float startX, float startY, int currentHealth, int currentSoul) {
        this.world = world;
        this.health = currentHealth;
        this.soul = currentSoul;
        defineKnight(startX, startY);
    }

    public void update(float dt) {
        // Decrease timers
        if (iFrameTimer > 0) iFrameTimer -= dt;
        if (attackDurationTimer > 0) attackDurationTimer -= dt;
        if (spritCastTimer > 0) spritCastTimer -= dt;
        if (wraithsTimer > 0) wraithsTimer -= dt;

        previousState = currentState;
        currentState = getState();
    }

    private KnightState getState() {
        if (attackDurationTimer > 0) {
            return isAttackingDown ? KnightState.ATTACKING_DOWN : KnightState.ATTACKING_SIDE;
        }

        if (spritCastTimer > 0) return hasVoidHeart ? KnightState.CASTING_VOID_SPIRIT : KnightState.CASTING_SPIRIT;
        if (wraithsTimer > 0) return hasVoidHeart ? KnightState.CASTING_VOID_WRAITHS : KnightState.CASTING_WRAITHS;

        // 2. State Triggers
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

    private void defineKnight(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        b2body = world.createBody(bodyDef);
        b2body.setFixedRotation(true);

        // Main Body Fixture
        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(Constants.Knight.WIDTH_HALVED_SCALED, Constants.Knight.HEIGHT_HALVED_SCALED);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = bodyShape;
        fixtureDef.friction = Constants.Knight.FRICTION;
        fixtureDef.density = Constants.Knight.DENSITY;

        b2body.createFixture(fixtureDef).setUserData("knight");

        // Foot Sensor (Detects Grounding)
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

        // Left wall sensor
        PolygonShape leftSensor = new PolygonShape();
        leftSensor.setAsBox(Constants.Knight.SENSOR_WIDTH, Constants.Knight.HEIGHT_HALVED_SCALED,
            new Vector2(-Constants.Knight.WIDTH_HALVED_SCALED, 0), 0);
        FixtureDef leftDef = new FixtureDef();
        leftDef.shape = leftSensor;
        leftDef.isSensor = true;
        b2body.createFixture(leftDef).setUserData("left_sensor");

        // Right wall sensor
        PolygonShape rightSensor = new PolygonShape();
        rightSensor.setAsBox(Constants.Knight.SENSOR_WIDTH, Constants.Knight.HEIGHT_HALVED_SCALED,
            new Vector2(Constants.Knight.WIDTH_HALVED_SCALED, 0), 0);
        FixtureDef rightDef = new FixtureDef();
        rightDef.shape = rightSensor;
        rightDef.isSensor = true;
        b2body.createFixture(rightDef).setUserData("right_sensor");

        // Memory cleanup
        bodyShape.dispose();
        footSensor.dispose();
        leftSensor.dispose();
        rightSensor.dispose();
    }

    public void addSoul(int amount) {
        soul += amount;
        if (soul > Constants.Knight.MAX_SOUL) {
            soul = Constants.Knight.MAX_SOUL;
        }
        System.out.println("Soul gained! Current Soul: " + soul);
    }

    public void heal(int amount) {
        health += amount;
        if (health > Constants.Knight.MAX_HEALTH) {
            health = Constants.Knight.MAX_HEALTH;
        }
        System.out.println("Healed! HP: " + health);
    }

    public void takeDamage(int amount, float knockbackDirX) {
        // Also intercept damages if Cheat is active
        if (isGodMode || isNoclip || iFrameTimer > 0 || isDead) return;

        // EMERGENCY HEAL SAFETY NET INTERCEPT
        if (health - amount <= 0 && emergencyHealArmed) {
            amount = health - 1; // This ensures health -= amount leaves exactly 1 HP
            emergencyHealArmed = false;
            System.out.println("Emergency Heal prevented death! Taking knockback.");
        }

        health -= amount;
        // Interrupt focus if we get hit!
        isFocusing = false;
        focusTimer = 0f;

        if (health <= 0) {
            health = 0;
            isDead = true;
            System.out.println("Knight has died!");
            // TODO: Trigger respawn logic later.
        } else {
            // Give 1 second of invincibility
            iFrameTimer = Constants.Knight.I_FRAME_DURATION;
            b2body.setLinearVelocity(0, 0);
            b2body.applyLinearImpulse(
                new Vector2(knockbackDirX * Constants.Knight.KNOCKBACK_FORCE_X, Constants.Knight.KNOCKBACK_FORCE_Y),
                b2body.getWorldCenter(),
                true
            );
            System.out.println("Knight took damage! HP: " + health);
        }
    }
}
