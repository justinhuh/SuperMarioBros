package com.justinhuh.mario.Sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.justinhuh.mario.MarioDemo;
import com.justinhuh.mario.Scenes.Hud;
import com.justinhuh.mario.Screens.PlayScreen;

/**
 * Created by Justin on 9/6/2016.
 */
public class Coin extends InteractiveTileObject{
    private static TiledMapTileSet tileSet;
    private final int BLANK_COIN = 28;
    public Coin(PlayScreen screen, MapObject object) {
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(MarioDemo.COIN_BIT);
    }

    @Override
    //plays proper sound when mario hits coin
    public void onHeadHit(Mario mario) {
        if(getCell().getTile().getId() == BLANK_COIN)
            MarioDemo.manager.get("audio/sounds/bump.wav", Sound.class).play();
        else {
            if(object.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioDemo.PPM), Mushroom.class));
                MarioDemo.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            }
            else
                MarioDemo.manager.get("audio/sounds/coin.wav", Sound.class).play();
        }
        getCell().setTile(tileSet.getTile(BLANK_COIN));
        Hud.addScore(200);
    }
}
