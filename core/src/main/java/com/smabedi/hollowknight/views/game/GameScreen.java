package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.controllers.CheatController;
import com.smabedi.hollowknight.controllers.PlayerController;
import com.smabedi.hollowknight.models.entities.enemies.*;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.npcs.Zote;
import com.smabedi.hollowknight.models.entities.spells.VengefulSpirit;
import com.smabedi.hollowknight.models.game.B2WorldCreator;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.models.game.WorldContactListener;

public class GameScreen implements Screen {
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GameSession session;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private World world;
    private float accumulator = 0;
    private Knight player;
    private Array<Enemy> enemies;
    private Zote zote;
    private GameUI gameUI;
    private PlayerController playerController;
    private final Box2DDebugRenderer b2dr;
    public float timeScale = 1f;
    private CheatController cheatController;
    private final Array<Body> bodyBuffer = new Array<>();

    public GameScreen(GameSession session) {
        this.session = session;
        camera = new OrthographicCamera();
        viewport = new FitViewport(
            Constants.UI.DEFAULT_WIDTH / Constants.World.PPM,
            Constants.UI.DEFAULT_HEIGHT / Constants.World.PPM,
            camera);

        // The Debug Renderer draws colored outlines around the hitboxes, for now.
        b2dr = new Box2DDebugRenderer();

        loadMap(session.location);
    }

    public void loadMap(LocationType location) {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        if (gameUI != null) gameUI.dispose();
        if (world != null) world.dispose();

        world = new World(Constants.World.GRAVITY_VECTOR, true);
        String tmxFile = location.getPath();
        TmxMapLoader mapLoader = new TmxMapLoader();
        map = mapLoader.load(tmxFile);
        renderer = new OrthogonalTiledMapRenderer(map, 1f / Constants.World.PPM);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);

        assert map != null;
        new B2WorldCreator(world, map);
        player = new Knight(world, session.playerX, session.playerY);

        enemies = new Array<>();
//        enemies.add(new Crawlid(world, session.playerX + 300f, session.playerY + 200f));
//        enemies.add(new Mossfly(world, session.playerX + 450f, session.playerY + 400f));
//        enemies.add(new HuskHornhead(world, session.playerX + 2000f, session.playerY));
//        enemies.add(new CrystalGuardian(world, session.playerX + 1000f, session.playerY + 200f, true));
        zote = new Zote(world, session.playerX + 1000f, session.playerY + 100f);
//        enemies.add(new FalseKnight(world, session.playerX + 1000f, session.playerY + 100f));

        gameUI = new GameUI(session.inventory);
        playerController = new PlayerController(player, session.inventory, gameUI);
        cheatController = new CheatController(player, gameUI, this);

        world.setContactListener(new WorldContactListener(player, session.inventory));
    }

    public void update(float dt) {
        if (gameUI.isPaused()) return;

        // Scale timers and inputs
        float scaledDt = dt * timeScale;
        playerController.handleInput(scaledDt);

        // DO NOT scale the frameTime added to the accumulator
        float frameTime = Math.min(dt, 0.25f);
        accumulator += frameTime;
        float TIME_STEP = Constants.World.TIME_STEP;

        while (accumulator >= TIME_STEP) {
            // Scale the physics step internally, keeping 60 smooth frames!
            world.step(TIME_STEP * timeScale, 6, 2);
            accumulator -= TIME_STEP;
        }

        for (int i = 0; i < enemies.size; i++) {
            enemies.get(i).update(scaledDt, player);
        }

        if (zote != null) {
            zote.update(scaledDt, player);
        }

        world.getBodies(bodyBuffer); // LibGDX safely clears and refills this existing array!
        //noinspection GDXJavaUnsafeIterator
        for (Body body : bodyBuffer) {
            Object userData = body.getUserData();
            if (userData instanceof VengefulSpirit sprit && sprit.setToDestroy && !sprit.isDestroyed) {
                world.destroyBody(body);
                sprit.isDestroyed = true;
            } else if (userData instanceof Shockwave wave && wave.setToDestroy && !wave.isDestroyed) {
                world.destroyBody(body);
                wave.isDestroyed = true;
            }
        }

        // Update camera to follow the player with a slight lerp (smoothness)
        camera.position.x += (player.b2body.getPosition().x - camera.position.x) * 0.1f;
        camera.position.y += (player.b2body.getPosition().y - camera.position.y) * 0.1f;

        camera.update();
        renderer.setView(camera);
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        viewport.apply();
        renderer.setView(camera);
        renderer.render();
        b2dr.render(world, camera.combined);

        // Draw the UI Stage ON TOP of the game world
        gameUI.stage.getViewport().apply();
        gameUI.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        gameUI.resize(width, height);
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(cheatController);
        multiplexer.addProcessor(gameUI.stage);
        multiplexer.addProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (gameUI.isInventoryOpen()) {
                        gameUI.toggleInventory(); // Close inventory if it's open
                    } else {
                        gameUI.togglePause(); // Otherwise toggle normal pause
                    }
                    return true;
                }

                // Toggle inventory menu, checking for animation locks!
                if (keycode == GameSettings.getKey(GameSettings.KEY_INVENTORY)) {

                    // Check the actual timers to guarantee we are locked in an animation
                    boolean isAnimationLocked = player.focusTimer > 0
                        || player.wraithsTimer > 0
                        || player.spritCastTimer > 0;

                    // Only allow toggling if we aren't locked, OR if the menu is already open
                    if (!isAnimationLocked || gameUI.isInventoryOpen()) {
                        if (!gameUI.isPaused() || gameUI.isInventoryOpen()) {
                            gameUI.toggleInventory();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
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
        map.dispose();
        renderer.dispose();
        world.dispose();
        gameUI.dispose();
        b2dr.dispose();
    }
}
