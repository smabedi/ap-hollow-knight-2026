package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.views.game.LocationType;

public class B2WorldCreator {

    public B2WorldCreator(World world, TiledMap map, GameSession session) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        // 1. Extract Global Map Dimensions automatically
        int mapWidthInTiles = map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        int tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        int tilePixelHeight = map.getProperties().get("tileheight", Integer.class);

        session.mapMinX = 0;
        session.mapMinY = 0;
        session.mapMaxX = (mapWidthInTiles * tilePixelWidth) / Constants.World.PPM;
        session.mapMaxY = (mapHeightInTiles * tilePixelHeight) / Constants.World.PPM;

        // Generate Static GROUND Hitboxes
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
                // Transformed vertices already contain world positions, so the body anchor is 0,0
                bodyDef.position.set(0, 0);

                body = world.createBody(bodyDef);

                shape.set(worldVertices);
                fixtureDef.shape = shape;
                fixtureDef.friction = 0.5f;

                body.createFixture(fixtureDef).setUserData("ground");
            }
        }

        // Generate Static SPIKES Hitboxes
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
                fixtureDef.isSensor = true; // Sensors detect overlap but don't block movement

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

        // Generate Arena Sensors (Trigger)
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

        // Generate Arena Gates (Initially sensors so player can walk in)
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
                fixtureDef.isSensor = true; // STARTS AS SENSOR
                body.createFixture(fixtureDef).setUserData("arena_gate");
            }
        }

        if (map.getLayers().get("camera_bounds") != null) {
            for (MapObject object : map.getLayers().get("camera_bounds").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();

                    // Convert pixels to meters (PPM) and store in session
                    session.arenaMinX = rect.getX() / Constants.World.PPM;
                    session.arenaMinY = rect.getY() / Constants.World.PPM;
                    session.arenaMaxX = (rect.getX() + rect.getWidth()) / Constants.World.PPM;
                    session.arenaMaxY = (rect.getY() + rect.getHeight()) / Constants.World.PPM;
                }
            }
        }

        // Generate Safe Spot Sensors
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

        // Generate Transition Sensors
        if (map.getLayers().get("transitions") != null) {
            for (MapObject object : map.getLayers().get("transitions").getObjects()) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                    (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);
                body = world.createBody(bodyDef);

                // Fetch custom properties from Tiled
                String targetMapStr = object.getProperties().get("target_map", String.class);
                LocationType targetLoc = LocationType.valueOf(targetMapStr);

                // Attach our data class directly to the BODY
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
