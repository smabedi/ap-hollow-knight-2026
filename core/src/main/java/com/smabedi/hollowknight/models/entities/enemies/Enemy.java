package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.IDamageable;
import com.smabedi.hollowknight.models.entities.knight.Knight;

/**
 * Abstract base class for all hostile entities.
 * Encapsulates core Box2D physics integration, health management, and base combat interactions.
 */
public abstract class Enemy implements IDamageable {
    public Body b2body;
    protected World world;
    protected int maxHp;
    protected int hp;
    protected boolean dead;
    protected float startX;
    protected float startY;
    protected float respawnDistance = Constants.Enemy.RESPAWN_DISTANCE;
    protected float stunTimer = 0f;
    protected EventCallback eventCallback;
    protected EnemyType type;
    protected long loopSoundId = -1;

    public Enemy(World world, float x, float y, int maxHp, EventCallback eventCallback, EnemyType type) {
        this.world = world;
        this.startX = x;
        this.startY = y;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.dead = false;
        this.eventCallback = eventCallback;
        this.type = type;
    }

    /**
     * Defines the precise physical fixture geometry. Must be implemented by concrete subclasses.
     */
    public abstract void defineShape();

    /**
     * Initializes the dynamic rigid body within the Box2D simulation and delegates fixture creation.
     */
    public void define() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(startX, startY);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bodyDef);
        b2body.setFixedRotation(true);
        defineShape();
    }

    /**
     * Processes frame-by-frame AI logic and spatial evaluations.
     *
     * @param dt     The scaled delta time.
     * @param player A reference to the active player character for proximity and line-of-sight checks.
     */
    public abstract void update(float dt, Knight player);

    /**
     * Evaluates whether a collision fixture corresponds to impassable terrain or another active entity.
     */
    protected boolean hitEnd(Fixture fixture) {
        Object userData = fixture.getUserData();
        if ("spikes".equals(userData) || "ground".equals(userData)) {
            return true;
        }
        if (userData instanceof Enemy otherEnemy) {
            return otherEnemy != this && !otherEnemy.isDead();
        }
        return false;
    }

    /**
     * Verifies the player's spatial distance from the entity's original spawn coordinates
     * to dynamically trigger a respawn if the entity is currently dead.
     */
    protected void checkRespawn(Knight player) {
        if (player.b2body == null) return;

        float dist = Math.abs(player.b2body.getPosition().x - (startX));
        if (dist > respawnDistance && dead) {
            respawn();
        }
    }

    /**
     * Restores health parameters and relocates the physical body back to its initial coordinate vector.
     */
    public void respawn() {
        this.hp = maxHp;
        this.dead = false;
        this.stunTimer = 0f;
        b2body.setTransform(startX, startY, 0);
        b2body.setLinearVelocity(0, 0);
        System.out.println(this.getClass().getSimpleName() + " Respawned!");
    }

    @Override
    public void takeDamage(int amount) {
        if (dead) return;
        hp -= amount;
        stunTimer = 1f;
        System.out.println(this.getClass().getSimpleName() + " hit! HP left: " + hp);
        if (hp <= 0) die();
    }

    @Override
    public void applyKnockback(float dirX, float dirY) {
        if (dead) return;
        b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
        b2body.applyLinearImpulse(new Vector2(dirX, dirY), b2body.getWorldCenter(), true);
    }

    @Override
    public void die() {
        dead = true;
        System.out.println(this.getClass().getSimpleName() + " died! Turning into a corpse.");

        b2body.setGravityScale(1f);

        b2body.setLinearVelocity(b2body.getLinearVelocity().x, 0);
        b2body.applyLinearImpulse(new Vector2(0, Constants.Enemy.DEATH_KNOCKBACK), b2body.getWorldCenter(), true);

        eventCallback.onEnemyDeath(type);
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    public int getHp() {
        return hp;
    }
}
