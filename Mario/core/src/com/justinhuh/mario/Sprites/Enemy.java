package com.justinhuh.mario.Sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.justinhuh.mario.Screens.PlayScreen;

/**
 * Created by Justin on 9/8/2016.
 */
public abstract class Enemy extends Sprite {
    protected World world;
    protected PlayScreen screen;
    public Body b2body;
    public Vector2 velocity;

    public Enemy(PlayScreen screen, float x, float y){
        this.world = screen.getWorld();
        this.screen = screen;
        setPosition(x, y);
        defineEnemy();
        velocity = new Vector2(1, 0);
        b2body.setActive(false); //sleeps enemy until mario gets near
    }

    protected abstract void defineEnemy();
    public abstract void update(float dt);
    public abstract void hitOnHead(Mario mario);
    public abstract void onEnemyHit(Enemy enemy);

    //enemies switch direction if they come in contact with objects
    public void reverseVelocity(boolean x, boolean y){
        if(x)
            velocity.x =  -velocity.x;
        if(y)
            velocity.y = -velocity.y;
    }
}
