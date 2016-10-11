package com.justinhuh.mario.Sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.justinhuh.mario.MarioDemo;
import com.justinhuh.mario.Scenes.Hud;
import com.justinhuh.mario.Screens.PlayScreen;

/**
 * Created by Justin on 9/6/2016.
 */
public class Brick extends InteractiveTileObject {
    public Brick(PlayScreen screen, MapObject object){
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(MarioDemo.BRICK_BIT);
    }

    //destroys bricks on hit
    @Override
    public void onHeadHit(Mario mario) {
        if (mario.isBig()) {
            setCategoryFilter(MarioDemo.DESTROYED_BIT);
            getCell().setTile(null);
            Hud.addScore(200);
            MarioDemo.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
        }
        MarioDemo.manager.get("audio/sounds/bump.wav", Sound.class).play();
    }
}
