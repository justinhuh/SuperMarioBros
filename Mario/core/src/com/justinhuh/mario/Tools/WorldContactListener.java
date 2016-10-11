package com.justinhuh.mario.Tools;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.justinhuh.mario.MarioDemo;
import com.justinhuh.mario.Sprites.Enemy;
import com.justinhuh.mario.Sprites.InteractiveTileObject;
import com.justinhuh.mario.Sprites.Item;
import com.justinhuh.mario.Sprites.Mario;

/**
 * Created by Justin on 9/8/2016.
 */
public class WorldContactListener implements ContactListener {
    @Override
    //listens for contact/collision and behaves appropriately
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch(cDef){
            case MarioDemo.MARIO_HEAD_BIT | MarioDemo.BRICK_BIT:
            case MarioDemo.MARIO_HEAD_BIT | MarioDemo.COIN_BIT:
                if (fixA.getFilterData().categoryBits == MarioDemo.MARIO_HEAD_BIT)
                    ((InteractiveTileObject) fixB.getUserData()).onHeadHit((Mario) fixA.getUserData());
                else
                    ((InteractiveTileObject) fixA.getUserData()).onHeadHit((Mario) fixB.getUserData());
                break;
            case MarioDemo.ENEMY_HEAD_BIT | MarioDemo.MARIO_BIT: //mario jumps on top of enemy
                if (fixA.getFilterData().categoryBits == MarioDemo.ENEMY_HEAD_BIT)
                    ((Enemy)fixA.getUserData()).hitOnHead((Mario) fixB.getUserData());
                else
                    ((Enemy)fixB.getUserData()).hitOnHead((Mario) fixA.getUserData());
                break;
            case MarioDemo.ENEMY_BIT | MarioDemo.OBJECT_BIT: //enemy runs into an object
                if (fixA.getFilterData().categoryBits == MarioDemo.ENEMY_BIT)
                    ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                else
                    ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                break;
            case MarioDemo.MARIO_BIT | MarioDemo.ENEMY_BIT: //mario runs into enemy
                if (fixA.getFilterData().categoryBits == MarioDemo.MARIO_BIT)
                    ((Mario) fixA.getUserData()).hit((Enemy) fixB.getUserData());
                else
                    ((Mario) fixB.getUserData()).hit((Enemy) fixA.getUserData());
                break;
            case MarioDemo.ENEMY_BIT | MarioDemo.ENEMY_BIT:
                ((Enemy)fixA.getUserData()).onEnemyHit((Enemy)fixB.getUserData());
                ((Enemy)fixB.getUserData()).onEnemyHit((Enemy)fixA.getUserData());
                break;
            case MarioDemo.ITEM_BIT | MarioDemo.OBJECT_BIT: //Item runs into an object
                if (fixA.getFilterData().categoryBits == MarioDemo.ITEM_BIT)
                    ((Item)fixA.getUserData()).reverseVelocity(true, false);
                else
                    ((Item)fixB.getUserData()).reverseVelocity(true, false);
                break;
            case MarioDemo.ITEM_BIT | MarioDemo.MARIO_BIT: //Mario runs into an item
                if (fixA.getFilterData().categoryBits == MarioDemo.ITEM_BIT)
                    ((Item)fixA.getUserData()).use((Mario) fixB.getUserData());
                else
                    ((Item)fixB.getUserData()).use((Mario) fixA.getUserData());
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
