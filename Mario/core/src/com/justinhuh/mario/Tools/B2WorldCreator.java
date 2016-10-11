package com.justinhuh.mario.Tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.justinhuh.mario.MarioDemo;
import com.justinhuh.mario.Screens.PlayScreen;
import com.justinhuh.mario.Sprites.Brick;
import com.justinhuh.mario.Sprites.Coin;
import com.justinhuh.mario.Sprites.Enemy;
import com.justinhuh.mario.Sprites.Goomba;
import com.justinhuh.mario.Sprites.Turtle;

/**
 * Created by Justin on 9/6/2016.
 */
public class B2WorldCreator {
    private Array<Goomba> goombas;
    private Array<Turtle> turtles;

    public B2WorldCreator(PlayScreen screen) {
        World world = screen.getWorld();
        TiledMap map = screen.getMap();
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        //ground bodies
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioDemo.PPM, (rect.getY() + rect.getHeight() / 2) / MarioDemo.PPM);
            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / MarioDemo.PPM, rect.getHeight() / 2 / MarioDemo.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }
        //pipe bodies
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;

            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioDemo.PPM, (rect.getY() + rect.getHeight() / 2) / MarioDemo.PPM);
            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / MarioDemo.PPM, rect.getHeight() / 2 / MarioDemo.PPM);
            fdef.shape = shape;
            fdef.filter.categoryBits = MarioDemo.OBJECT_BIT;
            body.createFixture(fdef);
        }
        //brick bodies
        for (MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {

            new Brick(screen, object);
        }
        //coin bodies
        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {

            new Coin(screen, object);
        }
        //goomba creation and storage
        goombas = new Array<Goomba>();
        for (MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            goombas.add(new Goomba(screen, rect.getX() / MarioDemo.PPM, rect.getY() / MarioDemo.PPM));
        }

        //turtle creation and storage
        turtles = new Array<Turtle>();
        for (MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            turtles.add(new Turtle(screen, rect.getX() / MarioDemo.PPM, rect.getY() / MarioDemo.PPM));
        }
    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }
    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(turtles);
        return enemies;
    }
}