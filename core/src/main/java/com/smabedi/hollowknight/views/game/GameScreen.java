package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.controllers.CheatController;
import com.smabedi.hollowknight.controllers.PlayerController;
import com.smabedi.hollowknight.models.entities.enemies.*;
import com.smabedi.hollowknight.models.entities.items.Shockwave;
import com.smabedi.hollowknight.models.entities.items.VfxCallback;
import com.smabedi.hollowknight.models.entities.items.VfxInstance;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.npcs.Zote;
import com.smabedi.hollowknight.models.entities.items.VengefulSpirit;
import com.smabedi.hollowknight.models.game.B2WorldCreator;
import com.smabedi.hollowknight.models.game.GameSession;
import com.smabedi.hollowknight.models.game.WorldContactListener;
import com.smabedi.hollowknight.views.entities.*;

public class GameScreen implements Screen {
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GameSession session;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private World world;
    private float accumulator = 0;
    private Knight player;
    private KnightRenderer knightRenderer;
    private Array<Enemy> enemies;
    private Array<EntityRenderer> entityRenderers;
    private Zote zote;
    private ZoteRenderer zoteRenderer;
    private GameUI gameUI;
    private PlayerController playerController;
    private final Box2DDebugRenderer b2dr;
    private SpriteBatch batch;
    public float timeScale = 1f;
    private CheatController cheatController;
    private final Array<Body> bodyBuffer = new Array<>();
    public static final Array<VfxInstance> vfxList = new Array<>();

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
        batch = new SpriteBatch();

        entityRenderers = new Array<>();
        TextureAtlas entityAtlas = Assets.getEntityAtlas();

        assert map != null;
        new B2WorldCreator(world, map);

        VfxCallback vfxCallback = (anim, x, y, offsetX, offsetY, facingRight, defaultFacesRight) -> {
            vfxList.add(new VfxInstance(anim, x, y, offsetX, offsetY, facingRight, defaultFacesRight));
        };
        TextureAtlas knightAtlas = Assets.getKnightAtlas();

        Animation<TextureRegion> damageAnimation = new Animation<>(0.05f, Assets.getVfxAtlas().findRegions("damage_vfx"), Animation.PlayMode.NORMAL);
        enemies = new Array<>();
        for (MapObject object : map.getLayers().get("spawns").getObjects()) {

            float x = ((float) object.getProperties().get("x")) / Constants.World.PPM;
            float y = ((float) object.getProperties().get("y")) / Constants.World.PPM;
            String type = (String) object.getProperties().get("type");

            switch (type) {
                case "player" -> {
                    player = new Knight(world, x, y, session.health, session.soul);
                    knightRenderer = new KnightRenderer(player, Assets.getKnightAtlas());
                }
                case "crawlid" -> {
                    Crawlid crawlid = new Crawlid(world, x, y);
                    enemies.add(crawlid);
                    entityRenderers.add(new CrawlidRenderer(crawlid, entityAtlas));
                }
                case "mossfly" -> {
                    Mossfly mossfly = new Mossfly(world, x, y);
                    enemies.add(mossfly);
                    entityRenderers.add(new MossflyRenderer(mossfly, entityAtlas));
                }
                case "hornhead" -> {
                    HuskHornhead hornhead = new HuskHornhead(world, x, y);
                    enemies.add(hornhead);
                    entityRenderers.add(new HuskHornheadRenderer(hornhead, entityAtlas));
                }
                case "crystal_guardian" -> {
                    CrystalGuardian guardian = new CrystalGuardian(world, x, y, false); // false = starts facing left
                    enemies.add(guardian);
                    entityRenderers.add(new CrystalGuardianRenderer(guardian, entityAtlas));
                }
                case "boss" -> { // Match the Type string from your Tiled map
                    FalseKnight boss = new FalseKnight(world, x, y, vfxCallback, damageAnimation);
                    enemies.add(boss); // Bosses extend Enemy, so they fit in the AI update loop naturally
                    entityRenderers.add(new FalseKnightRenderer(boss, Assets.getBossAtlas()));
                }
                case "zote" -> {
                    zote = new Zote(world, x, y);
                    zoteRenderer = new ZoteRenderer(zote, Assets.getEntityAtlas());
                }
            }
        }

