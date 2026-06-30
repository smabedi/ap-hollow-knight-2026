package com.smabedi.hollowknight.models.game;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.smabedi.hollowknight.config.Constants;

public class B2WorldCreator {

    @SuppressWarnings("GDXJavaUnsafeIterator")
    public B2WorldCreator(World world, TiledMap map) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        // Generate Static GROUND Hitboxes
        for (RectangleMapObject object : map.getLayers().get("ground").getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = object.getRectangle();

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);

            body = world.createBody(bodyDef);

            shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                (rect.getHeight() / 2) / Constants.World.PPM);
            fixtureDef.shape = shape;
            fixtureDef.friction = 0.5f;

            body.createFixture(fixtureDef).setUserData("ground");
        }

        // Generate Static SPIKES Hitboxes
        for (RectangleMapObject object : map.getLayers().get("spikes").getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = object.getRectangle();

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / Constants.World.PPM,
                (rect.getY() + rect.getHeight() / 2) / Constants.World.PPM);

            body = world.createBody(bodyDef);

            shape.setAsBox((rect.getWidth() / 2) / Constants.World.PPM,
                (rect.getHeight() / 2) / Constants.World.PPM);
            fixtureDef.shape = shape;
            fixtureDef.isSensor = true; // Sensors detect overlap but don't block movement

            body.createFixture(fixtureDef).setUserData("spikes");
        }

        shape.dispose();
    }
}
