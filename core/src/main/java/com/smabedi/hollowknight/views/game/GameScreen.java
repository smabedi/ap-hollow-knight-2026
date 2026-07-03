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
import com.smabedi.hollowknight.controllers.PlayerController;
import com.smabedi.hollowknight.models.entities.enemies.*;
import com.smabedi.hollowknight.models.entities.knight.Knight;
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
    private GameUI gameUI;
    private PlayerController playerController;
    private final Box2DDebugRenderer b2dr;
    private Array<Enemy> enemies;

    public GameScreen(GameSession session) {
        this.session = session;
        camera = new OrthographicCamera();
        viewport = new FitViewport(
            Constants.UI.DEFAULT_WIDTH / Constants.World.PPM,
            Constants.UI.DEFAULT_HEIGHT / Constants.World.PPM,
            camera);

        // The Debug Renderer draws colored outlines around the hitboxes, for now.
        b2dr = new Box2DDebugRenderer();

        loadMap(session.getLocation());
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

        new B2WorldCreator(world, map);
        player = new Knight(world, session.getPlayerX(), session.getPlayerY());

        enemies = new Array<>();
        enemies.add(new Crawlid(world, session.getPlayerX() + 300f, session.getPlayerY() + 200f));
        enemies.add(new Mossfly(world, session.getPlayerX() + 450f, session.getPlayerY() + 400f));
        enemies.add(new HuskHornhead(world, session.getPlayerX() + 2000f, session.getPlayerY()));
        enemies.add(new CrystalGuardian(world, session.getPlayerX() + 1000f, session.getPlayerY() + 200f, true));

        gameUI = new GameUI();
        playerController = new PlayerController(player);

        world.setContactListener(new WorldContactListener(player));
    }

    public void update(float dt) {
        if (gameUI.isPaused()) return;

        playerController.handleInput(dt);

        float frameTime = Math.min(dt, 0.25f);
        accumulator += frameTime;
        float TIME_STEP = Constants.World.TIME_STEP;
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }

        // Destroy spoiled Vengeful Spirit bodies
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        //noinspection GDXJavaUnsafeIterator
        for (Body body : bodies) {
            if (body.getUserData() instanceof VengefulSpirit sprit) {
                if (sprit.setToDestroy && !sprit.isDestroyed) {
                    world.destroyBody(body);
                    sprit.isDestroyed = true;
                }
            }
        }

        // Tick AI logic for all enemies
        for (int i = 0; i < enemies.size; i++) {
            enemies.get(i).update(dt, player);
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
        multiplexer.addProcessor(gameUI.stage);
        multiplexer.addProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    gameUI.togglePause();
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
