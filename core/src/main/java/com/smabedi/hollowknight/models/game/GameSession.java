package com.smabedi.hollowknight.models.game;

import com.smabedi.hollowknight.views.game.LocationType;

public class GameSession {
    private int health;
    private int maxHealth;
    private int soul;

    private LocationType location;
    private float playerX;
    private float playerY;

    private int playtime;

    public GameSession() {
        // HACK: Values are used as an example. Proper loading system has to me implemented.
        this.health = 5;
        this.maxHealth = 5;
        this.soul = 0;
        this.location = LocationType.FORGOTTEN_CROSSROADS;
        this.playerX = 200f;
        this.playerY = 500f;
        this.playtime = 0;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getSoul() {
        return soul;
    }

    public LocationType getLocation() {
        return location;
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public int getPlaytime() {
        return playtime;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setSoul(int soul) {
        this.soul = soul;
    }

    public void setLocation(LocationType location) {
        this.location = location;
    }

    public void setPlayerX(float playerX) {
        this.playerX = playerX;
    }

    public void setPlayerY(float playerY) {
        this.playerY = playerY;
    }

    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

    public void addPlaytime(int additionalMinutes) {
        this.playtime += additionalMinutes;
    }

    // NOTE: fuck me.
}
