package com.github.jwright159.gdx.screen;

import com.github.jwright159.gdx.*;
import com.github.jwright159.gdx.actor.ScreenActor;
import com.github.jwright159.gdx.graphics.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import java.util.*;

public class GameScreen implements Screen{
	private Stage worldStage, uiStage;
	private boolean isShowing, isRunning;
	private InputMultiplexer multiplexer;
	private Skin skin;
	private TextureAtlas skinta;
	private Table table;
	
	private FocusTable focusedTable;

	public GameScreen(Stage otherWorldStage, Stage otherUiStage){
		worldStage = otherWorldStage == null ? new Stage(new ScreenViewport()) : otherWorldStage;
		uiStage = otherUiStage == null ? new Stage(new ScreenViewport()) : otherUiStage;
		
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(uiStage);
		multiplexer.addProcessor(worldStage);
		multiplexer.addProcessor(new InputAdapter(){
				public boolean keyDown(int keycode){
					switch(keycode){
						case Input.Keys.UP:
						case Input.Keys.DOWN:
						case Input.Keys.LEFT:
						case Input.Keys.RIGHT:
						case Input.Keys.ENTER:
							if(focusedTable != null){
								focusedTable.handleFocus(keycode);
								return true;
							}else
								return false;
						
						default:
							return false;
					}
				}
			});
		
		skin = new FreeSkin(Gdx.files.internal("uiskin.json"), skinta = new NineRegionTextureAtlas(Gdx.files.internal("uiskin.atlas")));
		uiStage.addActor(table = new Table(skin));
		table.setFillParent(true);
	}
	public GameScreen(Viewport worldView, Viewport uiView){
		this(new Stage(worldView == null ? new ScreenViewport() : worldView), new Stage(uiView == null ? new ScreenViewport() : uiView));
	}
	
	public GameScreen(Stage otherWorldStage, Viewport uiView){
		this(otherWorldStage, new Stage(uiView == null ? new ScreenViewport() : uiView));
	}
	public GameScreen(Viewport worldView, Stage otherUiStage){
		this(new Stage(worldView == null ? new ScreenViewport() : worldView), otherUiStage);
	}
	
	public GameScreen(Stage otherUiStage){
		this((Stage)null, otherUiStage);
	}
	public GameScreen(){
		this((Stage)null, (Stage)null);
	}

	@Override
	public void render(float delta){
		if(isShowing){
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}

		if(isRunning && delta != 0){
			act(delta);
			worldStage.act();
			uiStage.act();
		}
		if(isShowing){
			com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile.updateAnimationBaseTime();
			worldStage.getViewport().apply();
			draw();
			worldStage.draw();
			uiStage.getViewport().apply();
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
	public void setViewport(Viewport viewport){
		worldStage.setViewport(viewport);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	public Viewport getViewport(){
		return worldStage.getViewport();
	}
	public void setUiViewport(Viewport viewport){
		uiStage.setViewport(viewport);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	public Viewport getUiViewport(){
		return uiStage.getViewport();
	}
	
	public InputMultiplexer getMultiplexer(){
		return multiplexer;
	}
	
	public void setSkin(Skin skin){
		this.skin = skin;
	}
	public Skin getSkin(){
		return skin;
	}
	public Table getTable(){
		return table;
	}
	
	public float getWidth(){
		return getCamera().viewportWidth;
	}
	public float getHeight(){
		return getCamera().viewportHeight;
	}
	
	public void setFocusTable(FocusTable table){
		boolean wasFocused = focusedTable != null && focusedTable.isFocussed();
		if(wasFocused)
			focusedTable.unfocus();
		focusedTable = table;
		if(wasFocused && focusedTable != null)
			focusedTable.focus();
	}
	public FocusTable getFocusTable(){
		return focusedTable;
	}
	
	public void dispose(boolean disposeUi){
		for(Actor actor : worldStage.getActors())
			if(actor instanceof ScreenActor)
				((ScreenActor)actor).dispose();
		worldStage.dispose();
		if(disposeUi){
			for(Actor actor : uiStage.getActors())
				if(actor instanceof ScreenActor)
					((ScreenActor)actor).dispose();
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
		Log.ui("Resizing! "+x+"x"+y);
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
