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

public class GuiScreen implements Screen{
	private Stage stage;
	private boolean isShowing, isRunning;
	private InputMultiplexer multiplexer;
	private Skin skin;
	private TextureAtlas skinta;
	private Table table;
	
	private FocusTable focusedTable;

	public GuiScreen(Stage otherStage){
		stage = otherStage == null ? new Stage(new ScreenViewport()) : otherStage;
		
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
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
		stage.addActor(table = new Table(skin));
		table.setFillParent(true);
	}
	public GuiScreen(Viewport view){
		this(new Stage(view == null ? new ScreenViewport() : view));
	}
	public GuiScreen(){
		this((Stage)null);
	}

	@Override
	public void render(float delta){
		if(isShowing){
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}

		if(isRunning && delta != 0){
			act(delta);
			stage.act();
		}
		if(isShowing){
			com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile.updateAnimationBaseTime();
			draw();
			stage.draw();
		}
	}
	
	public void act(float delta){}
	public void draw(){}
	
	public Stage getStage(){
		return stage;
	}
	public OrthographicCamera getCamera(){
		return (OrthographicCamera)stage.getCamera();
	}
	public void setViewport(Viewport viewport){
		stage.setViewport(viewport);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	public Viewport getViewport(){
		return stage.getViewport();
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
	
	@Override
	public void dispose(){
		for(Actor actor : stage.getActors())
			if(actor instanceof ScreenActor)
				((ScreenActor)actor).dispose();
		stage.dispose();
		skin.dispose();
		skinta.dispose();
	}

	@Override
	public void resize(int x, int y){
		Log.ui("Resizing! "+x+"x"+y);
		stage.getViewport().update(x,y);
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
