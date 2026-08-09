package com.smabedi.hollowknight.views.entities;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Defines the standard contract for entity rendering components.
 * Facilitates polymorphic iteration over diverse enemy types within the primary rendering pipeline,
 * completely decoupling the visual representation from the Box2D physics models.
 */
public interface EntityRenderer {
    void render(Batch batch, float dt);
}
