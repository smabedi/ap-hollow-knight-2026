package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.IDamageable;
import com.smabedi.hollowknight.models.entities.enemies.Enemy;
import com.smabedi.hollowknight.models.entities.enemies.Shockwave;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.npcs.Zote;
import com.smabedi.hollowknight.models.entities.spells.VengefulSpirit;
import com.smabedi.hollowknight.models.inventory.CharmType;
import com.smabedi.hollowknight.models.inventory.Inventory;

public class WorldContactListener implements ContactListener {
    private final Knight player;
    private final Inventory inventory;

    public WorldContactListener(Knight player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
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

        handlePlayerDamage(fixA, fixB);
        handleSpellCollisions(fixA, fixB);
        handleShockwaveCollisions(fixA, fixB);
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

            // If it's an enemy, check if it's already dead, or it's the Zote so we don't take damage from them
            if (hazardData instanceof IDamageable) {
                if (((IDamageable) hazardData).isDead() || hazardData instanceof Zote) return;
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

    private void handleSpellCollisions(Fixture fixA, Fixture fixB) {
        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        boolean isAVSprit = dataA instanceof VengefulSpirit;
        boolean isBVSprit = dataB instanceof VengefulSpirit;

        if (isAVSprit || isBVSprit) {
            VengefulSpirit sprit = isAVSprit ? (VengefulSpirit) dataA : (VengefulSpirit) dataB;
            Fixture hazardFix = isAVSprit ? fixB : fixA;
            Object hazardData = hazardFix.getUserData();

            if ("ground".equals(hazardData)) {
                // Destroys itself on walls
                sprit.setToDestroy = true;
            } else if (hazardData instanceof IDamageable enemy) {
                // Damages enemies, but passes through them (does not destroy itself)
                if (!enemy.isDead()) {
                    // Apply Void Heart modifier
                    int spellDamage = inventory.isEquipped(CharmType.VOID_HEART) ? 2 : 1;
                    enemy.takeDamage(spellDamage);
                }
            }
        }
    }

    private void handleShockwaveCollisions(Fixture fixA, Fixture fixB) {
        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        boolean isAShockwave = dataA instanceof Shockwave;
        boolean isBShockwave = dataB instanceof Shockwave;

        if (isAShockwave || isBShockwave) {
            Shockwave shockwave = isAShockwave ? (Shockwave) dataA : (Shockwave) dataB;
            Fixture hazardFix = isAShockwave ? fixB : fixA;
            Object hazardData = hazardFix.getUserData();

            if ("knight".equals(hazardData)) {
                // Determine knockback direction based on which way the shockwave is traveling
                float knockbackDirX = shockwave.b2body.getLinearVelocity().x > 0 ? 1f : -1f;

                // Power Slam Shockwaves usually deal 2 damage (double a standard hit)
                player.takeDamage(2, knockbackDirX);
            } else if ("ground".equals(hazardData) || "spikes".equals(hazardData)) {
                // Destroy the shockwave if it hits a wall
                shockwave.setToDestroy = true;
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        boolean isAPlayer = "knight".equals(dataA);
        boolean isBPlayer = "knight".equals(dataB);

        boolean isAEnemy = dataA instanceof Enemy;
        boolean isBEnemy = dataB instanceof Enemy;

        boolean isAZote = dataA instanceof Zote;
        boolean isBZote = dataB instanceof Zote;

        // --- 1. Remove friction if the Knight is moving UP against a wall ---
        if ((isAPlayer && "ground".equals(dataB)) || (isBPlayer && "ground".equals(dataA))) {
            if (player.b2body.getLinearVelocity().y > 0) {
                contact.setFriction(0f);
            }
        }

        // --- 2. Player vs Enemy Collisions ---
        if ((isAPlayer && isBEnemy) || (isBPlayer && isAEnemy)) {
            Enemy enemy = isAEnemy ? (Enemy) dataA : (Enemy) dataB;

            // Phase through corpses OR phase through living enemies if we have I-Frames
            if (enemy.isDead() || player.iFrameTimer > 0) {
                contact.setEnabled(false);
            }
        }
        // --- 3. Enemy vs Enemy Collisions ---
        else if (isAEnemy && isBEnemy) {
            Enemy enemyA = (Enemy) dataA;
            Enemy enemyB = (Enemy) dataB;

            // If either bug is a corpse, disable the physical bump so they walk right through it!
            if (enemyA.isDead() || enemyB.isDead()) {
                contact.setEnabled(false);
            }
        }
        // --- 4. Zote collisions ---
        else if ((isAZote && isBPlayer) || (isBZote && isAPlayer)) {
            Zote zote = isAZote ? (Zote) dataA : (Zote) dataB;

            // Phase through zote if he's angry, running around
            if (zote.isAngry()) {
                contact.setEnabled(false);
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
