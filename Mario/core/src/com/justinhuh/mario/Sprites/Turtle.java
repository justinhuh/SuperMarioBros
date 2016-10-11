package com.justinhuh.mario.Sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.justinhuh.mario.MarioDemo;
import com.justinhuh.mario.Screens.PlayScreen;

/**
 * Created by Justin on 10/8/2016.
 */
public class Turtle extends Enemy {
    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;
    public enum State {WALKING, STANDING_SHELL, MOVING_SHELL, DEAD};
    public State currentState;
    public State previousState;
    private float stateTime;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;
    private TextureRegion shell;
    private boolean setToDestroy;
    private boolean destroyed;
    private float deadRotationDegrees;


    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 16, 0, 16, 24));
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 64, 0, 16, 24);
        walkAnimation = new Animation(0.2f, frames);
        currentState = previousState = State.WALKING;
        deadRotationDegrees = 0;

        setBounds(getX(), getY(), 16 / MarioDemo.PPM, 24 / MarioDemo.PPM);
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioDemo.PPM);
        fdef.filter.categoryBits = MarioDemo.ENEMY_BIT;
        fdef.filter.maskBits = MarioDemo.GROUND_BIT
                | MarioDemo.COIN_BIT
                | MarioDemo.BRICK_BIT
                | MarioDemo.ENEMY_BIT
                | MarioDemo.OBJECT_BIT
                | MarioDemo.MARIO_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        //Turtle Head Fixture
        PolygonShape head = new PolygonShape();
        Vector2[] vertice = new Vector2[4];
        vertice[0] = new Vector2(-5, 8).scl(1 / MarioDemo.PPM);
        vertice[1] = new Vector2(5, 8).scl(1 / MarioDemo.PPM);
        vertice[2] = new Vector2(-3, 3).scl(1 / MarioDemo.PPM);
        vertice[3] = new Vector2(3, 3).scl(1 / MarioDemo.PPM);
        head.set(vertice);

        fdef.shape = head;
        fdef.restitution = .5f; //bounce factor, could be 1.5
        fdef.filter.categoryBits = MarioDemo.ENEMY_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void onEnemyHit(Enemy enemy)
    {
        if(enemy instanceof Turtle){
            if(((Turtle) enemy).currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL){
                killed();
            }
            else if(currentState == State.MOVING_SHELL && ((Turtle) enemy ).currentState == State.WALKING)
                return;
            else
                reverseVelocity(true, false);
        }
        else if(currentState != State.MOVING_SHELL)
                reverseVelocity(true, false);
    }

    public TextureRegion getFrame(float dt){
        TextureRegion region;
        switch (currentState){
            case STANDING_SHELL:
            case MOVING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if(velocity.x > 0 && region.isFlipX() == false){
            region.flip(true, false);
        }
        if(velocity.x < 0 && region.isFlipX()){
            region.flip(true, false);
        }
        stateTime = currentState == previousState ? stateTime + dt : 0;
        previousState = currentState;
        return region;
    }
    @Override
    public void update(float dt) {
        setRegion(getFrame(dt));
        if(currentState == State.STANDING_SHELL && stateTime > 5){
            currentState = State.WALKING;
            velocity.x = 1;
        }

        setPosition(b2body.getPosition().x - getWidth()/2, b2body.getPosition().y - 8 / MarioDemo.PPM);

        if (currentState == State.DEAD){
            deadRotationDegrees += 3;
            rotate(deadRotationDegrees);
            if(stateTime > 5 && !destroyed){
                world.destroyBody(b2body);
                destroyed = true;
            }
        }
        else
            b2body.setLinearVelocity(velocity);
    }

    @Override
    public void hitOnHead(Mario mario) {
        if(currentState != State.STANDING_SHELL){
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        }
        else
        {
            kick(mario.getX() <= this.getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED);
        }
    }

    public void kick(int speed){
        velocity.x  = speed;
        currentState = State.MOVING_SHELL;
    }

    public State getCurrentState(){
        return currentState;
    }

    public void killed(){
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioDemo.NOTHING_BIT;

        for (Fixture fixture : b2body.getFixtureList())
            fixture.setFilterData(filter);
        b2body.applyLinearImpulse(new Vector2(0, 5f), b2body.getWorldCenter(), true );
    }

    public void draw(Batch batch){
        if(!destroyed)
            super.draw(batch);
    }
}
