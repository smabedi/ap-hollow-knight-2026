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
        public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
        public static final int HEIGHT = 80;
        public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
        public static final float MAX_SPEED = 5f;
        public static final float JUMP_STRENGTH = 3f;
        public static final float FRICTION = 0.1f;
        public static final float DENSITY = 1f;
        public static final float JUMP_CUTOFF_MULTIPLIER = 0.25f;
        public static final float POGO_BOUNCE_STRENGTH = JUMP_STRENGTH * 1.1f;
        public static final float POGO_REACH = 0.5f;
        public static final float NAIL_ATTACK_DURATION = 0.15f; // Hitbox lingers for 150ms
        public static final float DASH_SPEED = 15f;
        public static final float DASH_DURATION = 0.2f;
        public static final float DASH_COOLDOWN = 0.6f;
        public static final float WALL_SLIDE_SPEED = 2.5f;
    }
}
