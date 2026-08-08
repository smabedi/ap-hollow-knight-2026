package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.enemies.Crawlid;

public class CrawlidRenderer implements EntityRenderer {
    private final Crawlid crawlid;
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> deathAnim;
    private float stateTimer = 0f;
    private boolean wasDead = false;

    public CrawlidRenderer(Crawlid crawlid, TextureAtlas atlas) {
        this.crawlid = crawlid;
        walkAnim = new Animation<>(0.1f, atlas.findRegions("crawlid_walk"), Animation.PlayMode.LOOP);
        deathAnim = new Animation<>(0.15f, atlas.findRegions("crawlid_death"), Animation.PlayMode.NORMAL);
    }

    public void render(Batch batch, float dt) {
        boolean isDead = crawlid.isDead();

        // 1. Detect state transition to reset the animation timer
        // This handles both dying and respawning!
        if (isDead != wasDead) {
            stateTimer = 0f;
            wasDead = isDead;
        }

        stateTimer += dt;
        TextureRegion currentFrame = isDead ? deathAnim.getKeyFrame(stateTimer) : walkAnim.getKeyFrame(stateTimer);

        // 2. Flipping logic (Default asset faces LEFT)
        float velX = crawlid.b2body.getLinearVelocity().x;

        if (velX > 0 && !currentFrame.isFlipX()) {
            // Moving Right: Flip to face right
            currentFrame.flip(true, false);
        } else if (velX < 0 && currentFrame.isFlipX()) {
            // Moving Left: Un-flip to face left
            currentFrame.flip(true, false);
        }
        // NOTE: If velX == 0 (e.g., dead and stopped), it safely retains its current flip state.

        // 3. Draw
        float width = currentFrame.getRegionWidth() / Constants.World.PPM;
        float height = currentFrame.getRegionHeight() / Constants.World.PPM;
        float x = crawlid.b2body.getPosition().x - (width / 2f);
        float y = crawlid.b2body.getPosition().y - (height / 2f) + Constants.Enemy.Crawlid.HEIGHT_HALVED_SCALED * 2f;

        batch.draw(currentFrame, x, y, width, height);
    }
}
