package com.smabedi.hollowknight.views;

import com.badlogic.gdx.Screen;
import com.smabedi.hollowknight.Main;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.game.GameScreen;
import com.smabedi.hollowknight.views.menus.MainMenuScreen;
import com.smabedi.hollowknight.views.menus.SettingsMenuScreen;
import com.smabedi.hollowknight.views.menus.StartGameMenuScreen;

import java.util.HashMap;
import java.util.Map;

public final class ScreenManager {
    private static Main main;
    private static final Map<ScreenType, Screen> screens = new HashMap<>();

    private ScreenManager() {
    }

    public static void init(Main main) {
        ScreenManager.main = main;
    }

    public static void setMenuScreen(ScreenType type) {
        if (!screens.containsKey(type)) {
            Screen newScreen = switch (type) {
                case MAIN -> new MainMenuScreen();
                case START_GAME -> new StartGameMenuScreen();
                case SETTINGS -> new SettingsMenuScreen();
            };
            screens.put(type, newScreen);
        }

        main.setScreen(screens.get(type));
    }

    public static void setGameScreen(GameSession session) {
        GameScreen gameScreen = new GameScreen(session);
        main.setScreen(gameScreen);
    }

    public static void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();
    }
}
