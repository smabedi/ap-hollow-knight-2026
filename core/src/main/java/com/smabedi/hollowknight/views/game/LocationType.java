package com.smabedi.hollowknight.views.game;

import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;

/**
 * Defines the logical environment identifiers, directly bridging localized UI labels
 * to their corresponding TMX map asset paths.
 */
public enum LocationType {
    FORGOTTEN_CROSSROADS(Constants.Paths.FORGOTTEN_CROSSROADS),
    GREENPATH(Constants.Paths.GREENPATH);

    private final String path;

    LocationType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    /**
     * Resolves the localized display string mapped to the active environment identifier.
     */
    public String getName() {
        return Assets.getString(this.name().toLowerCase());
    }
}
