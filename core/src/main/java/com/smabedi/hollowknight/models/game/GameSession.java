package com.smabedi.hollowknight.models.game;

import com.smabedi.hollowknight.models.inventory.CharmType;
import com.smabedi.hollowknight.models.inventory.Inventory;
import com.smabedi.hollowknight.views.game.LocationType;

public class GameSession {
    public int health;
    public int maxHealth;
    public int soul;
    public LocationType location;
    public float playerX;
    public float playerY;
    public int playtime;
    public Inventory inventory;

    public GameSession() {
        // HACK: Values are used as an example. Proper loading system has to me implemented.
        this.health = 5;
        this.maxHealth = 5;
        this.soul = 0;
        this.location = LocationType.FORGOTTEN_CROSSROADS;
        this.playerX = 200f;
        this.playerY = 500f;
        this.playtime = 0;
        this.inventory = new Inventory();
        this.inventory.addOwnedCharm(CharmType.SOUL_CATCHER);
        this.inventory.addOwnedCharm(CharmType.DASHMASTER);
        this.inventory.addOwnedCharm(CharmType.SHARP_SHADOW);
        this.inventory.addOwnedCharm(CharmType.QUICK_SLASH);
        this.inventory.equipCharm(CharmType.SHARP_SHADOW);
        this.inventory.equipCharm(CharmType.DASHMASTER);
        this.inventory.equipCharm(CharmType.QUICK_SLASH);
    }

    // NOTE: fuck me.
}
