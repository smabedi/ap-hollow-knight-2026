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
    public int playtime;
    private transient float playtimeAccumulator = 0f;
    public int deathCounter;
    public int enemyKillCounter;
    public Inventory inventory;
    public float shakeTrauma = 0f;
    public boolean isArenaLocked = false;
    public boolean arenaGatesSolidified = false;
    public float mapMinX;
    public float mapMaxX;
    public float mapMinY;
    public float mapMaxY;
    public float arenaMinX = 0f;
    public float arenaMinY = 0f;
    public float arenaMaxX = 0f;
    public float arenaMaxY = 0f;
    public float playerX = -1;
    public float playerY = -1;
    public float initialSpawnX = -1f;
    public float initialSpawnY = -1f;
    public boolean pendingDeathRespawn = false;
    public float lastSafeX = -1f;
    public float lastSafeY = -1f;
    public boolean pendingRespawn = false;
    public boolean safeSpotUpdated = false;
    public float bossTeleportX = -1f;
    public float bossTeleportY = -1f;
    public boolean pendingTransition = false;
    public LocationType nextLocation;
    public boolean pendingBossTeleport = false;

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
        deathCounter = 0;
        enemyKillCounter = 0;
        inventory = new Inventory();
    }

    public void addTrauma(float amount) {
        shakeTrauma += amount;
        if (shakeTrauma > Constants.Camera.TRAUMA_MAX) shakeTrauma = Constants.Camera.TRAUMA_MAX;
    }

    public void setTrauma(float amount) {
        shakeTrauma = amount;
        if (shakeTrauma > Constants.Camera.TRAUMA_MAX) shakeTrauma = Constants.Camera.TRAUMA_MAX;
    }

    public void update(float scaledDt, float dt) {
        // 1. Decay camera trauma
        if (shakeTrauma > 0) {
            shakeTrauma -= scaledDt * Constants.Camera.TRAUMA_DECAY;
            if (shakeTrauma < 0) shakeTrauma = 0;
        }

        // 2. Increment Playtime
        playtimeAccumulator += dt;
        if (playtimeAccumulator >= 1.0f) {
            playtime++; // Add one second
            playtimeAccumulator -= 1.0f; // Carry over any extra fractions of a second
        }
    }

    // NOTE: fuck me.
}
