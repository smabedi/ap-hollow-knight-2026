package com.smabedi.hollowknight.models.entities.items;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class Shockwave {
    public Body b2body;
    public boolean isDestroyed = false;
    public boolean setToDestroy = false;

    public Shockwave(World world, float x, float y, float direction) {
        BodyDef bodyDef = new BodyDef();
        // Spawning slightly offset from the center of the impact
        bodyDef.position.set(x + (direction * 0.5f), y - (Constants.FalseKnight.HEIGHT_HALVED_SCALED * 0.8f));
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        b2body = world.createBody(bodyDef);
        b2body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        // A low, wide rectangular hitbox for the shockwave
        shape.setAsBox(Constants.FalseKnight.ShockWave.WIDTH, Constants.FalseKnight.ShockWave.HEIGHT);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // Passes through walls, only triggers on the player

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        // Apply constant horizontal velocity
        float shockwaveSpeed = Constants.FalseKnight.ShockWave.SPEED;
        b2body.setLinearVelocity(direction * shockwaveSpeed, 0);
    }
}
