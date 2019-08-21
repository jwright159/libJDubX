package wrightway.gdx.graphics;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;

public class LayeredTextureRegion extends Array<TextureRegion>{
	private int width, height;
	public LayeredTextureRegion(){
		super();
	}
	public LayeredTextureRegion(TextureRegion region){
		this();
		set(0,region);
	}
	
	public void calculateDimensions(){
		width = height = 0;
		for(TextureRegion region : this){
			if(region == null)
				continue;
			width = region.getRegionWidth() > width ? region.getRegionWidth() : width;
			height = region.getRegionHeight() > height ? region.getRegionHeight() : height;
		}
	}
	public int getRegionWidth(){
		return width;
	}
	public int getRegionHeight(){
		return height;
	}

	@Override
	public void set(int index, TextureRegion value){
		while(size <= index)
			add(null);
		super.set(index, value);
		calculateDimensions();
	}
	@Override
	public TextureRegion removeIndex(int index){
		TextureRegion rtn = super.removeIndex(index);
		calculateDimensions();
		insert(index,null);
		return rtn;
	}
	
	public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
		//Tenebrae.verbose("tiledraw",scaleX, scaleY);
		for(TextureRegion reg : this)
			if(reg != null)
				batch.draw(reg.getTexture(), x, y, originX, originY,
					width, height, scaleX, scaleY, rotation, reg.getRegionX(), reg.getRegionY(),
					reg.getRegionWidth(), reg.getRegionHeight(), false, false);
	}
	public void draw(Batch batch, float x, float y, float width, float height, float scaleX, float scaleY){
		draw(batch, x, y, 0, 0, width, height, scaleX, scaleY, 0);
	}
	public void draw(Batch batch, float x, float y, float width, float height){
		draw(batch, x, y, 0, 0, width, height, 1, 1, 0);
	}
	
	public void dispose(){
		for(TextureRegion reg : this)
			if(reg != null)
				reg.getTexture().dispose();
	}
}
