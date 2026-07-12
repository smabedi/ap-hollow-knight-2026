package com.smabedi.hollowknight.controllers;

import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.entities.enemies.EnemyType;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.game.GameUI;

public class AchievementManager {
    private final GameSession session;
    private GameUI gameUI;

    // 10 minutes in seconds for the speedrun
    private static final int SPEEDRUN_TIME_LIMIT_SECONDS = 600;

    public AchievementManager(GameSession session, GameUI gameUI) {
        this.session = session;
        this.gameUI = gameUI;
    }

    public void evaluateEnemyKill(EnemyType enemyType) {
        // Log the kill globally
        GameSettings.registerEnemyKill(enemyType.name());

        // Check if ALL types have been killed across any save file
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

    public void evaluateBossDefeat() {
        unlock("DEFEAT_FALSE_KNIGHT");
        unlock("COMPLETION"); // False Knight marks the end of this scope

        if (session.playtime <= SPEEDRUN_TIME_LIMIT_SECONDS) {
            unlock("SPEEDRUN");
        }

        // ONE SHOT, ONE KILL: Must be one sitting AND zero deaths!
        if (session.isOneSitting && session.deathCounter == 0) {
            unlock("ONE_SHOT");
        }
    }

    private void unlock(String achievementId) {
        if (!GameSettings.isAchievementUnlocked(achievementId)) {
            GameSettings.unlockAchievement(achievementId);
            // Directly trigger the toast in the UI
            gameUI.showToast(Assets.getString("achievements") + ": " + Assets.getString("achv_" + achievementId));
        }
    }

    public void setGameUI(GameUI gameUI) {
        this.gameUI = gameUI;
    }
}
