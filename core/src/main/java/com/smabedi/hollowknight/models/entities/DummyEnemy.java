package com.smabedi.hollowknight.models.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class DummyEnemy implements IDamageable {
    public Body b2body;
    private int hp = 3;
    private boolean dead = false;

    public DummyEnemy(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x / Constants.World.PPM, y / Constants.World.PPM);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(30 / Constants.World.PPM, 40 / Constants.World.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void takeDamage(int amount) {
        hp -= amount;
        System.out.println("Dummy hit! HP left: " + hp);
        if (hp <= 0) die();
    }

    @Override
    public void applyKnockback(float dirX, float dirY) {
        b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
        b2body.applyLinearImpulse(new Vector2(dirX, dirY), b2body.getWorldCenter(), true);
    }

    @Override
    public void die() {
        dead = true;
        System.out.println("Dummy destroyed!");
        // TODO: Flag for Box2D body destruction in the main loop later
    }

    @Override
    public boolean isDead() {
        return dead;
    }
}
