package com.smabedi.hollowknight.views.game;

import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;

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

    public String getName() {
        return Assets.getString(this.name().toLowerCase());
    }
}
