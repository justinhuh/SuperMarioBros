package com.justinhuh.mario.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.justinhuh.mario.MarioDemo;
import com.justinhuh.mario.Scenes.Hud;
import com.justinhuh.mario.Sprites.Enemy;
import com.justinhuh.mario.Sprites.Goomba;
import com.justinhuh.mario.Sprites.Item;
import com.justinhuh.mario.Sprites.ItemDef;
import com.justinhuh.mario.Sprites.Mario;
import com.justinhuh.mario.Sprites.Mushroom;
import com.justinhuh.mario.Tools.B2WorldCreator;
import com.justinhuh.mario.Tools.WorldContactListener;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
//import static java.lang.System.out;
/**
 * Created by Justin on 9/4/2016.
 */
public class PlayScreen implements Screen{
    private MarioDemo game;
    private TextureAtlas atlas;

    private OrthographicCamera gamecam; //game camera
    private Viewport gamePort; //game viewport
    private Hud hud;

    //box2d
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    //sprites
    private Mario player;

    private Music music;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    //tiled map
    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    public PlayScreen (MarioDemo game){
        atlas = new TextureAtlas("Mario_Characters.pack");
        this.game = game;
        gamecam = new OrthographicCamera(); //camera to follow mario
        gamePort = new FitViewport(MarioDemo.V_WIDTH / MarioDemo.PPM, MarioDemo.V_HEIGHT / MarioDemo.PPM, gamecam); //fitViewport is used for most flexible adaptation to resolutions

        //HUD for score/level information
        hud = new Hud(game.batch);

        //load map and setup renderer
        maploader = new TmxMapLoader();
        map = maploader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioDemo.PPM);

        //set our game camera
        gamecam.position.set(gamePort.getWorldWidth()/2, gamePort.getWorldHeight()/2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        //create mario
        creator = new B2WorldCreator(this);

        player = new Mario(this);
        world.setContactListener(new WorldContactListener());

        music = MarioDemo.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();
    }

    public void spawnItem(ItemDef idef){
        itemsToSpawn.add(idef);
    }

    public void handleSpawningItems() {
        if(!itemsToSpawn.isEmpty()){
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class){
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }


    public TextureAtlas getAtlas(){
        return atlas;
    }
    @Override
    public void show() {

    }

    public void handleInput(float dt){
        //moves character based on button press if Mario is alive
        if(player.currentState != Mario.State.DEAD){
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && player.currentState != Mario.State.JUMPING)
               player.b2body.applyLinearImpulse(new Vector2(0,4f), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2)
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2)
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
        }
    }

    public void update(float dt){
        //user input handler
        handleInput(dt);
        handleSpawningItems();

        //updates 60 times a second
        world.step(1/60f, 6, 2);

        player.update(dt);
        for(Enemy enemy: creator.getEnemies()) {
            enemy.update(dt);
            if (enemy.getX() < player.getX() + 224/MarioDemo.PPM)
                enemy.b2body.setActive(true); //goomba wakes from sleep
        }

        for(Item item : items)
            item.update(dt);

        hud.update(dt);

        //move our gamecam around to follow Mario if alive
        if (player.currentState != Mario.State.DEAD) {
            gamecam.position.x = player.b2body.getPosition().x;
        }
        gamecam.update();
        //render only what game camera sees
        renderer.setView(gamecam);
    }

    @Override
    public void render(float delta) {
        update(delta);
        //draw black background
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();
        b2dr.render(world, gamecam.combined);

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for(Enemy enemy: creator.getEnemies())
            enemy.draw(game.batch);
        for (Item item : items)
            item.draw(game.batch);
        game.batch.end();

        //draw HUD camera view
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()){

            if (player.currentState == Mario.State.WIN) {
                game.setScreen(new WinOverScreen(game, hud.getScore()));
                dispose();
            }
            else if (player.currentState == Mario.State.DEAD) {
                game.setScreen(new GameOverScreen(game));
                dispose();
            }
        }
    }

    public boolean gameOver(){
        if((player.currentState == Mario.State.DEAD || player.currentState == Mario.State.WIN) && player.getStateTimer() > 3)
            return true;
        if (hud.getTime() <= 0) {
            player.currentState = Mario.State.DEAD;
            return true;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width,height);
    }

    public TiledMap getMap(){
        return map;
    }

    public World getWorld(){
        return world;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }
}
