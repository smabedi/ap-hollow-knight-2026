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
    public float nailDurationTimer = 0f;
    public boolean canDash = true;
    public boolean isDashing = false;
    public float dashTimer = 0f;
    public boolean facingRight = true;
    public float dashCooldownTimer = 0f;
    public boolean isTouchingLeftWall = false;
    public boolean isTouchingRightWall = false;

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
}
