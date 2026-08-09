package com.smabedi.hollowknight.controllers;

import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.entities.enemies.EnemyType;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.game.GameUI;

/**
 * Evaluates game states and triggers achievement unlocks.
 */
public class AchievementManager {
    private static final int SPEEDRUN_TIME_LIMIT_SECONDS = 600;
    private final GameSession session;
    private GameUI gameUI;

    public AchievementManager(GameSession session, GameUI gameUI) {
        this.session = session;
        this.gameUI = gameUI;
    }

    /**
     * Registers an enemy kill globally and evaluates the "True Hunter" achievement.
     */
    public void evaluateEnemyKill(EnemyType enemyType) {
        GameSettings.registerEnemyKill(enemyType.name());

        boolean allKilled = true;
        for (EnemyType type : EnemyType.values()) {
            if (!GameSettings.hasKilledEnemy(type.name())) {
                allKilled = false;
                break;
            }
        }

        if (allKilled) {
            unlock("TRUE_HUNTER");
        }
    }

    /**
     * Evaluates endgame achievements triggered upon boss defeat.
     */
    public void evaluateBossDefeat() {
        unlock("DEFEAT_FALSE_KNIGHT");
        unlock("COMPLETION");

        if (session.playtime <= SPEEDRUN_TIME_LIMIT_SECONDS) {
            unlock("SPEEDRUN");
        }

        if (session.isOneSitting && session.deathCounter == 0) {
            unlock("ONE_SHOT");
        }
    }

    private void unlock(String achievementId) {
        if (!GameSettings.isAchievementUnlocked(achievementId)) {
            GameSettings.unlockAchievement(achievementId);
            gameUI.showToast(Assets.getString("achievements") + ": " + Assets.getString("achv_" + achievementId));
            AudioManager.playSfx(Constants.Paths.Sounds.SFX_ACHIEVEMENT);
        }
    }

    public void setGameUI(GameUI gameUI) {
        this.gameUI = gameUI;
    }
}
