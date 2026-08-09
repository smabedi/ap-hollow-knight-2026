package com.smabedi.hollowknight.models.game;

import com.smabedi.hollowknight.views.game.LocationType;

/**
 * Data transfer object attached to Box2D map transition sensors.
 * Holds routing information for loading new areas.
 */
public class TransitionData {
    public LocationType targetLocation;

    public TransitionData(LocationType location) {
        this.targetLocation = location;
    }
}
