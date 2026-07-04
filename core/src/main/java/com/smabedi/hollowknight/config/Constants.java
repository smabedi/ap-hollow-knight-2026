package com.smabedi.hollowknight.config;

import com.badlogic.gdx.Input;
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
        public static final float ATTACK_COOLDOWN = 0.3f;
        public static final float MAX_SPEED = 5f;
        public static final float JUMP_STRENGTH = 3f;
        public static final float FRICTION = 0.1f;
        public static final float DENSITY = 1f;
        public static final float JUMP_CUTOFF_MULTIPLIER = 0.25f;
        public static final float POGO_BOUNCE_STRENGTH = JUMP_STRENGTH * 1.1f;
        public static final float POGO_REACH = 0.5f;
        public static final float POGO_ATTACK_DURATION = 0.15f; // Hitbox lingers for 150ms
        public static final float DASH_SPEED = 15f;
        public static final float DASH_DURATION = 0.2f;
        public static final float DASH_COOLDOWN = 2f;
        public static final float WALL_SLIDE_SPEED = 2.5f;
        public static final float NAIL_REACH = 0.8f;
        public static final float KNOCKBACK_FORCE_X = 5f;
        public static final float KNOCKBACK_FORCE_Y = 2f;
        public static final float I_FRAME_DURATION = 1f; // 1 second of invincibility
        public static final int MAX_HEALTH = 5;
        public static final int MAX_SOUL = 99;
        public static final int SOUL_PER_HIT = 11;
        public static final int FOCUS_COST = 33;
        public static final float FOCUS_DURATION = 1.5f;
        public static final float WRAITHS_DURATION = 0.6f;
        public static final float WRAITHS_WIDTH = 1f;
        public static final float WRAITHS_HEIGHT = 1.5f;
        public static final float SPRIT_SPEED = 15f;
        public static final float SPRIT_WIDTH = 0.6f;
        public static final float SPRIT_HEIGHT = 0.4f;
        public static final float SPRIT_CAST_DURATION = 0.25f;
    }

    public static final class Enemy {
        public static final float RESPAWN_DISTANCE = 20f;

        public static final class Crawlid {
            public static final int HP = 2;
            public static final float SPEED = 1.5f;
            public static final int WIDTH = 80;
            public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
            public static final int HEIGHT = 40;
            public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
            public static final float FRICTION = 0.2f;
        }

        public static final class Mossfly {
            public static final int HP = 3;
            public static final float SPEED = 2.5f;
            public static final float AGGRO_RADIUS = 5f;
            public static final int RADIUS = 30;
            public static final float RADIUS_HALVED_SCALED = RADIUS / World.PPM;
            public static final float FRICTION = 0f;
        }

        public static final class HuskHornhead {
            public static final int HP = 4;
            public static final float WALK_SPEED = 1f;
            public static final float CHARGE_SPEED = 5f;
            public static final int WIDTH = 50;
            public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
            public static final int HEIGHT = 80;
            public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
            public static final float FRICTION = 0.2f;
            public static final float VISION_RANGE = 8f; // How far it sees in front of itself
            public static final float WALK_DURATION = 4f;
            public static final float REST_DURATION = 2f;
        }

        public static final class CrystalGuardian {
            public static final int HP = 6;
            public static final float CHARGE_SPEED = 5.5f;
            public static final float RETURN_SPEED = 2f;
            public static final int WIDTH = 60;
            public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
            public static final int HEIGHT = 70;
            public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
            public static final float FRICTION = 0.2f;
            public static final float VISION_RANGE = 12f; // Long range laser sight!
            public static final float ENRAGE_DURATION = 3f;
            public static final float LASER_TELEGRAPH_TIME = 0.4f; // A brief pause to warn the player
        }
    }

    public static final class Inventory {
        public static final int MAX_NOTCHES = 3;
    }

    public static final class Cheats {
        // TODO: Adjust these to match the exact Tiled map coordinates later.
        public static final float BOSS_ARENA_X = 500f;
        public static final float BOSS_ARENA_Y = 500f;

        public static final class Keys {
            public static final int MODIFIER = Input.Keys.CONTROL_LEFT;
            public static final int GOD_MODE = Input.Keys.G;
            public static final int REFILL_SOUL = Input.Keys.R;
            public static final int EMERGENCY_HEAL = Input.Keys.H;
            public static final int BOSS_TELEPORT = Input.Keys.T;
            public static final int TIME_DILATION = Input.Keys.D;
            public static final int SPECTATOR_MODE = Input.Keys.N;
        }
    }
}
