package com.smabedi.hollowknight.models.inventory;

import com.smabedi.hollowknight.config.Assets;

public enum CharmType {
    SOUL_CATCHER,
    DASHMASTER,
    UNBREAKABLE_STRENGTH,
    QUICK_SLASH,
    QUICK_FOCUS,
    HEAVY_BLOW,
    SHARP_SHADOW,
    VOID_HEART;

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
