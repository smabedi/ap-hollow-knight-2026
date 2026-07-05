package com.smabedi.hollowknight.models.inventory;

import com.badlogic.gdx.utils.Array;
import com.smabedi.hollowknight.config.Constants;

public class Inventory {
    private final Array<CharmType> ownedCharms;
    private final Array<CharmType> equippedCharms;

    public Inventory() {
        this.ownedCharms = new Array<>();
        this.equippedCharms = new Array<>();
    }

    public void addOwnedCharm(CharmType charm) {
        if (!ownedCharms.contains(charm, true)) {
            ownedCharms.add(charm);
        }
    }

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
