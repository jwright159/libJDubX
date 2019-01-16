package wrightway.gdx;

import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;

public class NineRegion extends NinePatch{
	private TextureRegion[] regions = new TextureRegion[9];
	public NineRegion(TextureRegion region, int left, int right, int top, int bottom){
		super((TextureRegion)null);

		if(region == null) throw new IllegalArgumentException("region cannot be null.");
		final int middleWidth = region.getRegionWidth() - left - right;
		final int middleHeight = region.getRegionHeight() - top - bottom;

		TextureRegion[] regions = new TextureRegion[9];
		if(top > 0){
			if(left > 0) regions[TOP_LEFT] = new TextureRegion(region, 0, 0, left, top);
			if(middleWidth > 0) regions[TOP_CENTER] = new TextureRegion(region, left, 0, middleWidth, top);
			if(right > 0) regions[TOP_RIGHT] = new TextureRegion(region, left + middleWidth, 0, right, top);
		}
		if(middleHeight > 0){
			if(left > 0) regions[MIDDLE_LEFT] = new TextureRegion(region, 0, top, left, middleHeight);
			if(middleWidth > 0) regions[MIDDLE_CENTER] = new TextureRegion(region, left, top, middleWidth, middleHeight);
			if(right > 0) regions[MIDDLE_RIGHT] = new TextureRegion(region, left + middleWidth, top, right, middleHeight);
		}
		if(bottom > 0){
			if(left > 0) regions[BOTTOM_LEFT] = new TextureRegion(region, 0, top + middleHeight, left, bottom);
			if(middleWidth > 0) regions[BOTTOM_CENTER] = new TextureRegion(region, left, top + middleHeight, middleWidth, bottom);
			if(right > 0) regions[BOTTOM_RIGHT] = new TextureRegion(region, left + middleWidth, top + middleHeight, right, bottom);
		}

		// If split only vertical, move splits from right to center.
		if(left == 0 && middleWidth == 0){
			regions[TOP_CENTER] = regions[TOP_RIGHT];
			regions[MIDDLE_CENTER] = regions[MIDDLE_RIGHT];
			regions[BOTTOM_CENTER] = regions[BOTTOM_RIGHT];
			regions[TOP_RIGHT] = null;
			regions[MIDDLE_RIGHT] = null;
			regions[BOTTOM_RIGHT] = null;
		}
		// If split only horizontal, move splits from bottom to center.
		if(top == 0 && middleHeight == 0){
			regions[MIDDLE_LEFT] = regions[BOTTOM_LEFT];
			regions[MIDDLE_CENTER] = regions[BOTTOM_CENTER];
			regions[MIDDLE_RIGHT] = regions[BOTTOM_RIGHT];
			regions[BOTTOM_LEFT] = null;
			regions[BOTTOM_CENTER] = null;
			regions[BOTTOM_RIGHT] = null;
		}
		setRegions(regions);
	}
	public NineRegion(TextureRegion region){
		super((TextureRegion)null);
		setRegions(new TextureRegion[]{
				null, null, null,
				null, region, null,
				null, null, null
			});
	}
	public NineRegion(NineRegion newRegions){
		super((TextureRegion)null);
		TextureRegion[] regions = new TextureRegion[newRegions.regions.length];
		System.arraycopy(newRegions.regions, 0, regions, 0, regions.length);
		setRegions(regions);
	}
	public void setRegions(TextureRegion[] regions){
		this.regions = regions;
		if(regions[MIDDLE_LEFT] != null) setLeftWidth(regions[MIDDLE_LEFT].getRegionWidth());
		if(regions[MIDDLE_CENTER] != null) setMiddleWidth(regions[MIDDLE_CENTER].getRegionWidth());
		if(regions[MIDDLE_RIGHT] != null) setRightWidth(regions[MIDDLE_RIGHT].getRegionWidth());
		if(regions[TOP_CENTER] != null) setTopHeight(regions[TOP_CENTER].getRegionHeight());
		if(regions[MIDDLE_CENTER] != null) setMiddleHeight(regions[MIDDLE_CENTER].getRegionHeight());
		if(regions[BOTTOM_CENTER] != null) setBottomHeight(regions[BOTTOM_CENTER].getRegionHeight());
	}
	public TextureRegion[] getRegions(){
		return regions;
	}
	@Override
	public Texture getTexture(){
		return regions[MIDDLE_CENTER].getTexture();
	}
	@Override
	public void draw(Batch batch, float x, float y, float width, float height){
		Color oldColor = batch.getColor(), color = getColor();
		if(color != Color.WHITE) batch.setColor(oldColor.mul(color));
		
		//Tenebrae.debug("drawin", this, x, y, getLeftWidth(), getBottomHeight(), getTopHeight(), getMiddleHeight(), height);
		float middleWidth = width - getLeftWidth() - getRightWidth();
		float middleHeight = height - getTopHeight() - getBottomHeight();
		for(int yi = 0; yi < 9; yi += 3){
			for(int xi = 0; xi < 3; xi++){
				TextureRegion r = regions[xi + yi];
				if(r == null)
					continue;

				//ok listen here dum dum 0 = top left not the bottom ok? ok
				float x2, y2;
				if(xi == TOP_LEFT) x2 = x;
				else if(xi == TOP_CENTER) x2 = x + getLeftWidth();
				else /*if(xi == TOP_RIGHT)*/ x2 = x + getLeftWidth() + middleWidth;
				if(yi == BOTTOM_LEFT) y2 = y;
				else if(yi == MIDDLE_LEFT) y2 = y + getBottomHeight();
				else /*if(yi == TOP_LEFT)*/ y2 = y + getBottomHeight() + middleHeight;

				if(xi == TOP_LEFT || xi == TOP_RIGHT){
					if(yi == TOP_LEFT || yi == BOTTOM_LEFT)
						batch.draw(r, x2, y2);
					else{
						int remainingY;
						for(remainingY = (int)middleHeight; remainingY > getMiddleHeight(); remainingY -= getMiddleHeight())
							batch.draw(r, x2, y2 + middleHeight - remainingY);
						batch.draw(r.getTexture(), x2, y2 + middleHeight - remainingY, r.getRegionX(), r.getRegionY() + r.getRegionHeight() - remainingY, r.getRegionWidth(), remainingY);
					}
				}else{
					if(yi == TOP_LEFT || yi == BOTTOM_LEFT){
						int remainingX;
						for(remainingX = (int)middleWidth; remainingX > getMiddleWidth(); remainingX -= getMiddleWidth())
							batch.draw(r, x2 + middleWidth - remainingX, y2);
						batch.draw(r.getTexture(), x2 + middleWidth - remainingX, y2, r.getRegionX(), r.getRegionY(), remainingX, r.getRegionHeight());
					}else{
						int remainingX;
						for(remainingX = (int)middleWidth; remainingX > getMiddleWidth(); remainingX -= getMiddleWidth()){
							int remainingY;
							for(remainingY = (int)middleHeight; remainingY > getMiddleHeight(); remainingY -= getMiddleHeight())
								batch.draw(r, x2 + middleWidth - remainingX, y2 + middleHeight - remainingY);
							batch.draw(r.getTexture(), x2 + middleWidth - remainingX, y2 + middleHeight - remainingY, r.getRegionX(), r.getRegionY() + r.getRegionHeight() - remainingY, r.getRegionWidth(), remainingY);
						}
						int remainingY;
						for(remainingY = (int)middleHeight; remainingY > getMiddleHeight(); remainingY -= getMiddleHeight())
							batch.draw(r.getTexture(), x2 + middleWidth - remainingX, y2 + middleHeight - remainingY, r.getRegionX(), r.getRegionY(), remainingX, r.getRegionHeight());
						batch.draw(r.getTexture(), x2 + middleWidth - remainingX, y2 + middleHeight - remainingY, r.getRegionX(), r.getRegionY() + r.getRegionHeight() - remainingY, remainingX, remainingY);
					}
				}
			}
		}
		
		if(color != Color.WHITE) batch.setColor(oldColor);
	}

	public static NinePatchDrawable tint(NinePatchDrawable drawable, Color tint){
		drawable = new NinePatchDrawable(drawable);
		NineRegion regions = new NineRegion((NineRegion)drawable.getPatch());
		regions.setColor(tint);
		drawable.setPatch(regions);
		return drawable;
	}
}