        gameUI = new GameUI(session, player);
        playerController = new PlayerController(player, session.inventory, gameUI, vfxCallback, damageAnimation);
        cheatController = new CheatController(player, gameUI, this);

        world.setContactListener(new WorldContactListener(player, session.inventory, vfxCallback, damageAnimation));
    }

    public void update(float dt) {
        if (gameUI.isPaused()) return;

        // Scale timers and inputs
        float scaledDt = dt * timeScale;
        playerController.handleInput(scaledDt);

        player.update(scaledDt);

        for (int i = vfxList.size - 1; i >= 0; i--) {
            VfxInstance vfx = vfxList.get(i);
            vfx.timer += scaledDt;
            if (vfx.animation.isAnimationFinished(vfx.timer)) {
                vfxList.removeIndex(i);
            }
        }

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
            if (userData instanceof VengefulSpirit sprit) {
                if (sprit.setToDestroy && !sprit.isDestroyed) {
                    world.destroyBody(body);
                    sprit.isDestroyed = true;
                } else if (!sprit.isDestroyed) {
                    sprit.stateTimer += scaledDt;
                }
            } else if (userData instanceof Shockwave wave) {
                if (wave.setToDestroy && !wave.isDestroyed) {
                    world.destroyBody(body);
                    wave.isDestroyed = true;
                } else if (!wave.isDestroyed) {
                    wave.stateTimer += scaledDt;
                }
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

        // 1. Define your Anchor Point (Where does the camera start when the level loads?)
        // You usually get these from your spawn point object!
        float anchorX = 16; // Replace with your actual spawn/start X (scaled by PPM)
        float anchorY = 11;  // Replace with your actual spawn/start Y (scaled by PPM)

        // 2. Store the camera's true position
        float realX = camera.position.x;
        float realY = camera.position.y;

        // 3. Calculate how far the camera has traveled away from the anchor
        float travelX = realX - anchorX;
        float travelY = realY - anchorY;

        // 4. Pre-calculate the Overscan dimensions so we don't repeat math
        float overscan = Constants.UI.OVERSCREEN;
        float viewWidth = camera.viewportWidth + (overscan * 2);
        float viewHeight = camera.viewportHeight + (overscan * 2);

        // =========================================================
        // PASS 1: BACKGROUNDS (Slow Parallax)
        // =========================================================
        // Start at the anchor, and only apply the 0.5f slowdown to the distance traveled!
        camera.position.set(anchorX + (travelX * 0.5f), anchorY + (travelY * 0.5f), 0);
        camera.update();

        // Render visual_background (0) and midgrounds (1, 2)
        renderer.setView(camera.combined,
            camera.position.x - (camera.viewportWidth / 2) - overscan,
            camera.position.y - (camera.viewportHeight / 2) - overscan,
            viewWidth, viewHeight
        );
        renderer.render(new int[]{0});

        // =========================================================
        // PASS 2: TERRAINS (Normal Speed)
        // =========================================================
        camera.position.set(realX, realY, 0);
        camera.update();

        // Render visual_terrain 0, 1, 2 (Layers 3, 4, 5)
        renderer.setView(camera.combined,
            camera.position.x - (camera.viewportWidth / 2) - overscan,
            camera.position.y - (camera.viewportHeight / 2) - overscan,
            viewWidth, viewHeight
        );
        renderer.render(new int[]{1, 2, 3, 4, 5});

        // =========================================================
        // PASS 3: DRAW ENTITIES
        // =========================================================
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw the player
        float renderDelta = delta * timeScale;

        // Draw enemies and Zote first so they appear behind the Knight if they overlap
        //noinspection GDXJavaUnsafeIterator
        for (EntityRenderer entityRenderer : entityRenderers) {
            entityRenderer.render(batch, renderDelta);
        }

        knightRenderer.render(batch, renderDelta);

        // Draw Zote
        if (zoteRenderer != null) {
            zoteRenderer.render(batch, renderDelta);
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // --- DRAW STATIC VFX ---
        //noinspection GDXJavaUnsafeIterator
        for (VfxInstance vfx : vfxList) {
            TextureRegion frame = vfx.animation.getKeyFrame(vfx.timer);

            // Dynamic flipping: If its natural direction doesn't match the required direction, flip it!
            boolean needsFlip = (vfx.facingRight != vfx.defaultFacesRight);
            if (frame.isFlipX() != needsFlip) {
                frame.flip(true, false);
            }

            float w = frame.getRegionWidth() / Constants.World.PPM;
            float h = frame.getRegionHeight() / Constants.World.PPM;

            batch.draw(
                frame,
                vfx.x - (w / 2f) + (vfx.facingRight ? vfx.offsetX : -vfx.offsetX),
                vfx.y - (h / 2f) + vfx.offsetY,
                w, h
            );
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // --- DRAW DYNAMIC PROJECTILES ---
        world.getBodies(bodyBuffer);
        //noinspection GDXJavaUnsafeIterator
        for (Body body : bodyBuffer) {
            Object userData = body.getUserData();

            if (userData instanceof VengefulSpirit sprit && !sprit.isDestroyed) {
                Animation<TextureRegion> anim = sprit.isVoid ? Assets.getVoidSpiritProjectile() : Assets.getSpiritProjectile();
                TextureRegion frame = anim.getKeyFrame(sprit.stateTimer);

                boolean needsFlip = !sprit.facingRight;
                if (frame.isFlipX() != needsFlip) frame.flip(true, false);

                float w = frame.getRegionWidth() / Constants.World.PPM;
                float h = frame.getRegionHeight() / Constants.World.PPM;

                batch.draw(frame, body.getPosition().x - (w / 2f), body.getPosition().y - (h / 2f), w, h);
            }

            // 2. FALSE KNIGHT SHOCKWAVE
            else if (userData instanceof Shockwave wave && !wave.isDestroyed) {
                Animation<TextureRegion> anim = Assets.getShockwaveVfx();

                // Increment timer (if you aren't already doing it in a Shockwave.update() method)
                wave.stateTimer += renderDelta;

                TextureRegion frame = anim.getKeyFrame(wave.stateTimer);

                // Shockwave asset faces Right by default. Check Box2D velocity for direction!
                boolean movingRight = body.getLinearVelocity().x > 0;
                boolean needsFlip = !movingRight;

                if (frame.isFlipX() != needsFlip) frame.flip(true, false);

                float w = frame.getRegionWidth() / Constants.World.PPM;
                float h = frame.getRegionHeight() / Constants.World.PPM;

                // Center it on the Box2D body
                batch.draw(frame, body.getPosition().x - (w / 2f), body.getPosition().y - (h / 2f) + 0.5f, w, h);

                if (anim.isAnimationFinished(wave.stateTimer)) { wave.setToDestroy = true; }
            }
        }

        batch.end();

        // =========================================================
        // PASS 4: FOREGROUNDS (Fast Parallax)
        // =========================================================
        // Move camera faster than the player (1.2f) for things close to the lens
        camera.position.set(realX * 1.2f, realY * 1.2f, 0);
        camera.update();

        renderer.setView(camera.combined,
            camera.position.x - (camera.viewportWidth / 2) - overscan,
            camera.position.y - (camera.viewportHeight / 2) - overscan,
            viewWidth, viewHeight
        );
        // Render visual_foreground (6) and visual_overlay (7)
        renderer.render(new int[]{6, 7});

        // =========================================================
        // PASS 5: DEBUG & UI (Reset to Normal)
        // =========================================================
        // Snap camera back to reality one last time for Box2D lines
        camera.position.set(realX, realY, 0);
        camera.update();

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
