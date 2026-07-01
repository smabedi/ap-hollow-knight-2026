package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.IDamageable;
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

        // Ground and Wall Sensors
        if (isContact(fixA, fixB, "foot_sensor", "ground")) {
            player.isGrounded = true;
            player.canDoubleJump = true;
            player.canDash = true;
        }

        if (isContact(fixA, fixB, "left_sensor", "ground")) player.isTouchingLeftWall = true;
        if (isContact(fixA, fixB, "right_sensor", "ground")) player.isTouchingRightWall = true;

        // Player taking damage from Hazards/Enemies
        handlePlayerDamage(fixA, fixB);
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

    private void handlePlayerDamage(Fixture fixA, Fixture fixB) {
        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        boolean isAPlayer = "knight".equals(dataA);
        boolean isBPlayer = "knight".equals(dataB);

        // If neither fixture is the player's main body, ignore it.
        if (!isAPlayer && !isBPlayer) return;

        Fixture playerFixture = isAPlayer ? fixA : fixB;
        Fixture hazardFixture = isAPlayer ? fixB : fixA;
        Object hazardData = hazardFixture.getUserData();

        // Check if the hazard is spikes or an enemy
        if ("spikes".equals(hazardData) || hazardData instanceof IDamageable) {

            // If it's an enemy, check if it's already dead so we don't take damage from corpses
            if (hazardData instanceof IDamageable) {
                if (((IDamageable) hazardData).isDead()) return;
            }

            // Calculate knockback direction. If the hazard is to our right, we get knocked left (-1).
            float knockbackDirX = getKnockbackDirX(hazardFixture, playerFixture);
            player.takeDamage(1, knockbackDirX);

            // TODO: Implement safe position teleportation for Spikes.
        }
    }

    private static float getKnockbackDirX(Fixture hazardFixture, Fixture playerFixture) {
        float hazardX = hazardFixture.getBody().getPosition().x;
        float playerX = playerFixture.getBody().getPosition().x;
        float knockbackDirX;

        // If falling perfectly dead-center on a hazard, bounce backward based on facing direction
        if (Math.abs(playerX - hazardX) < Constants.Knight.WIDTH_HALVED_SCALED * 1.5f) {
            knockbackDirX = 0;
        } else {
            // Otherwise, bounce away from the hazard
            knockbackDirX = (playerX < hazardX) ? -1f : 1f;
        }
        return knockbackDirX;
    }

    private boolean isContact(Fixture a, Fixture b, String sensorUserData, String targetUserData) {
        boolean aIsSensor = sensorUserData.equals(a.getUserData());
        boolean bIsTarget = targetUserData.equals(b.getUserData());

        boolean bIsSensor = sensorUserData.equals(b.getUserData());
        boolean aIsTarget = targetUserData.equals(a.getUserData());

        return (aIsSensor && bIsTarget) || (bIsSensor && aIsTarget);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
