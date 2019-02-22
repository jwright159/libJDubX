package wrightway.gdx;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class WScreen implements Screen{
	private Stage worldStage, uiStage;
	private boolean isShowing, isRunning;
	private InputMultiplexer multiplexer;
	private Skin skin;
	private TextureAtlas skinta;
	private Table table;

	public WScreen(Stage otherUiStage){
		worldStage = new Stage(new ScreenViewport());
		if(uiStage != null)
			uiStage = otherUiStage;
		else
			uiStage = new Stage(new ScreenViewport());
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(uiStage);
		multiplexer.addProcessor(worldStage);
		skin = new WSkin(Gdx.files.internal("uiskin.json"), skinta = new NineRegionTextureAtlas(Gdx.files.internal("uiskin.atlas")));
		uiStage.addActor(table = new Table(skin));
		table.setFillParent(true);
	}
	public WScreen(){
		this(null);
	}

	@Override
	public void render(float delta){
		if(isShowing){
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}

		if(isRunning){
			act(delta);
			worldStage.act();
			uiStage.act();
		}
		if(isShowing){
			com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile.updateAnimationBaseTime();
			draw();
			worldStage.draw();
			uiStage.draw();
		}
	}
	
	public void act(float delta){}
	public void draw(){}
	
	public Stage getStage(){
		return worldStage;
	}
	public Stage getUiStage(){
		return uiStage;
	}
	public OrthographicCamera getCamera(){
		return (OrthographicCamera)worldStage.getCamera();
	}
	public InputMultiplexer getMultiplexer(){
		return multiplexer;
	}
	public Skin getSkin(){
		return skin;
	}
	public Table getTable(){
		return table;
	}
	
	public void dispose(boolean disposeUi){
		for(Actor actor : worldStage.getActors())
			if(actor instanceof WActor)
				((WActor)actor).dispose();
		worldStage.dispose();
		if(disposeUi){
			for(Actor actor : uiStage.getActors())
				if(actor instanceof WActor)
					((WActor)actor).dispose();
			uiStage.dispose();
		}
		skin.dispose();
		skinta.dispose();
	}
	@Override
	public void dispose(){
		dispose(true);
	}

	@Override
	public void resize(int x, int y){
		Log.verbose("Resizing! "+x+"x"+y);
		worldStage.getViewport().update(x,y);
		uiStage.getViewport().update(x,y);
	}

	@Override
	public void show(){
		isShowing = true;
		isRunning = true;
		setProcessor();
	}

	@Override
	public void hide(){
		isShowing = false;
		isRunning = false;
		setProcessor();
	}

	@Override
	public void pause(){
		isRunning = false;
		setProcessor();
	}

	@Override
	public void resume(){
		isRunning = isShowing;
		setProcessor();
	}
	
	private void setProcessor(){
		if(isRunning && isShowing)
			Gdx.input.setInputProcessor(multiplexer);
		else if(Gdx.input.getInputProcessor() == multiplexer)
			Gdx.input.setInputProcessor(null);
	}
}
