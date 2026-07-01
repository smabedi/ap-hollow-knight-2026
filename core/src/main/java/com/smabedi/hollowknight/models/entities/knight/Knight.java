package com.smabedi.hollowknight.models.entities.knight;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class Knight {
    public World world;
    public Body b2body;
    public boolean isGrounded = false;
    public boolean canDoubleJump = false;
    public boolean isJumping = false;
    public float pogoDurationTimer = 0f;
    public boolean canDash = true;
    public boolean isDashing = false;
    public float dashTimer = 0f;
    public boolean facingRight = true;
    public float dashCooldownTimer = 0f;
    public boolean isTouchingLeftWall = false;
    public boolean isTouchingRightWall = false;
    public int health = Constants.Knight.MAX_HEALTH;
    public float iFrameTimer = 0f;
    public boolean isDead = false;
    public int soul = 0;
    public boolean isFocusing = false;
    public float focusTimer = 0f;

    public Knight(World world, float startX, float startY) {
        this.world = world;
        defineKnight(startX, startY);
    }

    private void defineKnight(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Constants.World.PPM, y / Constants.World.PPM);

        b2body = world.createBody(bodyDef);
        b2body.setFixedRotation(true);
        PolygonShape body = new PolygonShape();
        body.setAsBox(Constants.Knight.WIDTH_HALVED_SCALED, Constants.Knight.HEIGHT_HALVED_SCALED);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = body;
        fixtureDef.friction = Constants.Knight.FRICTION;
        fixtureDef.density = Constants.Knight.DENSITY;

        b2body.createFixture(fixtureDef).setUserData("knight");

        PolygonShape footSensor = new PolygonShape();
        footSensor.setAsBox(Constants.Knight.WIDTH_HALVED_SCALED * 0.9f,
            2 / Constants.World.PPM,
            new Vector2(0, -Constants.Knight.HEIGHT_HALVED_SCALED),
            0);

        fixtureDef.shape = footSensor;
        fixtureDef.isSensor = true; // It detects collisions but doesn't bump into things

        b2body.createFixture(fixtureDef).setUserData("foot_sensor");

        // Left wall sensor
        PolygonShape leftSensor = new PolygonShape();
        leftSensor.setAsBox(2 / Constants.World.PPM, Constants.Knight.HEIGHT_HALVED_SCALED,
            new Vector2(-Constants.Knight.WIDTH_HALVED_SCALED, 0), 0);

        FixtureDef leftDef = new FixtureDef();
        leftDef.shape = leftSensor;
        leftDef.isSensor = true;
        b2body.createFixture(leftDef).setUserData("left_sensor");

        // Right wall sensor
        PolygonShape rightSensor = new PolygonShape();
        rightSensor.setAsBox(2 / Constants.World.PPM, Constants.Knight.HEIGHT_HALVED_SCALED,
            new Vector2(Constants.Knight.WIDTH_HALVED_SCALED, 0), 0);

        FixtureDef rightDef = new FixtureDef();
        rightDef.shape = rightSensor;
        rightDef.isSensor = true;
        b2body.createFixture(rightDef).setUserData("right_sensor");

        // Clean up shapes memory
        body.dispose();
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
        if (iFrameTimer > 0 || isDead) return;

        health -= amount;

        // Interrupt focus if we get hit!
        isFocusing = false;
        focusTimer = 0f;

        if (health <= 0) {
            health = 0;
            isDead = true;
            System.out.println("Knight has died!");
            // TODO: Trigger respawn logic later
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
