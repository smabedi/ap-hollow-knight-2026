package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.game.GameScreen;
import com.smabedi.hollowknight.views.game.GameUI;
import com.smabedi.hollowknight.views.game.LocationType;

public class CheatController extends InputAdapter {
    private final Knight player;
    private GameUI gameUI;
    private final GameScreen gameScreen;
    private final GameSession session;

    public CheatController(Knight player, GameUI gameUI, GameScreen gameScreen, GameSession session) {
        this.player = player;
        this.gameUI = gameUI;
        this.gameScreen = gameScreen;
        this.session = session;
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
                    AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    return true;

                // 2. Refill Soul
                case Constants.Cheats.Keys.REFILL_SOUL:
                    player.soul = Constants.Knight.MAX_SOUL;
                    gameUI.showToast(Assets.getString("soul_refilled"));
                    AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    return true;

                // 3. Emergency Heal (Safety Net Toggle)
                case Constants.Cheats.Keys.EMERGENCY_HEAL:
                    player.emergencyHealArmed = !player.emergencyHealArmed;
                    gameUI.showToast(Assets.getString("emergency_auto_heal") + ": " +
                        (player.emergencyHealArmed ? Assets.getString("armed") : Assets.getString("disarmed")));
                    AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    return true;

                // 4. Boss Arena Teleport
                case Constants.Cheats.Keys.BOSS_TELEPORT:
                    if (session.location != LocationType.GREENPATH) {
                        // We are in another map! Trigger a transition.
                        session.pendingTransition = true;
                        session.nextLocation = LocationType.GREENPATH;
                        session.pendingBossTeleport = true;
                        session.isArenaLocked = false; // Ensure camera unlocks
                        gameUI.showToast("Warping to Greenpath...");
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    } else {
                        // We are already in Greenpath, just teleport instantly.
                        player.b2body.setTransform(session.bossTeleportX, session.bossTeleportY, 0);
                        session.lastSafeX = session.bossTeleportX;
                        session.lastSafeY = session.bossTeleportY;
                        gameUI.showToast(Assets.getString("teleported_to_false_knight_arena"));
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    }
                    return true;

                // 5. Time Dilation
                case Constants.Cheats.Keys.TIME_DILATION:
                    if (gameScreen.timeScale == 1f) {
                        gameScreen.timeScale = 0.3f; // 30% speed
                        gameUI.showToast(Assets.getString("time_dilation") + ": " + Assets.getString("activated"));
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    } else {
                        gameScreen.timeScale = 1f; // Normal speed
                        gameUI.showToast(Assets.getString("time_dilation") + ": " + Assets.getString("deactivated"));
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    }
                    return true;

                // 6. Noclip / Spectator Mode
                case Constants.Cheats.Keys.SPECTATOR_MODE:
                    player.isNoclip = !player.isNoclip;
                    if (player.isNoclip) {
                        player.b2body.setGravityScale(0f); // Float
                        for (Fixture fix : player.b2body.getFixtureList()) fix.setSensor(true); // Phase through walls
                        gameUI.showToast(Assets.getString("spectator_mode") + ": " + Assets.getString("on"));
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    } else {
                        player.b2body.setGravityScale(1f); // Fall
                        for (Fixture fix : player.b2body.getFixtureList()) {
                            // Only revert the main body. The foot/wall detectors are naturally sensors.
                            if ("knight".equals(fix.getUserData())) fix.setSensor(false);
                        }
                        gameUI.showToast(Assets.getString("spectator_mode") + ": " + Assets.getString("off"));
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                    }
                    return true;
            }
        }
        return false;
    }

    public void setGameUI(GameUI gameUI) {
        this.gameUI = gameUI;
    }
}
