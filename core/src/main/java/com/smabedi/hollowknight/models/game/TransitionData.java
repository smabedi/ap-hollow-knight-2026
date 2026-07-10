package com.smabedi.hollowknight.models.game;

import com.smabedi.hollowknight.views.game.LocationType;

public class TransitionData {
    public LocationType targetLocation;

    public TransitionData(LocationType location) {
        this.targetLocation = location;
    }
}
