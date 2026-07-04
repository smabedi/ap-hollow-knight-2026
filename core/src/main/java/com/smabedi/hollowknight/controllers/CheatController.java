package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.views.game.GameScreen;
import com.smabedi.hollowknight.views.game.GameUI;

public class CheatController extends InputAdapter {
    private final Knight player;
    private final GameUI gameUI;
    private final GameScreen gameScreen;

    public CheatController(Knight player, GameUI gameUI, GameScreen gameScreen) {
        this.player = player;
        this.gameUI = gameUI;
        this.gameScreen = gameScreen;
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    @Override
    public boolean keyDown(int keycode) {
        if (Gdx.input.isKeyPressed(Constants.Cheats.Keys.MODIFIER)) {

            switch (keycode) {
                // 1. God Mode
                case Constants.Cheats.Keys.GOD_MODE:
                    player.isGodMode = !player.isGodMode;
                    gameUI.showToast(Assets.getString("god_mode") + ": " +
                        (player.isGodMode ? Assets.getString("on") : Assets.getString("off")));
                    return true;

                // 2. Refill Soul
                case Constants.Cheats.Keys.REFILL_SOUL:
                    player.soul = Constants.Knight.MAX_SOUL;
                    gameUI.showToast(Assets.getString("soul_refilled"));
                    return true;

                // 3. Emergency Heal (Safety Net Toggle)
                case Constants.Cheats.Keys.EMERGENCY_HEAL:
                    player.emergencyHealArmed = !player.emergencyHealArmed;
                    gameUI.showToast(Assets.getString("emergency_auto_heal") + ": " +
                        (player.emergencyHealArmed ? Assets.getString("armed") : Assets.getString("disarmed")));
                    return true;

                // 4. Boss Arena Teleport
                case Constants.Cheats.Keys.BOSS_TELEPORT:
                    // Requires Constants.Cheats.BOSS_ARENA_X and Y to be defined in Constants.java
                    player.b2body.setTransform(
                        Constants.Cheats.BOSS_ARENA_X / Constants.World.PPM,
                        Constants.Cheats.BOSS_ARENA_Y / Constants.World.PPM,
                        0
                    );
                    gameUI.showToast(Assets.getString("teleported_to_false_knight_arena"));
                    return true;

                // 5. Time Dilation
                case Constants.Cheats.Keys.TIME_DILATION:
                    if (gameScreen.timeScale == 1f) {
                        gameScreen.timeScale = 0.3f; // 30% speed
                        gameUI.showToast(Assets.getString("time_dilation") + ": " + Assets.getString("activated"));
                    } else {
                        gameScreen.timeScale = 1f; // Normal speed
                        gameUI.showToast(Assets.getString("time_dilation") + ": " + Assets.getString("deactivated"));
                    }
                    return true;

                // 6. Noclip / Spectator Mode
                case Constants.Cheats.Keys.SPECTATOR_MODE:
                    player.isNoclip = !player.isNoclip;
                    if (player.isNoclip) {
                        player.b2body.setGravityScale(0f); // Float
                        for (Fixture fix : player.b2body.getFixtureList()) fix.setSensor(true); // Phase through walls
                        gameUI.showToast(Assets.getString("spectator_mode") + ": " + Assets.getString("on"));
                    } else {
                        player.b2body.setGravityScale(1f); // Fall
                        for (Fixture fix : player.b2body.getFixtureList()) {
                            // Only revert the main body. The foot/wall detectors are naturally sensors.
                            if ("knight".equals(fix.getUserData())) fix.setSensor(false);
                        }
                        gameUI.showToast(Assets.getString("spectator_mode") + ": " + Assets.getString("off"));
                    }
                    return true;
            }
        }
        return false;
    }
}
