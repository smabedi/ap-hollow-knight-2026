package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.IDamageable;
import com.smabedi.hollowknight.models.entities.enemies.Enemy;
import com.smabedi.hollowknight.models.entities.items.Shockwave;
import com.smabedi.hollowknight.models.entities.items.VengefulSpirit;
import com.smabedi.hollowknight.models.entities.items.VfxType;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.npcs.Zote;
import com.smabedi.hollowknight.models.inventory.CharmType;
import com.smabedi.hollowknight.models.inventory.Inventory;

/**
 * Listens for and processes Box2D collision events across the active game world.
 * Enforces physics-based gameplay interactions, including damage resolution,
 * spell mechanics, arena triggers, and conditional fixture phasing.
 */
public class WorldContactListener implements ContactListener {
    private final Knight player;
    private final Inventory inventory;
    private final EventCallback eventCallback;
    private final GameSession session;

    public WorldContactListener(Knight player, Inventory inventory, GameSession session, EventCallback eventCallback) {
        this.player = player;
        this.inventory = inventory;
        this.eventCallback = eventCallback;
        this.session = session;
    }

    /**
     * Calculates the horizontal knockback trajectory for the player upon taking damage.
     *
     * @param hazardFixture The physical body applying the damage.
     * @param playerFixture The player's active body.
     * @return 1f for rightward trajectory, -1f for leftward, or 0f for neutral vertical drops.
     */
    private static float getKnockbackDirX(Fixture hazardFixture, Fixture playerFixture) {
        float hazardX = hazardFixture.getBody().getPosition().x;
        float playerX = playerFixture.getBody().getPosition().x;
        float knockbackDirX;

        if (Math.abs(playerX - hazardX) < Constants.Knight.WIDTH_HALVED_SCALED * 1.5f) {
            knockbackDirX = 0;
        } else {
            knockbackDirX = (playerX < hazardX) ? -1f : 1f;
        }
        return knockbackDirX;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        // Check for Arena Lock Trigger
        if (isContact(fixA, fixB, "knight", "arena_sensor")) {
            if (!session.isArenaLocked) {
                session.isArenaLocked = true;
                System.out.println("Boss Arena Locked!");
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_BOSS_TRANSITION);
                AudioManager.playMusic(Constants.Paths.Sounds.BGM_BOSS, true);
            }
        }

        // Ground and Wall Sensors
        if (isContact(fixA, fixB, "foot_sensor", "ground")) {
            if (!player.isGrounded) {
                AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_LAND, 0.9f, 1.1f);
            }
            player.isGrounded = true;
            player.canDoubleJump = true;
            player.canDash = true;
        }

        if (isContact(fixA, fixB, "left_sensor", "ground")) player.isTouchingLeftWall = true;
        if (isContact(fixA, fixB, "right_sensor", "ground")) player.isTouchingRightWall = true;

        // Update last safe spot dynamically
        if (isContact(fixA, fixB, "knight", "safe_spot")) {
            Fixture safeSpotFix = "safe_spot".equals(fixA.getUserData()) ? fixA : fixB;

            float newX = safeSpotFix.getBody().getPosition().x;
            float newY = safeSpotFix.getBody().getPosition().y;

            // Enforce small epsilon margin to prevent floating point instability triggers
            if (Math.abs(session.lastSafeX - newX) > 0.1f || Math.abs(session.lastSafeY - newY) > 0.1f) {
                session.lastSafeX = newX;
                session.lastSafeY = newY;
                session.safeSpotUpdated = true;
            }
        }

        // Map Transition Trigger
        if (isContact(fixA, fixB, "knight", "transition_sensor")) {
            Fixture transitionFix = "transition_sensor".equals(fixA.getUserData()) ? fixA : fixB;
            TransitionData data = (TransitionData) transitionFix.getBody().getUserData();

            session.pendingTransition = true;
            session.nextLocation = data.targetLocation;
        }

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

    /**
     * Resolves incoming damage directed at the player, evaluating immunities and hazards.
     */
    private void handlePlayerDamage(Fixture fixA, Fixture fixB) {
        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        boolean isAPlayer = "knight".equals(dataA);
        boolean isBPlayer = "knight".equals(dataB);

        if (!isAPlayer && !isBPlayer) return;

        Fixture playerFixture = isAPlayer ? fixA : fixB;
        Fixture hazardFixture = isAPlayer ? fixB : fixA;
        Object hazardData = hazardFixture.getUserData();

        if ("spikes".equals(hazardData) || hazardData instanceof IDamageable) {

            if (hazardData instanceof IDamageable) {
                if (((IDamageable) hazardData).isDead() || hazardData instanceof Zote) return;
            }

            float knockbackDirX = getKnockbackDirX(hazardFixture, playerFixture);
            player.takeDamage(1, knockbackDirX);
            eventCallback.spawnStaticVfx(VfxType.DAMAGE, playerFixture.getBody().getPosition().x, playerFixture.getBody().getPosition().y, 0, 0, true, true);

            if ("spikes".equals(hazardData)) {
                session.pendingRespawn = true;
            }
        }
    }

    /**
     * Helper method to verify target pairings during collision mapping.
     */
    private boolean isContact(Fixture a, Fixture b, String sensorUserData, String targetUserData) {
        boolean aIsSensor = sensorUserData.equals(a.getUserData());
        boolean bIsTarget = targetUserData.equals(b.getUserData());

        boolean bIsSensor = sensorUserData.equals(b.getUserData());
        boolean aIsTarget = targetUserData.equals(a.getUserData());

        return (aIsSensor && bIsTarget) || (bIsSensor && aIsTarget);
    }

    /**
     * Governs the interactions of dynamic projectile spells against world geometry and entities.
     */
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
                sprit.setToDestroy = true;
            } else if (hazardData instanceof IDamageable enemy) {
                if (!enemy.isDead()) {
                    int spellDamage = inventory.isEquipped(CharmType.VOID_HEART) ? 2 : 1;
                    enemy.takeDamage(spellDamage);
                    eventCallback.spawnStaticVfx(VfxType.DAMAGE, hazardFix.getBody().getPosition().x, hazardFix.getBody().getPosition().y, 0, 0, true, true);
                }
            }
        }
    }

    /**
     * Resolves interactions specifically involving False Knight shockwave spawns.
     */
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
                float knockbackDirX = shockwave.b2body.getLinearVelocity().x > 0 ? 1f : -1f;
                player.takeDamage(2, knockbackDirX);
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

        // Strip friction interactions when the Knight is traveling vertically against a wall
        if ((isAPlayer && "ground".equals(dataB)) || (isBPlayer && "ground".equals(dataA))) {
            if (player.b2body.getLinearVelocity().y > 0) {
                contact.setFriction(0f);
            }
        }

        // Disable standard physical collision against corpses and during I-frames
        if ((isAPlayer && isBEnemy) || (isBPlayer && isAEnemy)) {
            Enemy enemy = isAEnemy ? (Enemy) dataA : (Enemy) dataB;

            if (enemy.isDead() || player.iFrameTimer > 0) {
                contact.setEnabled(false);
            }
        }
        else if (isAEnemy && isBEnemy) {
            Enemy enemyA = (Enemy) dataA;
            Enemy enemyB = (Enemy) dataB;

            if (enemyA.isDead() || enemyB.isDead()) {
                contact.setEnabled(false);
            }
        }
        else if ((isAZote && isBPlayer) || (isBZote && isAPlayer)) {
            Zote zote = isAZote ? (Zote) dataA : (Zote) dataB;

            if (zote.isAngry()) {
                contact.setEnabled(false);
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
