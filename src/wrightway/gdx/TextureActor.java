package wrightway.gdx;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

public class TextureActor extends ScreenActor{
	private LayeredTextureRegion region;
	public TextureActor(){
		region = new LayeredTextureRegion();
	}
	public TextureActor(TextureRegion region){
		this();
		setRegion(0, region);
	}
	public TextureActor(LayeredTextureRegion region){
		setRegions(region);
	}
	public TextureActor(TextureActor sprite){
		super(sprite);
		setRegions(sprite.region);
	}
	
	public void setRegion(int i, TextureRegion region){
		float width = this.region.getRegionWidth(), height = this.region.getRegionHeight();
		this.region.set(i, region);
		setSize(width == 0 ? region.getRegionWidth() : getWidth() * region.getRegionWidth() / width, height == 0 ? region.getRegionHeight() : getHeight() * region.getRegionHeight() / height);
	}
	public void setRegion(TextureRegion region){
		setRegion(0, region);
	}

	public TextureRegion getRegion(int i){
		return region.get(i);
	}
	public TextureRegion getRegion(){
		return region.get(0);
	}

	public TextureRegion removeRegion(int i){
		float width = region.getRegionWidth(), height = region.getRegionHeight();
		TextureRegion rtn = region.removeIndex(i);
		setSize(width == 0 ? region.getRegionWidth() : getWidth() * region.getRegionWidth() / width, height == 0 ? region.getRegionHeight() : getHeight() * region.getRegionHeight() / height);
		return rtn;
	}
	public TextureRegion removeRegion(){
		return removeRegion(0);
	}

	public void setRegions(LayeredTextureRegion region){
		this.region = region;
		setSize(region.getRegionWidth(), region.getRegionHeight());
	}
	public LayeredTextureRegion getRegions(){
		return region;
	}

	private static Color tmp = new Color();
	@Override
	public void draw(Batch batch, float parentAlpha){
		tmp.set(batch.getColor());
		batch.setColor(batch.getColor().mul(getColor()));
		region.draw(batch, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
		batch.getColor().set(tmp);
	}

	@Override
	public void dispose(){
		super.dispose();
		//region.dispose();
	}
}
