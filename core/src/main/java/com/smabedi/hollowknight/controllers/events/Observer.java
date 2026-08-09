package com.smabedi.hollowknight.controllers.events;

/**
 * Interface for receiving global game events.
 */
public interface Observer {
    void onNotify(GameEvent event, Object payload);
}
