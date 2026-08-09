package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;

/**
 * The final sequence view displayed upon defeating the primary boss.
 * Renders a looping video background via texture animation and computes aggregate gameplay statistics.
 */
public class EndGameScreen implements Screen {
    private final Stage stage;
    private final Skin skin;
    private final GameSession session;
    private final Animation<TextureRegion> videoAnimation;
    private float stateTime = 0f;

    public EndGameScreen(GameSession session) {
        this.session = session;
        this.skin = Assets.getSkin();
        this.videoAnimation = new Animation<>(0.1f, Assets.getBackgroundAtlas().findRegions("eternal_ordeal"), Animation.PlayMode.LOOP);

        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        this.stage = new Stage(viewport);
        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);

        // Initialize the root container with an alpha of zero to facilitate a smooth fade-in transition
        root.setColor(1, 1, 1, 0);

        Label titleLabel = new Label(Assets.getString("you_won"), skin);
        titleLabel.setColor(Color.GOLD);
        root.add(titleLabel).padBottom(40).row();

        // Calculate human-readable playtime from the total accumulated seconds
        int hours = session.playtime / 3600;
        int minutes = (session.playtime % 3600) / 60;
        int seconds = session.playtime % 60;
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        Table statsTable = new Table();
        statsTable.defaults().pad(10).center();

        statsTable.add(new Label(Assets.getString("stats_deaths") + " " + session.deathCounter, skin)).row();
        statsTable.add(new Label(Assets.getString("stats_kills") + " " + session.enemyKillCounter, skin)).row();
        statsTable.add(new Label(Assets.getString("stats_time") + " " + timeFormatted, skin)).row();

        root.add(statsTable).padBottom(50).row();

        TextButton mainMenuBtn = new TextButton(Assets.getString("main_menu"), skin);
        mainMenuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
                dispose();
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_UI_BUTTON);
            }
        });
        root.add(mainMenuBtn).width(200);

        // Apply a progressive fade-in action spanning 1.5 seconds
        root.addAction(Actions.fadeIn(1.5f));

        stage.addActor(root);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        AudioManager.playMusic(Constants.Paths.Sounds.BGM_WIN, false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;
        TextureRegion currentFrame = videoAnimation.getKeyFrame(stateTime, true);

        float viewWidth = stage.getViewport().getWorldWidth();
        float viewHeight = stage.getViewport().getWorldHeight();

        float texWidth = currentFrame.getRegionWidth();
        float texHeight = currentFrame.getRegionHeight();

        // Calculate the optimal dimensions to fill the viewport while strictly maintaining the original aspect ratio
        Vector2 size = com.badlogic.gdx.utils.Scaling.fill.apply(texWidth, texHeight, viewWidth, viewHeight);

        float x = (viewWidth - size.x) / 2f;
        float y = (viewHeight - size.y) / 2f;

        stage.getBatch().begin();
        stage.getBatch().draw(currentFrame, x, y, size.x, size.y);
        stage.getBatch().end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
