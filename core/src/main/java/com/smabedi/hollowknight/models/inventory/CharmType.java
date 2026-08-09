package com.smabedi.hollowknight.models.inventory;

import com.smabedi.hollowknight.config.Assets;

/**
 * Enumeration of all available Charms within the game.
 * Acts as a standardized key for fetching localized display names and descriptions.
 */
public enum CharmType {
    SOUL_CATCHER,
    DASHMASTER,
    UNBREAKABLE_STRENGTH,
    QUICK_SLASH,
    QUICK_FOCUS,
    HEAVY_BLOW,
    SHARP_SHADOW,
    VOID_HEART;

    /**
     * Derives the corresponding localization key based on the enum's constant name.
     */
    public String getLangKey() {
        return this.name().toLowerCase();
    }

    public String getName() {
        return Assets.getString(getLangKey());
    }

    public String getDescription() {
        return Assets.getString(getLangKey() + "_desc");
    }
}
