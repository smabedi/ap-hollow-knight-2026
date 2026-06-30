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

        if (isFootContact(fixA, fixB, "ground")) {
            player.isGrounded = true;
        }

        // TODO: Add an if statement here for "spikes" to trigger respawns.
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if (isFootContact(fixA, fixB, "ground")) {
            player.isGrounded = false;
        }
    }

    private boolean isFootContact(Fixture a, Fixture b, String targetUserData) {
        boolean aIsFoot = "foot".equals(a.getUserData());
        boolean bIsTarget = targetUserData.equals(b.getUserData());

        boolean bIsFoot = "foot".equals(b.getUserData());
        boolean aIsTarget = targetUserData.equals(a.getUserData());

        return (aIsFoot && bIsTarget) || (bIsFoot && aIsTarget);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
