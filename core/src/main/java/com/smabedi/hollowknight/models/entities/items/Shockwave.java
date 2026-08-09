package com.smabedi.hollowknight.models.entities.items;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

/**
 * Represents the physical shockwave projectile emitted by the False Knight's power attacks.
 * Modeled as a Box2D KinematicBody to ensure consistent horizontal velocity while ignoring gravity.
 */
public class Shockwave {
    public Body b2body;
    public float stateTimer = 0;
    public boolean isDestroyed = false;
    public boolean setToDestroy = false;

    /**
     * Initializes the shockwave physical body and sets its initial velocity.
     *
     * @param world     The active Box2D physics world.
     * @param x         The origin X coordinate.
     * @param y         The origin Y coordinate.
     * @param direction The horizontal direction vector (1f for right, -1f for left).
     */
    public Shockwave(World world, float x, float y, float direction) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x + (direction * 0.5f), y - (Constants.FalseKnight.HEIGHT_HALVED_SCALED * 0.8f));
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        b2body = world.createBody(bodyDef);
        b2body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(Constants.FalseKnight.ShockWave.WIDTH, Constants.FalseKnight.ShockWave.HEIGHT);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        float shockwaveSpeed = Constants.FalseKnight.ShockWave.SPEED;
        b2body.setLinearVelocity(direction * shockwaveSpeed, 0);
    }
}
