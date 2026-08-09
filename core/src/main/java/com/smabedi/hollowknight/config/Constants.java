package com.smabedi.hollowknight.config;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

/**
 * Global configuration data.
 * Centralizes all magic numbers, file paths, and physical attributes
 * to ensure consistency and facilitate rapid gameplay balancing.
 */
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

        public static final class Saves {
            public static final String ROOT = "saves/";
            public static final String ROOT_PATH = "../" + ROOT;
            public static final String DATABASE = ROOT_PATH + "game.db";
        }

        public static final class Textures {
            public static final String ROOT = "textures/";
            public static final String KNIGHT_ATLAS = ROOT + "knight/knight.atlas";
            public static final String VFX_ATLAS = ROOT + "vfx/vfx.atlas";
            public static final String ENTITY_ATLAS = ROOT + "entities/entities.atlas";
            public static final String BOSS_ATLAS = ROOT + "boss/boss.atlas";
            public static final String UI_ROOT = ROOT + "ui/";
            public static final String UI_ATLAS = UI_ROOT + "ui.atlas";
            public static final String CURSOR = UI_ROOT + "cursor.png";
            public static final String SPLASH_LOGO = UI_ROOT + "loading.png";
        }

        public static final class Videos {
            public static final String ROOT = "videos/";
            public static final String BACKGROUND = ROOT + "background.atlas";
        }

        public static final class Sounds {
            public static final String ROOT = "sounds/";
            public static final String SPLASH = ROOT + "splash_sound.wav";
            public static final String BGM_MENU = ROOT + "menu.mp3";
            public static final String BGM_CROSSROADS = ROOT + "crossroads.mp3";
            public static final String BGM_GREENPATH = ROOT + "greenpath.mp3";
            public static final String BGM_BOSS = ROOT + "boss.mp3";
            public static final String BGM_WIN = ROOT + "win.mp3";
            public static final String SFX_BOSS_TRANSITION = ROOT + "boss_transition.wav";
            public static final String SFX_FOCUS = ROOT + "focus_health_heal.wav";
            public static final String SFX_DAMAGE = ROOT + "hero_damage.wav";
            public static final String SFX_DASH = ROOT + "hero_dash.wav";
            public static final String SFX_JUMP = ROOT + "hero_jump.wav";
            public static final String SFX_RUN = ROOT + "hero_run_footsteps_stone.wav";
            public static final String SFX_LAND = ROOT + "hero_land_soft.wav";
            public static final String SFX_SPELL_CAST = ROOT + "hero_scream_spell.wav";
            public static final String SFX_VOID_SPELL_CAST = ROOT + "hero_void_scream_spell.wav";
            public static final String SFX_WALL_SLIDE = ROOT + "hero_wall_slide.wav";
            public static final String SFX_NOTIFICATION = ROOT + "notification.mp3";
            public static final String SFX_ACHIEVEMENT = ROOT + "achievement_unlocked.mp3";
            public static final String SFX_ZOTE_ATTACK = ROOT + "zote_attack_loop.wav";
            public static final String SFX_UI_BUTTON = ROOT + "ui_button_confirm.wav";
            public static final String SFX_MOSSFLY_FLY = ROOT + "fly_flying_loop.wav";
            public static final String SFX_ENEMY_WALKING = ROOT + "enemy_walking_loop.wav";
            public static final String SFX_GUARDIAN_RUNNING_LOOP = ROOT + "guardian_run_loop.wav";
            public static final String SFX_LASER_BURST = ROOT + "laser_burst.wav";
            public static final String SFX_FK_STUN_HIT = ROOT + "false_knight_head_damage.wav";
            public static final String SFX_FK_ARMOR_HIT = ROOT + "false_knight_damage_armour.wav";
            public static final String SFX_FK_OPEN_ARMOR_HIT = ROOT + "false_knight_damage_armour_final.wav";
            public static final String SFX_FK_SWING = ROOT + "false_knight_swing.wav";
            public static final String SFX_FK_STRIKE = ROOT + "false_knight_strike_ground.wav";
            public static final String SFX_FK_POWER_STRIKE = ROOT + "false_knight_land_2.wav";
            public static final String SFX_FK_JUMP = ROOT + "false_knight_jump.wav";
            public static final String SFX_FK_LAND = ROOT + "false_knight_land_1.wav";
            public static final String SFX_FK_RUN_LOOP = ROOT + "false_knight_roll.wav";
            public static final String SFX_SOUL_FULL = ROOT + "enemy_death_sword.wav";
            public static final String[] SFX_FK_ROAR = {
                ROOT + "false_knight_attack_new_1.wav",
                ROOT + "false_knight_attack_new_2.wav",
                ROOT + "false_knight_attack_new_3.wav",
                ROOT + "false_knight_attack_new_4.wav",
                ROOT + "false_knight_attack_new_5.wav"
            };
            public static final String[] SFX_SOUL_PICKUP = {
                ROOT + "soul_pickup_1.wav",
                ROOT + "soul_pickup_2.wav",
                ROOT + "soul_pickup_3.wav",
                ROOT + "soul_pickup_4.wav",
                ROOT + "soul_pickup_5.wav",
                ROOT + "soul_pickup_6.wav",
                ROOT + "soul_pickup_7.wav"
            };
            public static final String[] SFX_ZOTE = {
                ROOT + "zote_1.wav",
                ROOT + "zote_2.wav",
                ROOT + "zote_3.wav",
                ROOT + "zote_4.wav",
                ROOT + "zote_5.wav"
            };
            public static final String[] SFX_SLASH = {
                ROOT + "sword_1.wav",
                ROOT + "sword_2.wav",
                ROOT + "sword_3.wav",
                ROOT + "sword_4.wav",
                ROOT + "sword_5.wav"
            };
        }
    }

    public static final class UI {
        public static final int DEFAULT_WIDTH = 1920;
        public static final int DEFAULT_HEIGHT = 1080;
        public static final float UPP = 2f; // Units Per Pixel UI scaling
        public static final int OVERSCREEN = 1000;
    }

    public static final class World {
        public static final Vector2 GRAVITY_VECTOR = new Vector2(0, -12f);
        public static final float TIME_STEP = 1 / 60f;
        // Pixels Per Meter - Standardizes Box2D physics scaling
        public static final float PPM = 100f;
    }

    public static final class Camera {
        public static final float MAX_SHAKE_OFFSET_X = 0.5f;
        public static final float MAX_SHAKE_OFFSET_Y = 0.5f;
        public static final float TRAUMA_DECAY = 0.75f;
        public static final float TRAUMA_MAX = 1.5f;
    }

    /**
     * Core player statistics, constraints, and spell parameters.
     */
    public static final class Knight {
        public static final int WIDTH = 50;
        public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
        public static final int HEIGHT = 80;
        public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
        public static final float SENSOR_WIDTH = 0.02f;
        public static final float ATTACK_COOLDOWN = 0.5f;
        public static final float MAX_SPEED = 5f;
        public static final float JUMP_STRENGTH = 3f;
        public static final float FRICTION = 0.1f;
        public static final float DENSITY = 1f;
        public static final float JUMP_CUTOFF_MULTIPLIER = 0.25f;
        public static final float WALL_SLIDE_SPEED = 2.5f;
        public static final float NAIL_REACH = 2f;
        public static final float KNOCKBACK_FORCE_X = 5f;
        public static final float KNOCKBACK_FORCE_Y = 2f;
        public static final float I_FRAME_DURATION = 1f; // 1 second of invincibility
        public static final int MAX_HEALTH = 5;
        public static final int MAX_SOUL = 99;
        public static final int SOUL_PER_HIT = 11;
        public static final int FOCUS_COST = 33;
        public static final float FOCUS_DURATION = 1.5f;

        public static final class Pogo {
            public static final float BOUNCE_STRENGTH = JUMP_STRENGTH * 1.1f;
            public static final float REACH = 0.6f;
            public static final float ATTACK_DURATION = 0.15f; // Hitbox lingers for 150ms
        }

        public static final class Dash {
            public static final float SPEED = 12f;
            public static final float DURATION = 0.4f;
            public static final float COOLDOWN = 2f;
        }

        public static final class HowlingWraiths {
            public static final float DURATION = 1f;
            public static final float WIDTH = 2f;
            public static final float HEIGHT = 2f;
        }

        public static final class VengefulSpirit {
            public static final float SPEED = 15f;
            public static final float WIDTH = 1f;
            public static final float HEIGHT = 0.4f;
            public static final float DURATION = 1f;
        }

        public static final class Inventory {
            public static final int MAX_NOTCHES = 3;
        }
    }

    public static final class Zote {
        public static final int WIDTH = 40;
        public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
        public static final int HEIGHT = 60;
        public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
        public static final float FRICTION = 0.5f;
        public static final float ANGRY_TIME = 4f;
        public static final float CHASE_SPEED = 3f;
        public static final float TYPE_SPEED = 0.025f;
        public static final int DIALOG_NUMBER = 3;
        public static final int PRECEPTS_NUMBER = 3;
        public static final float INTERACTION_DISTANCE = 2f;
    }

    public static final class FalseKnight {
        public static final int HP = 40;
        public static final int WIDTH = 200;
        public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
        public static final int HEIGHT = 300;
        public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
        public static final float FRICTION = 0.5f;
        public static final float DENSITY = 5f; // Heavy density resists light physical knockbacks

        public static final class ShockWave {
            public static final float WIDTH = 1f;
            public static final float HEIGHT = 0.6f;
            public static final float SPEED = 8f;
        }
    }

    public static final class Enemy {
        public static final float RESPAWN_DISTANCE = 15f;
        public static final float DEATH_KNOCKBACK = 5f;

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
            public static final int WIDTH = 80;
            public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
            public static final int HEIGHT = 120;
            public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
            public static final float FRICTION = 0.2f;
            public static final float VISION_RANGE = 8f; // How far it sees in front of itself
            public static final float WALK_DURATION = 4f;
            public static final float REST_DURATION = 2f;
        }

        public static final class CrystalGuardian {
            public static final int HP = 6;
            public static final float CHARGE_SPEED = 5.5f;
            public static final float RETURN_SPEED = 2.5f;
            public static final int WIDTH = 80;
            public static final float WIDTH_HALVED_SCALED = WIDTH / 2f / World.PPM;
            public static final int HEIGHT = 120;
            public static final float HEIGHT_HALVED_SCALED = HEIGHT / 2f / World.PPM;
            public static final float FRICTION = 0.2f;
            public static final float VISION_RANGE = 5f;
            public static final float LASER_RANGE = 40f;
            public static final float ENRAGE_DURATION = 3f;
            public static final float LASER_TELEGRAPH_TIME = 0.4f;
        }
    }

    public static final class Cheats {
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
