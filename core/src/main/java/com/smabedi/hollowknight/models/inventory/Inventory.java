package com.smabedi.hollowknight.models.inventory;

import com.badlogic.gdx.utils.Array;
import com.smabedi.hollowknight.config.Constants;

/**
 * Manages the player's collection and active loadout of Charms.
 * Enforces notch capacity limits and ensures duplicate charms cannot be equipped.
 */
public class Inventory {
    private final Array<CharmType> ownedCharms;
    private final Array<CharmType> equippedCharms;

    public Inventory() {
        this.ownedCharms = new Array<>();
        this.equippedCharms = new Array<>();
    }

    /**
     * Grants a new charm to the player's collection, preventing duplicates.
     */
    public void addOwnedCharm(CharmType charm) {
        if (!ownedCharms.contains(charm, true)) {
            ownedCharms.add(charm);
        }
    }

    /**
     * Attempts to equip a charm to the active loadout.
     * Evaluates ownership, current equipment status, and notch capacity constraints.
     *
     * @return true if the charm was successfully equipped, false otherwise.
     */
    public boolean equipCharm(CharmType charm) {
        if (!ownedCharms.contains(charm, true) || equippedCharms.contains(charm, true)) {
            return false;
        }

        if (equippedCharms.size >= Constants.Knight.Inventory.MAX_NOTCHES) {
            System.out.println("Cannot equip " + charm.name() + ": Notch capacity full.");
            return false;
        }

        equippedCharms.add(charm);
        return true;
    }

    /**
     * Removes a charm from the active loadout.
     */
    public void unequipCharm(CharmType charm) {
        equippedCharms.removeValue(charm, true);
    }

    public boolean isEquipped(CharmType charm) {
        return equippedCharms.contains(charm, true);
    }

    public Array<CharmType> getOwnedCharms() {
        return ownedCharms;
    }

    public int getUsedNotches() {
        return equippedCharms.size;
    }
}
