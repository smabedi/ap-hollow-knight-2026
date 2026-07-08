package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class B2WorldCreator {

    public B2WorldCreator(World world, TiledMap map) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

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

        shape.dispose();
    }
}
