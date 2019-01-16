package wrightway.gdx;

import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.*;

public class LayeredTextureRegionDrawable extends BaseDrawable implements TransformDrawable{
	private LayeredTextureRegion regions;

	public LayeredTextureRegionDrawable(LayeredTextureRegion regions){
		setRegions(regions);
	}
	public LayeredTextureRegionDrawable(LayeredTextureRegionDrawable drawable){
		super(drawable);
		setRegions(drawable.regions);
	}

	public void draw(Batch batch, float x, float y, float width, float height){
		regions.draw(batch, x, y, width, height);
	}

	public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
		regions.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
	}

	public void setRegions(LayeredTextureRegion regions){
		this.regions = regions;
		if(regions != null){
			setMinWidth(regions.getRegionWidth());
			setMinHeight(regions.getRegionHeight());
		}
	}

	public LayeredTextureRegion getRegions(){
		return regions;
	}

	/** Creates a new drawable that renders the same as this drawable tinted the specified color. */
	/*public Drawable tint(Color tint){
		Sprite sprite;
		if(region instanceof AtlasRegion)
			sprite = new AtlasSprite((AtlasRegion)region);
		else
			sprite = new Sprite(region);
		sprite.setColor(tint);
		sprite.setSize(getMinWidth(), getMinHeight());
		SpriteDrawable drawable = new SpriteDrawable(sprite);
		drawable.setLeftWidth(getLeftWidth());
		drawable.setRightWidth(getRightWidth());
		drawable.setTopHeight(getTopHeight());
		drawable.setBottomHeight(getBottomHeight());
		return drawable;
	}*/
}
