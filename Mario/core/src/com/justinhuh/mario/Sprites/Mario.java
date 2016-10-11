package com.justinhuh.mario.Sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.justinhuh.mario.MarioDemo;
import com.justinhuh.mario.Screens.PlayScreen;

/**
 * Created by Justin on 9/6/2016.
 */
public class Mario extends Sprite {
    public enum State {FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD, WIN};
    public State currentState;
    public State previousState;

    public World world;
    public Body b2body;

    private TextureRegion marioStand;
    private Animation marioRun;
    private TextureRegion marioJump;
    private TextureRegion marioDead;
    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private Animation bigMarioRun;
    private Animation growMario;

    private float stateTimer;
    private boolean runningRight;
    private boolean marioIsBig;
    private boolean runGrowAnimation;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;
    private boolean marioIsDead;
    private boolean soundFlag = false;
    private boolean deathByEnemy = false;
    private boolean win = false;

    public void grow() {
        if(!marioIsBig) {
            runGrowAnimation = true;
            marioIsBig = true;
            timeToDefineBigMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() * 2);
            MarioDemo.manager.get("audio/sounds/powerup.wav", Sound.class).play();
        }
    }

    public boolean isDead(){
        return marioIsDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public boolean isBig(){ return marioIsBig;}

    public void hit(Enemy enemy){
        if(enemy instanceof Turtle && ((Turtle)enemy).getCurrentState() == Turtle.State.STANDING_SHELL){
            ((Turtle) enemy).kick(this.getX() <= enemy.getX() ? Turtle.KICK_RIGHT_SPEED : Turtle.KICK_LEFT_SPEED);

        }else {
            if (marioIsBig) {
                marioIsBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() / 2);
                MarioDemo.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
            } else {
                deathByEnemy = true;
                MarioDemo.manager.get("audio/music/mario_music.ogg", Music.class).stop();
                MarioDemo.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
                marioIsDead = true;
                Filter filter = new Filter();
                filter.maskBits = MarioDemo.NOTHING_BIT;
                for (Fixture fixture : b2body.getFixtureList())
                    fixture.setFilterData(filter);
                b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
            }
        }
    }

    public Mario (PlayScreen screen){
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        Array<TextureRegion> frames = new Array<TextureRegion>();

        //Mario running textures
        for(int i = 1; i < 4; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        marioRun = new Animation(0.1f, frames);
        frames.clear();

        for(int i = 1; i < 4; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        bigMarioRun = new Animation(0.1f, frames);
        frames.clear();

        //Mario growing textures
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation(0.2f, frames);

        //Mario Jumping textures
        for (int i = 4; i < 6; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i*16, 10, 16, 16));
        marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"),80, 0, 16, 16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"),80, 0, 16, 32);

        //Mario Stand texture
        marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 2*16,0,16,16);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);

        //Mario Dead texture
        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96, 0, 16, 16);

        //define Mario with a Box2D body
        defineMario();

        //set Mario's bounds
        setBounds(0,0,16 / MarioDemo.PPM,16 /MarioDemo.PPM);
        setRegion(marioStand);
    }

    //set Mario skin to box2d object
    public void update(float dt){
        if(marioIsBig)
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() /2 - 6 / MarioDemo.PPM);
        else
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() /2);
        setRegion(getFrame(dt));
        if(timeToDefineBigMario)
            defineBigMario();
        if(timeToRedefineMario)
            redefineMario();
        if(b2body.getPosition().y < -2 && !soundFlag && !deathByEnemy) {
            marioIsDead = true;
            soundFlag = true;
            MarioDemo.manager.get("audio/music/mario_music.ogg", Music.class).stop();
            MarioDemo.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
        }
        if(b2body.getPosition().x > 31.9) {
            win = true;
            marioIsDead = true;
        }
    }

    public TextureRegion getFrame(float dt){
        currentState = getState();

        TextureRegion region;
        switch(currentState){
            case WIN:
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer);
                if(growMario.isAnimationFinished(stateTimer))
                    runGrowAnimation = false;
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }
        //Determine whether Mario is moving left or right and fix texture accordingly
        if ((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()){
            region.flip(true, false);
            runningRight = false;
        }
        else if ((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()){
            region.flip(true, false);
            runningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    //returns state of Mario
    public State getState() {
        if (marioIsDead && win)
            return State.WIN;
        else if (marioIsDead)
            return State.DEAD;
        else if (runGrowAnimation)
            return State.GROWING;
        else if(b2body.getLinearVelocity().y > 0 || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) //continue jump animation if Mario jumped
            return State.JUMPING;
        else if(b2body.getLinearVelocity().y < 0)
            return State.FALLING;
        else if(b2body.getLinearVelocity().x != 0)
            return State.RUNNING;
        else
            return State.STANDING;
    }

    //for when Mario is big but gets hit by enemy
    public void redefineMario(){
        Vector2 position = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioDemo.PPM);
        fdef.filter.categoryBits = MarioDemo.MARIO_BIT;
        fdef.filter.maskBits = MarioDemo.GROUND_BIT
                | MarioDemo.COIN_BIT
                | MarioDemo.BRICK_BIT
                | MarioDemo.OBJECT_BIT
                | MarioDemo.ENEMY_BIT
                | MarioDemo.ENEMY_HEAD_BIT
                | MarioDemo.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        //create head object
        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioDemo.PPM, 6 / MarioDemo.PPM), new Vector2(2 / MarioDemo.PPM, 6 /MarioDemo.PPM));
        fdef.filter.categoryBits = MarioDemo.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData(this);
        timeToRedefineMario = false;
    }

    //define Body for Big Mario
    public void defineBigMario(){
        Vector2 currentPosition = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPosition.add(0, 10 / MarioDemo.PPM));
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioDemo.PPM);
        fdef.filter.categoryBits = MarioDemo.MARIO_BIT;
        fdef.filter.maskBits = MarioDemo.GROUND_BIT
                | MarioDemo.COIN_BIT
                | MarioDemo.BRICK_BIT
                | MarioDemo.OBJECT_BIT
                | MarioDemo.ENEMY_BIT
                | MarioDemo.ENEMY_HEAD_BIT
                | MarioDemo.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
        shape.setPosition(new Vector2(0, -14 / MarioDemo.PPM ));
        b2body.createFixture(fdef).setUserData(this);

        //create head object
        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioDemo.PPM, 6 / MarioDemo.PPM), new Vector2(2 / MarioDemo.PPM, 6 /MarioDemo.PPM));
        fdef.filter.categoryBits = MarioDemo.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData(this);
        timeToDefineBigMario = false;
    }
    public void defineMario(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(32 / MarioDemo.PPM, 32 / MarioDemo.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioDemo.PPM);
        fdef.filter.categoryBits = MarioDemo.MARIO_BIT;
        fdef.filter.maskBits = MarioDemo.GROUND_BIT
                | MarioDemo.COIN_BIT
                | MarioDemo.BRICK_BIT
                | MarioDemo.OBJECT_BIT
                | MarioDemo.ENEMY_BIT
                | MarioDemo.ENEMY_HEAD_BIT
                | MarioDemo.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        //create head object
        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioDemo.PPM, 6 / MarioDemo.PPM), new Vector2(2 / MarioDemo.PPM, 6 /MarioDemo.PPM));
        fdef.filter.categoryBits = MarioDemo.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData(this);
    }
}
