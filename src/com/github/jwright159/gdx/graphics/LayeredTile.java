package com.github.jwright159.gdx.graphics;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.maps.tiled.*;

public class LayeredTile extends Array<TiledMapTile>{
	public LayeredTile(){
		super();
	}
	public LayeredTile(TiledMapTile tile){
		this();
		set(0, tile);
	}

	@Override
	public void set(int index, TiledMapTile value){
		while(size <= index)
			add(null);
		super.set(index, value);
	}
	@Override
	public TiledMapTile removeIndex(int index){
		TiledMapTile rtn = super.removeIndex(index);
		insert(index,null);
		return rtn;
	}

	public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
		//Tenebrae.verbose("tiledraw",scaleX, scaleY);
		for(TiledMapTile obj : this)
			if(obj != null)
				batch.draw(obj.getTextureRegion().getTexture(), x, y, originX, originY,
					width, height, scaleX, scaleY, rotation, obj.getTextureRegion().getRegionX(), obj.getTextureRegion().getRegionY(),
					obj.getTextureRegion().getRegionWidth(), obj.getTextureRegion().getRegionHeight(), false, false);
	}
	public void draw(Batch batch, float x, float y, float width, float height, float scaleX, float scaleY){
		draw(batch, x, y, 0, 0, width, height, scaleX, scaleY, 0);
	}
	public void draw(Batch batch, float x, float y, float width, float height){
		draw(batch, x, y, 0, 0, width, height, 1, 1, 0);
	}
}
