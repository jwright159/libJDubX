package wrightway.gdx;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

public class WScreen implements Screen{
	protected Stage worldStage, uiStage;
	public static float b = 0.0f, bb = b;
	private boolean isShowing, isRunning;
	protected InputMultiplexer multiplexer;

	public WScreen(){
		worldStage = new Stage(new ScreenViewport());
		uiStage = new Stage(new ScreenViewport());
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(uiStage);
		multiplexer.addProcessor(worldStage);
	}

	@Override
	public void render(float p1){
		if(isShowing){
			Gdx.gl.glClearColor(b, b, b, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		b = bb;

		if(isRunning){
			act();
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
	
	public void act(){}
	public void draw(){}
	
	@Override
	public void dispose(){
		if(worldStage != null){
			//Tenebrae.debug("Actors? "+stage.getActors().size);
			for(Actor actor : worldStage.getActors()){
				//Tenebrae.debug("Got an actor! "+actor);
				if(actor instanceof WActor){
					//Tenebrae.debug("It's a WActor! :D");
					((WActor)actor).dispose();
				}
			}
			worldStage.dispose();
		}
		if(uiStage != null){
			for(Actor actor : uiStage.getActors()){
				//Tenebrae.debug("Got a hud actor! "+actor);
				if(actor instanceof WActor){
					//Tenebrae.debug("It's a WActor! :D");
					((WActor)actor).dispose();
				}
			}
			uiStage.dispose();
		}
		//Tenebrae.debug("Disposing of screen! "+this);
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
