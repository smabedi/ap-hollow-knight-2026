package com.smabedi.hollowknight.models.entities.spells;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class VengefulSpirit {
    public Body b2body;
    public boolean isDestroyed = false;
    public boolean setToDestroy = false;

    public VengefulSpirit(World world, float x, float y, boolean facingRight) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.KinematicBody; // Kinematic bodies ignore gravity!
        b2body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(Constants.Knight.SPRIT_WIDTH, Constants.Knight.SPRIT_HEIGHT);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // "Passes through enemies" as per the document

        // We pass 'this' instance so the ContactListener can identify it
        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        // Apply constant horizontal velocity
        float velocityX = facingRight ? Constants.Knight.SPRIT_SPEED : -Constants.Knight.SPRIT_SPEED;
        b2body.setLinearVelocity(velocityX, 0);
    }
}
