package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class Crawlid extends Enemy {
    private boolean movingRight = true;

    public Crawlid(World world, float x, float y, EventCallback eventCallback) {
        super(world, x, y, Constants.Enemy.Crawlid.HP, eventCallback, EnemyType.CRAWLID);
        define();
    }

    @Override
    public void defineShape() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(Constants.Enemy.Crawlid.WIDTH_HALVED_SCALED, Constants.Enemy.Crawlid.HEIGHT_HALVED_SCALED);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = Constants.Enemy.Crawlid.FRICTION;

        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    @Override
    public void update(float dt, Knight player) {
        checkRespawn(player);

        if (dead) {
            // Apply heavy friction so the corpse stops sliding after taking the killing blow
            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y);
            return; // Stop AI logic
        }

        // Stun logic
        if (stunTimer > 0) {
            stunTimer -= dt;
            return; // Exit early! Let Box2D handle the knockback physics and friction.
        }

        Vector2 center = b2body.getWorldCenter();
        float direction = movingRight ? 1f : -1f;

        // 1. Raycast for Walls (Short line directly in front)
        Vector2 wallRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Enemy.Crawlid.WIDTH_HALVED_SCALED),
            center.y);
        final boolean[] hitWall = {false};

        world.rayCast((fixture, _, _, _) -> {
            if (hitEnd(fixture)) hitWall[0] = true;
            return 1;
        }, center, wallRayEnd);

        // 2. Raycast for Ledges (Short line angled down and forward)
        Vector2 ledgeRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Enemy.Crawlid.WIDTH_HALVED_SCALED),
            center.y - (1.5f * Constants.Enemy.Crawlid.HEIGHT_HALVED_SCALED));
        final boolean[] hitGround = {false};

        world.rayCast((fixture, _, _, _) -> {
            if (hitEnd(fixture)) hitGround[0] = true;
            return 1;
        }, center, ledgeRayEnd);

        // Turn around if we hit a wall, or if the ledge raycast found empty air!
        if (hitWall[0] || !hitGround[0]) {
            movingRight = !movingRight;
        }

        // Apply constant movement speed
        float velocityX = movingRight ? Constants.Enemy.Crawlid.SPEED : -Constants.Enemy.Crawlid.SPEED;

        // We only overwrite the X velocity, preserving the Y velocity (gravity/falling)
        b2body.setLinearVelocity(velocityX, b2body.getLinearVelocity().y);
    }
}
