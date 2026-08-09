package com.smabedi.hollowknight.models.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.controllers.EventCallback;
import com.smabedi.hollowknight.models.entities.knight.Knight;

/**
 * Basic ground-traversing enemy entity.
 * Employs simplistic horizontal patrol behavior, dynamically reversing
 * traversal direction upon encountering walls or ledges.
 */
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
            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y);
            return;
        }

        if (loopSoundId == -1) {
            loopSoundId = AudioManager.loopSpatialSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 12f);
        } else {
            AudioManager.updateSpatialSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, loopSoundId, b2body.getWorldCenter(), player.b2body.getWorldCenter(), 12f);
        }

        if (stunTimer > 0) {
            stunTimer -= dt;
            return;
        }

        Vector2 center = b2body.getWorldCenter();
        float direction = movingRight ? 1f : -1f;

        Vector2 wallRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Enemy.Crawlid.WIDTH_HALVED_SCALED),
            center.y);
        final boolean[] hitWall = {false};

        world.rayCast((fixture, _, _, _) -> {
            if (hitEnd(fixture)) hitWall[0] = true;
            return 1;
        }, center, wallRayEnd);

        Vector2 ledgeRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Enemy.Crawlid.WIDTH_HALVED_SCALED),
            center.y - (1.5f * Constants.Enemy.Crawlid.HEIGHT_HALVED_SCALED));
        final boolean[] hitGround = {false};

        world.rayCast((fixture, _, _, _) -> {
            if (hitEnd(fixture)) hitGround[0] = true;
            return 1;
        }, center, ledgeRayEnd);

        if (hitWall[0] || !hitGround[0]) {
            movingRight = !movingRight;
        }

        float velocityX = movingRight ? Constants.Enemy.Crawlid.SPEED : -Constants.Enemy.Crawlid.SPEED;

        b2body.setLinearVelocity(velocityX, b2body.getLinearVelocity().y);
    }

    @Override
    public void die() {
        super.die();
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, loopSoundId);
            loopSoundId = -1;
        }
    }

    @Override
    public void respawn() {
        super.respawn();
        if (loopSoundId != -1) {
            AudioManager.stopSfx(Constants.Paths.Sounds.SFX_ENEMY_WALKING, loopSoundId);
            loopSoundId = -1;
        }
    }
}
