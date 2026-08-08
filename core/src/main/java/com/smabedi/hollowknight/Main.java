package com.smabedi.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.SplashScreen;

public class Main extends Game {
    @Override
    public void create() {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(Constants.Paths.Textures.CURSOR));
        int xHotspot = 19;
        int yHotspot = 17;
        Cursor customCursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        Gdx.graphics.setCursor(customCursor);
        pixmap.dispose();

        GameSettings.load();
        ScreenManager.init(this);
        // Set the Splash Screen as the very first thing the user sees
        this.setScreen(new SplashScreen());
    }

    @Override
    public void render() {
        AudioManager.getInstance().update(Gdx.graphics.getDeltaTime());
        ScreenUtils.clear(0, 0, 0, 1f);
        super.render();
    }

    @Override
    public void dispose() {
        Assets.dispose();
        ScreenManager.dispose();
    }
}
