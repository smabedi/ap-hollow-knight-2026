package com.smabedi.hollowknight.models.entities.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface VfxCallback {
    void spawnStaticVfx(Animation<TextureRegion> anim, float x, float y, float offsetX, float offsetY, boolean facingRight, boolean defaultFacesRight);
}
