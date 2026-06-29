package com.smabedi.hollowknight.models.entities.knight;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class Knight {
    public World world;
    public Body b2body;

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
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(6 / Constants.World.PPM, 12 / Constants.World.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.2f;
        fixtureDef.density = 1.0f;

        b2body.createFixture(fixtureDef);
    }
}
