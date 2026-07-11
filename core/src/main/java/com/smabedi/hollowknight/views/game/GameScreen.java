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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.controllers.AchievementManager;
import com.smabedi.hollowknight.controllers.CheatController;
import com.smabedi.hollowknight.controllers.PlayerController;
import com.smabedi.hollowknight.models.entities.enemies.*;
import com.smabedi.hollowknight.models.entities.items.Shockwave;
import com.smabedi.hollowknight.controllers.EventCallback;
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
    private AmbientParticles ambientParticles;
    private AchievementManager achievementManager;

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
        if (ambientParticles != null) ambientParticles.dispose();

        world = new World(Constants.World.GRAVITY_VECTOR, true);
        String tmxFile = location.getPath();
        TmxMapLoader mapLoader = new TmxMapLoader();
        map = mapLoader.load(tmxFile);
        renderer = new OrthogonalTiledMapRenderer(map, 1f / Constants.World.PPM);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);
        batch = new SpriteBatch();

        // --- AUDIO & PARTICLE LOGIC ---
        // 1. Initialize Particles (It auto-detects the map internally!)
        ambientParticles = new AmbientParticles(location);

        // 2. Handle Music Swaps
        // TODO: BGM management here.
        // (Assuming you have an AudioManager or getMusic() in your Assets class)
        switch (location) {
            case FORGOTTEN_CROSSROADS -> {
                // e.g., AudioManager.playMusic(Assets.getMusic("crossroads_bgm"));
            }
            case GREENPATH -> {
                // e.g., AudioManager.playMusic(Assets.getMusic("greenpath_bgm"));
            }
        }

        entityRenderers = new Array<>();
        TextureAtlas entityAtlas = Assets.getEntityAtlas();

        assert map != null;
        new B2WorldCreator(world, map, session);

        EventCallback eventCallback = new EventCallback() {
            @Override
            public void spawnStaticVfx(Animation<TextureRegion> anim, float x, float y, float offsetX, float offsetY, boolean facingRight, boolean defaultFacesRight) {
                vfxList.add(new VfxInstance(anim, x, y, offsetX, offsetY, facingRight, defaultFacesRight));
            }

            @Override
            public void addCameraTrauma(float amount) {
                session.addTrauma(amount);
            }

            @Override
            public void setCameraTrauma(float amount) {
                session.setTrauma(amount);
            }

            @Override
            public void onBossDeath() {
                session.isArenaLocked = false;
                achievementManager.evaluateBossDefeat();
                System.out.println("Boss died. Arena unlocked.");
            }

            @Override
            public void onPlayerDeath() {
                session.isArenaLocked = false;
                session.pendingDeathRespawn = true;
                session.deathCounter++;
                System.out.println("Player died. Arena unlocked. Respawning.");
            }

            @Override
            public void onEnemyDeath(EnemyType enemyType) {
                session.enemyKillCounter++;
                achievementManager.evaluateEnemyKill(enemyType);
            }
        };

        Animation<TextureRegion> damageAnimation = new Animation<>(0.05f, Assets.getVfxAtlas().findRegions("damage_vfx"), Animation.PlayMode.NORMAL);
        enemies = new Array<>();
        for (MapObject object : map.getLayers().get("spawns").getObjects()) {
            float x = ((float) object.getProperties().get("x")) / Constants.World.PPM;
            float y = ((float) object.getProperties().get("y")) / Constants.World.PPM;
            String type = (String) object.getProperties().get("type");

            switch (type) {
                case "player" -> {
                    if (session.initialSpawnX == -1f) {
                        session.initialSpawnX = x;
                        session.initialSpawnY = y;
                    }

                    float spawnX = (session.playerX != -1f) ? session.playerX : x;
                    float spawnY = (session.playerY != -1f) ? session.playerY : y;

                    player = new Knight(world, spawnX, spawnY, session.health, session.soul, eventCallback);

                    // Synchronize session and initial safe spot
                    session.playerX = spawnX;
                    session.playerY = spawnY;
                    if (session.lastSafeX == -1f) {
                        session.lastSafeX = spawnX;
                        session.lastSafeY = spawnY;
                    }

                    knightRenderer = new KnightRenderer(player, Assets.getKnightAtlas());
                }
                case "boss_teleport" -> {
                    session.bossTeleportX = x;
                    session.bossTeleportY = y;
                }
                case "crawlid" -> {
                    Crawlid crawlid = new Crawlid(world, x, y, eventCallback);
                    enemies.add(crawlid);
                    entityRenderers.add(new CrawlidRenderer(crawlid, entityAtlas));
                }
                case "mossfly" -> {
                    Mossfly mossfly = new Mossfly(world, x, y, eventCallback);
                    enemies.add(mossfly);
                    entityRenderers.add(new MossflyRenderer(mossfly, entityAtlas));
                }
                case "hornhead" -> {
                    HuskHornhead hornhead = new HuskHornhead(world, x, y, eventCallback);
                    enemies.add(hornhead);
                    entityRenderers.add(new HuskHornheadRenderer(hornhead, entityAtlas));
                }
                case "crystal_guardian" -> {
                    CrystalGuardian guardian = new CrystalGuardian(world, x, y, false, eventCallback); // false = starts facing left
                    enemies.add(guardian);
                    entityRenderers.add(new CrystalGuardianRenderer(guardian, entityAtlas));
                }
                case "boss" -> { // Match the Type string from your Tiled map
                    FalseKnight boss = new FalseKnight(world, x, y, eventCallback, damageAnimation);
                    enemies.add(boss); // Bosses extend Enemy, so they fit in the AI update loop naturally
                    entityRenderers.add(new FalseKnightRenderer(boss, Assets.getBossAtlas()));
                }
                case "zote" -> {
                    zote = new Zote(world, x, y);
                    zoteRenderer = new ZoteRenderer(zote, Assets.getEntityAtlas());
                }
            }
        }

        // --- NEW: Execute Cross-Map Boss Teleport ---
        if (session.pendingBossTeleport && session.bossTeleportX != -1f) {
            // Snap the player to the teleport point
            assert player != null;
            player.b2body.setTransform(session.bossTeleportX, session.bossTeleportY, 0);

            // Sync all session coordinates so saving and respawning work perfectly
            session.playerX = session.bossTeleportX;
            session.playerY = session.bossTeleportY;
            session.lastSafeX = session.bossTeleportX;
            session.lastSafeY = session.bossTeleportY;

            session.pendingBossTeleport = false; // Consume the flag
            System.out.println("Cross-map boss teleport complete.");
        }

        gameUI = new GameUI(session, player);
        achievementManager = new AchievementManager(session, gameUI);
        playerController = new PlayerController(player, session.inventory, gameUI, eventCallback, damageAnimation);
        cheatController = new CheatController(player, gameUI, this, session);

        world.setContactListener(new WorldContactListener(player, session.inventory, session, eventCallback, damageAnimation));

        setupInputProcessors();
    }

    public void update(float dt) {
        if (gameUI.isPaused()) return;

        // --- MAP TRANSITION EXECUTION ---
        if (session.pendingTransition) {
            session.location = session.nextLocation;

            // Force the engine to use the target map's default player_spawn
            session.playerX = -1f;
            session.playerY = -1f;

            // Reset safe spots so we don't teleport back to the old map
            session.lastSafeX = -1f;
            session.lastSafeY = -1f;

            session.pendingTransition = false;
            vfxList.clear();
            loadMap(session.location);
            return;
        }

        // Scale timers and inputs
        float scaledDt = dt * timeScale;
        playerController.handleInput(scaledDt);

        player.update(scaledDt);

        session.update(scaledDt, dt);

        for (int i = vfxList.size - 1; i >= 0; i--) {
            VfxInstance vfx = vfxList.get(i);
            vfx.timer += scaledDt;
            if (vfx.animation.isAnimationFinished(vfx.timer)) {
                vfxList.removeIndex(i);
            }
        }

        // Safely solidify gates (index-based iteration)
        if (session.isArenaLocked && !session.arenaGatesSolidified) {
            world.getBodies(bodyBuffer);

            for (int i = 0; i < bodyBuffer.size; i++) {
                Body b = bodyBuffer.get(i);
                // It is safe to access the fixture list as we are not
                // destroying the body, just toggling a property.
                //noinspection GDXJavaUnsafeIterator
                for (Fixture f : b.getFixtureList()) {
                    if ("arena_gate".equals(f.getUserData())) {
                        f.setSensor(false); // Wall is now solid
                    }
                }
            }
            session.arenaGatesSolidified = true;
        }

        // Safely OPEN gates when arena is unlocked
        if (!session.isArenaLocked && session.arenaGatesSolidified) {
            world.getBodies(bodyBuffer);
            for (int i = 0; i < bodyBuffer.size; i++) {
                Body b = bodyBuffer.get(i);
                //noinspection GDXJavaUnsafeIterator
                for (Fixture f : b.getFixtureList()) {
                    if ("arena_gate".equals(f.getUserData())) {
                        f.setSensor(true); // Gates become ghosts again
                    }
                }
            }
            session.arenaGatesSolidified = false;
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

        // 1. UI Check: Did we just discover a new checkpoint?
        if (session.safeSpotUpdated) {
            gameUI.showToast(Assets.getString("checkpoint_reached"));
            session.safeSpotUpdated = false; // Consume the flag
        }

        // 2. Execute DEATH Respawn (Returns to Level Start)
        if (session.pendingDeathRespawn) {
            player.b2body.setTransform(session.initialSpawnX, session.initialSpawnY, 0);
            player.b2body.setLinearVelocity(0, 0);

            // Reset the safe spot to the start point!
            session.lastSafeX = session.initialSpawnX;
            session.lastSafeY = session.initialSpawnY;

            // You can add a localization string for "You Died!" later
            gameUI.showToast(Assets.getString("death_respawning"));

            session.pendingDeathRespawn = false;
        }
        // 3. Execute SPIKE Respawn (Returns to Last Checkpoint)
        else if (session.pendingRespawn) {
            player.b2body.setTransform(session.lastSafeX, session.lastSafeY, 0);
            player.b2body.setLinearVelocity(0, 0);

            gameUI.showToast(Assets.getString("checkpoint_respawning"));

            session.pendingRespawn = false;
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

        // 3. APPLY CAMERA LOGIC (Lerp, Clamp)
        float targetX = player.b2body.getPosition().x;
        float targetY = player.b2body.getPosition().y;

        camera.position.x += (targetX - camera.position.x) * 0.1f;
        camera.position.y += (targetY - camera.position.y) * 0.1f;

        float camHalfWidth = camera.viewportWidth / 2f;
        float camHalfHeight = camera.viewportHeight / 2f;

        float currentMinX = session.isArenaLocked ? session.arenaMinX : session.mapMinX;
        float currentMaxX = session.isArenaLocked ? session.arenaMaxX : session.mapMaxX;
        float currentMinY = session.isArenaLocked ? session.arenaMinY : session.mapMinY;
        float currentMaxY = session.isArenaLocked ? session.arenaMaxY : session.mapMaxY;

        // X-Axis Clamp
        if ((currentMaxX - currentMinX) <= camera.viewportWidth) {
            camera.position.x = currentMinX + (currentMaxX - currentMinX) / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, currentMinX + camHalfWidth, currentMaxX - camHalfWidth);
        }

        // Y-Axis Clamp
        if ((currentMaxY - currentMinY) <= camera.viewportHeight) {
            camera.position.y = currentMinY + (currentMaxY - currentMinY) / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, currentMinY + camHalfHeight, currentMaxY - camHalfHeight);
        }

        camera.update();
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        float anchorX = 16;
        float anchorY = 11;

        // Store the pure logical position calculated by update()
        float realX = camera.position.x;
        float realY = camera.position.y;
        float travelX = realX - anchorX;
        float travelY = realY - anchorY;

        // --- CALCULATE SHAKE OFFSET FOR THIS FRAME ---
        float shakeOffsetX = 0f;
        float shakeOffsetY = 0f;

        // Add the !gameUI.isPaused() check here:
        if (session.shakeTrauma > 0 && !gameUI.isPaused()) {
            float shake = session.shakeTrauma * session.shakeTrauma * session.shakeTrauma;
            shakeOffsetX = Constants.Camera.MAX_SHAKE_OFFSET_X * shake * MathUtils.random(-1f, 1f);
            shakeOffsetY = Constants.Camera.MAX_SHAKE_OFFSET_Y * shake * MathUtils.random(-1f, 1f);
        }

        float overscan = Constants.UI.OVERSCREEN;
        float viewWidth = camera.viewportWidth + (overscan * 2);
        float viewHeight = camera.viewportHeight + (overscan * 2);

        // =========================================================
        // PASS 1: BACKGROUNDS (Slow Parallax)
        // =========================================================
        // ADD the shake offsets to the parallax math!
        camera.position.set(anchorX + (travelX * 0.5f) + shakeOffsetX, anchorY + (travelY * 0.5f) + shakeOffsetY, 0);
        camera.update();

        renderer.setView(camera.combined,
            camera.position.x - (camera.viewportWidth / 2) - overscan,
            camera.position.y - (camera.viewportHeight / 2) - overscan,
            viewWidth, viewHeight
        );
        renderer.render(new int[]{0});

        // =========================================================
        // PASS 2: TERRAINS (Normal Speed)
        // =========================================================
        // ADD the shake offsets to the true position!
        camera.position.set(realX + shakeOffsetX, realY + shakeOffsetY, 0);
        camera.update();

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

        // Set to 0f if paused, freezing all entity animations & particles
        float renderDelta = gameUI.isPaused() ? 0f : (delta * timeScale);

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
        camera.position.set((realX * 1.3f) + shakeOffsetX, (realY * 1.3f) + shakeOffsetY, 0);
        camera.update();

        renderer.setView(camera.combined,
            camera.position.x - (camera.viewportWidth / 2) - overscan,
            camera.position.y - (camera.viewportHeight / 2) - overscan,
            viewWidth, viewHeight
        );
        renderer.render(new int[]{6, 7});

        // --- NEW: RENDER AMBIENT PARTICLES (Foreground Parallax) ---
        // Apply a 1.1x parallax multiplier so they float slightly in front of the gameplay layer
        camera.position.set((realX * 1.5f) + shakeOffsetX, (realY * 1.5f) + shakeOffsetY, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // We pass timeScale so particles pause when the game pauses or time dilates!
        ambientParticles.updateAndRender(delta * timeScale, camera, batch);

        // =========================================================
        // PASS 5: DEBUG & UI (Reset to Normal)
        // =========================================================
        // Snap camera back to the true position WITHOUT shake for debug lines
        // so the hitboxes don't wobble away from the bodies!
        camera.position.set(realX, realY, 0);
        camera.update();

        b2dr.render(world, camera.combined);

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
        setupInputProcessors();
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

    private void setupInputProcessors() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(cheatController);
        multiplexer.addProcessor(gameUI.stage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (gameUI.isInventoryOpen()) {
                        gameUI.toggleInventory();
                    } else {
                        gameUI.togglePause();
                    }
                    return true;
                }

                if (keycode == GameSettings.getKey(GameSettings.KEY_INVENTORY)) {
                    boolean isAnimationLocked = player.focusTimer > 0
                        || player.wraithsTimer > 0
                        || player.spritCastTimer > 0;

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

        // Tell LibGDX to use our fresh multiplexer!
        Gdx.input.setInputProcessor(multiplexer);
    }

    public void rebuildUI() {
        // Remember the current states
        boolean wasPaused = gameUI != null && gameUI.isPaused();
        boolean wasInventoryOpen = gameUI != null && gameUI.isInventoryOpen();

        if (gameUI != null) {
            gameUI.dispose();
        }

        // Rebuild with new language strings
        gameUI = new GameUI(session, player);

        // --- NEW: Update the controllers with the fresh UI reference! ---
        if (cheatController != null) cheatController.setGameUI(gameUI);
        if (playerController != null) playerController.setGameUI(gameUI);
        if (achievementManager != null) achievementManager.setGameUI(gameUI);

        // Restore the states
        if (wasInventoryOpen) {
            gameUI.toggleInventory();
        } else if (wasPaused) {
            gameUI.togglePause();
        }

        // Rebind the input multiplexer so the new GameUI stage receives clicks
        setupInputProcessors();
    }
}
