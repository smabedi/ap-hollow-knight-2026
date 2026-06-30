package com.smabedi.hollowknight.models.entities.knight;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class Knight {
    public World world;
    public Body b2body;
    public boolean isGrounded = false;

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
        body.setAsBox(Constants.Knight.WIDTH / 2f / Constants.World.PPM,
            Constants.Knight.HEIGHT / 2f / Constants.World.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = body;
        fixtureDef.friction = Constants.Knight.FRICTION;
        fixtureDef.density = Constants.Knight.DENSITY;

        b2body.createFixture(fixtureDef).setUserData("knight");

        PolygonShape foot = new PolygonShape();
        foot.setAsBox(Constants.Knight.WIDTH / 2f * 0.9f / Constants.World.PPM,
            2 / Constants.World.PPM,
            new Vector2(0, -Constants.Knight.HEIGHT / 2f / Constants.World.PPM),
            0);

        fixtureDef.shape = foot;
        fixtureDef.isSensor = true; // It detects collisions but doesn't bump into things

        b2body.createFixture(fixtureDef).setUserData("foot");
    }
}
