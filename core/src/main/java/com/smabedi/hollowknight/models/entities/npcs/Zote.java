package com.smabedi.hollowknight.models.entities.npcs;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.IDamageable;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class Zote implements IDamageable {
    private final World world;
    public Body b2body;
    private boolean isAngry = false;
    private float angryTimer = 0f;
    private float stunTimer = 0f;
    private boolean movingRight = true;
    private int dialogueIndex = 0;
    private boolean hasFinishedIntro = false;
    private float talkTimer = 0f;

    public Zote(World world, float x, float y) {
        this.world = world;
        defineZote(x, y);
    }

    private void defineZote(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(Constants.Zote.WIDTH_HALVED_SCALED, Constants.Zote.HEIGHT_HALVED_SCALED);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = Constants.Zote.FRICTION;

        assert b2body != null;
        b2body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    public void update(float dt, Knight player) {
        if (talkTimer > 0) talkTimer -= dt;

        if (stunTimer > 0) {
            stunTimer -= dt;
            Vector2 vel = b2body.getLinearVelocity();
            b2body.setLinearVelocity(vel.x * 0.9f, vel.y); // Heavy friction
            return; // Exit early!
        }

        if (isAngry) {
            angryTimer -= dt;

            Vector2 center = b2body.getWorldCenter();
            float direction = player.b2body.getPosition().x > b2body.getPosition().x ? 1f : -1f;
            movingRight = direction > 0;

            // 1. Raycast for Walls
            Vector2 wallRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Zote.WIDTH_HALVED_SCALED), center.y);
            final boolean[] hitWall = {false};
            world.rayCast((fixture, _, _, _) -> {
                if ("ground".equals(fixture.getUserData())) hitWall[0] = true;
                return 1;
            }, center, wallRayEnd);

            // 2. Raycast for Ledges
            Vector2 ledgeRayEnd = new Vector2(center.x + (direction * 1.5f * Constants.Zote.WIDTH_HALVED_SCALED), center.y - (1.5f * Constants.Zote.HEIGHT_HALVED_SCALED));
            final boolean[] hitGround = {false};
            world.rayCast((fixture, _, _, _) -> {
                if ("ground".equals(fixture.getUserData())) hitGround[0] = true;
                return 1;
            }, center, ledgeRayEnd);

            // Turn around if path is blocked or cliff detected
            if (hitWall[0] || !hitGround[0]) {
                movingRight = !movingRight;
            }

            float velocityX = direction * Constants.Zote.CHASE_SPEED;
            if (Math.abs(player.b2body.getPosition().x - b2body.getPosition().x)
                >= (Constants.Knight.WIDTH_HALVED_SCALED + Constants.Zote.WIDTH_HALVED_SCALED) * 1.5f) {
                b2body.setLinearVelocity(velocityX, b2body.getLinearVelocity().y);
            } else {
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
            }

            if (angryTimer <= 0) {
                isAngry = false;
                b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
            }
        } else {
            // Idle state
            b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
        }
    }

    public String getNextDialogue() {
        if (isAngry) return null;

        talkTimer = 0.1f;

        String textToDisplay;
        if (!hasFinishedIntro) {
            textToDisplay = Assets.getString("zote_dialog_" + dialogueIndex);
            dialogueIndex++;
            if (dialogueIndex >= Constants.Zote.DIALOG_NUMBER) {
                hasFinishedIntro = true;
                dialogueIndex = 0; // Reset for precepts
            }
        } else {
            textToDisplay = Assets.getString("zote_precept_" + dialogueIndex % Constants.Zote.PRECEPTS_NUMBER);
            dialogueIndex++;
        }
        return textToDisplay;
    }

    public boolean isAngry() {
        return isAngry;
    }

    @Override
    public void takeDamage(int amount) {
        if (!isAngry) {
            System.out.println("Zote: Curse you! Have at thee!");
            isAngry = true;
            angryTimer = Constants.Zote.ANGRY_TIME;
            AudioManager.playSfx(Constants.Paths.Sounds.SFX_ZOTE_ATTACK);
        }
        stunTimer = 0.5f;
    }

    @Override
    public void applyKnockback(float dirX, float dirY) {
        b2body.setLinearVelocity(0, b2body.getLinearVelocity().y);
        b2body.applyLinearImpulse(new Vector2(dirX, dirY), b2body.getWorldCenter(), true);
    }

    @Override
    public void die() {
    }

    @Override
    public boolean isDead() {
        return false;
    }

    public boolean isTalking() {
        return talkTimer > 0;
    }

    public boolean isFacingRight() {
        return movingRight;
    }
}
