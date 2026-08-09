package com.smabedi.hollowknight.views;

import com.badlogic.gdx.Screen;
import com.smabedi.hollowknight.Main;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.game.GameScreen;
import com.smabedi.hollowknight.views.menus.*;

import java.util.HashMap;
import java.util.Map;

import static com.smabedi.hollowknight.views.ScreenType.START_GAME;

/**
 * Centralized state machine for routing and caching application screens.
 * Manages memory by disposing active gameplay instances when navigating
 * back to core menus, while preserving standard UI layouts for rapid switching.
 */
public final class ScreenManager {
    private static final Map<ScreenType, Screen> screens = new HashMap<>();
    private static Main main;
    @SuppressWarnings("GDXJavaStaticResource")
    private static GameScreen currentGameScreen;
    @SuppressWarnings("GDXJavaStaticResource")
    private static EndGameScreen currentEndGameScreen;

    private ScreenManager() {
    }

    public static void init(Main main) {
        ScreenManager.main = main;
    }

    /**
     * Transitions to a designated menu screen. Lazily instantiates screens
     * to optimize startup time and memory footprint.
     */
    public static void setMenuScreen(ScreenType type) {
        if (!isGameActive()) {
            AudioManager.playMusic(Constants.Paths.Sounds.BGM_MENU, true);
        }

        if (!screens.containsKey(type)) {
            Screen newScreen = switch (type) {
                case MAIN -> new MainMenuScreen();
                case START_GAME -> new StartGameMenuScreen();
                case SETTINGS -> new SettingsMenuScreen();
                case GUIDE -> new GuideMenuScreen();
                case ACHIEVEMENTS -> new AchievementsMenuScreen();
            };
            screens.put(type, newScreen);
        }

        main.setScreen(screens.get(type));
    }

    public static void setGameScreen(GameSession session) {
        currentGameScreen = new GameScreen(session);
        main.setScreen(currentGameScreen);
    }

    public static void setEndGameScreen(GameSession session) {
        clearGameScreen();

        if (currentEndGameScreen != null) {
            currentEndGameScreen.dispose();
        }

        currentEndGameScreen = new EndGameScreen(session);
        main.setScreen(currentEndGameScreen);
    }

    public static boolean isGameActive() {
        return currentGameScreen != null;
    }

    public static void resumeGame() {
        if (currentGameScreen != null) {
            main.setScreen(currentGameScreen);
        }
    }

    /**
     * Purges the active game environment to free system resources,
     * forcing a reload of the primary menu states.
     */
    public static void clearGameScreen() {
        if (currentGameScreen != null) {
            currentGameScreen.dispose();
            currentGameScreen = null;
        }
        if (screens.containsKey(START_GAME)) {
            reloadLanguage();
        }
        AudioManager.stopAllSfx();
    }

    /**
     * Rebuilds all cached UI components to reflect updated localization strings.
     */
    public static void reloadLanguage() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();

        if (currentGameScreen != null) {
            currentGameScreen.rebuildUI();
        }
    }

    public static void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();
        clearGameScreen();
    }
}
