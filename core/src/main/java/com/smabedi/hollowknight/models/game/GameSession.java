package com.smabedi.hollowknight.models.game;

import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.inventory.Inventory;
import com.smabedi.hollowknight.views.game.LocationType;

public class GameSession {
    public int slotIndex;
    public int health;
    public int maxHealth;
    public int soul;
    public LocationType location;
    public float playerX;
    public float playerY;
    public int playtime;
    public Inventory inventory;

    // Default constructor required for LibGDX JSON deserialization
    public GameSession() {}

    // Constructor for starting a brand-new game
    public GameSession(int slotIndex) {
        this.slotIndex = slotIndex;
        health = Constants.Knight.MAX_HEALTH;
        maxHealth = Constants.Knight.MAX_HEALTH;
        soul = 0;
        location = LocationType.FORGOTTEN_CROSSROADS;
        playtime = 0;
        inventory = new Inventory();

        // HACK: Temporary values, need to be changed to session.currentSpawnNodeId
        playerX = 2f;
        playerY = 3f;
    }

    // NOTE: fuck me.
}
