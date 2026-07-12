package com.smabedi.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

public class Main extends Game {
    @Override
    public void create() {
        GameSettings.load();
        Assets.loadAssets();
        ScreenManager.init(this);
        ScreenManager.setMenuScreen(ScreenType.MAIN);
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
