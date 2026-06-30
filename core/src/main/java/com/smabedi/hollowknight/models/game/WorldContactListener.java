package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class WorldContactListener implements ContactListener {
    private final Knight player;

    public WorldContactListener(Knight player) {
        this.player = player;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if (isContact(fixA, fixB, "foot_sensor", "ground")) {
            player.isGrounded = true;
            player.canDoubleJump = true;
            player.canDash = true;
        }

        if (isContact(fixA, fixB, "left_sensor", "ground")) player.isTouchingLeftWall = true;
        if (isContact(fixA, fixB, "right_sensor", "ground")) player.isTouchingRightWall = true;
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if (isContact(fixA, fixB, "foot_sensor", "ground")) {
            player.isGrounded = false;
        }

        if (isContact(fixA, fixB, "left_sensor", "ground")) player.isTouchingLeftWall = false;
        if (isContact(fixA, fixB, "right_sensor", "ground")) player.isTouchingRightWall = false;
    }

    private boolean isContact(Fixture a, Fixture b, String sensorUserData, String targetUserData) {
        boolean aIsSensor = sensorUserData.equals(a.getUserData());
        boolean bIsTarget = targetUserData.equals(b.getUserData());

        boolean bIsSensor = sensorUserData.equals(b.getUserData());
        boolean aIsTarget = targetUserData.equals(a.getUserData());

        return (aIsSensor && bIsTarget) || (bIsSensor && aIsTarget);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
