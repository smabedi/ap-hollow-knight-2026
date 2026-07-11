package com.smabedi.hollowknight.controllers.events;

public interface Observer {
    void onNotify(GameEvent event, Object payload);
}
