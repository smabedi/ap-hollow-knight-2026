package com.smabedi.hollowknight.views.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.views.ScreenManager;
import com.smabedi.hollowknight.views.ScreenType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import java.io.FileNotFoundException;

public class EndGameScreen implements Screen {
    private final Stage stage;
    private final Skin skin;
    private final GameSession session;
    private VideoPlayer videoPlayer;
    private final Music winMusic;

    public EndGameScreen(GameSession session) {
        this.session = session;
        this.skin = Assets.getSkin();

        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        this.stage = new Stage(viewport);

        winMusic = Gdx.audio.newMusic(Gdx.files.internal(Constants.Paths.Sounds.WIN));
        winMusic.setLooping(false);
        winMusic.setVolume(GameSettings.getMusicVolume());

        buildUI();
        initVideo();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);

        // Start completely transparent for the fade-in effect
        root.setColor(1, 1, 1, 0);

        // 1. Title
        Label titleLabel = new Label(Assets.getString("you_won"), skin);
        titleLabel.setColor(Color.GOLD);
        // Assuming your skin has a title font style, you could pass "title" here
        root.add(titleLabel).padBottom(40).row();

        // 2. Stats Calculation
        int hours = session.playtime / 3600;
        int minutes = (session.playtime % 3600) / 60;
        int seconds = session.playtime % 60;
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        // 3. Stats Display
        Table statsTable = new Table();
        statsTable.defaults().pad(10).center();

        statsTable.add(new Label(Assets.getString("stats_deaths") + " " + session.deathCounter, skin)).row();
        statsTable.add(new Label(Assets.getString("stats_kills") + " " + session.enemyKillCounter, skin)).row();
        statsTable.add(new Label(Assets.getString("stats_time") + " " + timeFormatted, skin)).row();

        root.add(statsTable).padBottom(50).row();

        // 4. Main Menu Button
        TextButton mainMenuBtn = new TextButton(Assets.getString("main_menu"), skin);
        mainMenuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
                dispose();
            }
        });
        root.add(mainMenuBtn).width(200);

        // 5. Fade In Action (Takes 1.5 seconds)
        root.addAction(Actions.fadeIn(1.5f));

        stage.addActor(root);
    }

    private void initVideo() {
        videoPlayer = VideoPlayerCreator.createVideoPlayer();
        try {
            if (videoPlayer.load(Gdx.files.internal(Constants.Paths.Videos.WIN))) {
                videoPlayer.play();
                videoPlayer.setLooping(true);
                videoPlayer.setVolume(0.5f);
            }
        } catch (FileNotFoundException e) {
            System.err.println("End game video file not found!");
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        // Play the music as soon as the screen is fully shown
        if (GameSettings.shouldPlayMusic()) {
            winMusic.play();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Render Video Background first
        if (videoPlayer != null) {
            videoPlayer.update();
            Texture videoTexture = videoPlayer.getTexture();

            if (videoTexture != null) {
                stage.getBatch().begin();

                // Get the scaled world units from the UI Stage's viewport
                float viewWidth = stage.getViewport().getWorldWidth();
                float viewHeight = stage.getViewport().getWorldHeight();

                // Calculate "Fill" mode dimensions (preserves aspect ratio, crops overflow)
                Vector2 size = Scaling.fill.apply(videoTexture.getWidth(), videoTexture.getHeight(), viewWidth, viewHeight);

                // Center the video
                float x = (viewWidth - size.x) / 2f;
                float y = (viewHeight - size.y) / 2f;

                stage.getBatch().draw(videoTexture, x, y, size.x, size.y);
                stage.getBatch().end();
            }
        }

        // 2. Render the Scene2D UI over the video
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        if (videoPlayer != null) videoPlayer.pause();
        if (winMusic != null && winMusic.isPlaying()) winMusic.pause();
    }

    @Override
    public void resume() {
        if (videoPlayer != null) videoPlayer.play();
        if (winMusic != null && GameSettings.shouldPlayMusic()) winMusic.play();
    }

    @Override
    public void hide() {
        if (videoPlayer != null) videoPlayer.pause();
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (videoPlayer != null) videoPlayer.dispose();
        if (winMusic != null) winMusic.dispose();
    }
}
