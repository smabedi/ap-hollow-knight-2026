package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.views.game.LocationType;

/**
 * Utility class responsible for parsing TiledMap layers and generating
 * corresponding Box2D physics bodies and fixtures.
 * Constructs static environment geometry, hazard zones, and event triggers.
 */
public class B2WorldCreator {

    /**
     * Iterates through the provided TiledMap layers and builds the physical game world.
     *
     * @param world   The active Box2D world context.
     * @param map     The loaded Tiled map object.
     * @param session The active game session tracking player state and arena limits.
     */
    public B2WorldCreator(World world, TiledMap map, GameSession session) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        // Establish the operational boundaries for the camera based on map properties
        int mapWidthInTiles = map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        int tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        int tilePixelHeight = map.getProperties().get("tileheight", Integer.class);

        session.mapMinX = 0;
        session.mapMinY = 0;
        session.mapMaxX = (mapWidthInTiles * tilePixelWidth) / Constants.World.PPM;
        session.mapMaxY = (mapHeightInTiles * tilePixelHeight) / Constants.World.PPM;

        // Instantiate solid structural colliders for the map ground
        for (MapObject object : map.getLayers().get("ground").getObjects()) {

            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);

                body = world.createBody(bodyDef);

                shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getHeight() / 2) / Constants.World.PPM);
                fixtureDef.shape = shape;
                fixtureDef.friction = 0.5f;

                body.createFixture(fixtureDef).setUserData("ground");

            } else if (object instanceof PolygonMapObject polygonObject) {
                float[] vertices = polygonObject.getPolygon().getTransformedVertices();
                float[] worldVertices = new float[vertices.length];

                for (int i = 0; i < vertices.length; i++) {
                    worldVertices[i] = vertices[i] / Constants.World.PPM;
                }

                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set(0, 0);

                body = world.createBody(bodyDef);

                shape.set(worldVertices);
                fixtureDef.shape = shape;
                fixtureDef.friction = 0.5f;

                body.createFixture(fixtureDef).setUserData("ground");
            }
        }

        // Instantiate non-solid hazard regions for spikes
        for (MapObject object : map.getLayers().get("spikes").getObjects()) {

            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);

                body = world.createBody(bodyDef);

                shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getHeight() / 2) / Constants.World.PPM);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;

                body.createFixture(fixtureDef).setUserData("spikes");

            } else if (object instanceof PolygonMapObject polygonObject) {
                float[] vertices = polygonObject.getPolygon().getTransformedVertices();
                float[] worldVertices = new float[vertices.length];

                for (int i = 0; i < vertices.length; i++) {
                    worldVertices[i] = vertices[i] / Constants.World.PPM;
                }

                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set(0, 0);

                body = world.createBody(bodyDef);

                shape.set(worldVertices);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;

                body.createFixture(fixtureDef).setUserData("spikes");
            }
        }

        // Establish the entry detection zones for initiating boss fights
        if (map.getLayers().get("arena_sensors") != null) {
            for (MapObject object : map.getLayers().get("arena_sensors").getObjects()) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);
                body = world.createBody(bodyDef);
                shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getHeight() / 2) / Constants.World.PPM);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;
                body.createFixture(fixtureDef).setUserData("arena_sensor");
            }
        }

        // Prepare physical arena gates (initialized as permeable sensors)
        if (map.getLayers().get("arena_gates") != null) {
            for (MapObject object : map.getLayers().get("arena_gates").getObjects()) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);
                body = world.createBody(bodyDef);
                shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getHeight() / 2) / Constants.World.PPM);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;
                body.createFixture(fixtureDef).setUserData("arena_gate");
            }
        }

        // Restrict camera movement to the arena boundaries during boss sequences
        if (map.getLayers().get("camera_bounds") != null) {
            for (MapObject object : map.getLayers().get("camera_bounds").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();

                    session.arenaMinX = rect.getX() / Constants.World.PPM;
                    session.arenaMinY = rect.getY() / Constants.World.PPM;
                    session.arenaMaxX = (rect.getX() + rect.getWidth()) / Constants.World.PPM;
                    session.arenaMaxY = (rect.getY() + rect.getHeight()) / Constants.World.PPM;
                }
            }
        }

        // Establish safe respawn checkpoints
        if (map.getLayers().get("safe_spots") != null) {
            for (MapObject object : map.getLayers().get("safe_spots").getObjects()) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);

                body = world.createBody(bodyDef);

                shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getHeight() / 2) / Constants.World.PPM);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;

                body.createFixture(fixtureDef).setUserData("safe_spot");
            }
        }

        // Configure environmental mapping transitions
        if (map.getLayers().get("transitions") != null) {
            for (MapObject object : map.getLayers().get("transitions").getObjects()) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);
                body = world.createBody(bodyDef);

                String targetMapStr = object.getProperties().get("target_map", String.class);
                LocationType targetLoc = LocationType.valueOf(targetMapStr);

                body.setUserData(new TransitionData(targetLoc));

                shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getHeight() / 2) / Constants.World.PPM);
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;

                body.createFixture(fixtureDef).setUserData("transition_sensor");
            }
        }

        shape.dispose();
    }
}
