package com.smabedi.hollowknight.config;

import com.badlogic.gdx.math.Vector2;

public final class Constants {
    private Constants() {
    }

    public static final class Paths {
        public static final String UI = "ui/";
        public static final String SKIN = UI + "uiskin.json";
        public static final String LANG_MANAGEMENT = "i18n/";
        public static final String STRINGS = LANG_MANAGEMENT + "strings";
        public static final String MAPS = "maps/";
        public static final String FORGOTTEN_CROSSROADS = MAPS + "forgotten_crossroads.tmx";
        public static final String GREENPATH = MAPS + "greenpath.tmx";
    }

    public static final class UI {
        public static final int DEFAULT_WIDTH = 1920;
        public static final int DEFAULT_HEIGHT = 1080;
        public static final float UPP = 2f;
    }

    public static final class World {
        public static final Vector2 GRAVITY_VECTOR = new Vector2(0, -10f);
        public static final float TIME_STEP = 1 / 60f;
        public static final float PPM = 100f;
    }

    public static final class Knight {
        public static final int WIDTH = 50;
        public static final int HEIGHT = 80;
        public static final float MAX_SPEED = 5f;
        public static final float JUMP_STRENGTH = 2f;
        public static final float FRICTION = 0.1f;
        public static final float DENSITY = 0.8f;
    }
}
