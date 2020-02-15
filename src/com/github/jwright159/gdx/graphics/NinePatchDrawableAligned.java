package com.github.jwright159.gdx.graphics;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;

public class NinePatchDrawableAligned extends NinePatchDrawable{
	private int borderAlign = Align.right;

	public NinePatchDrawableAligned(){
		super();
	}
	public NinePatchDrawableAligned(NinePatch patch){
		super(patch);
	}
	public NinePatchDrawableAligned(NinePatchDrawable drawable){
		super(drawable);
	}
	public NinePatchDrawableAligned(NinePatchDrawableAligned drawable){
		this((NinePatchDrawable)drawable);
		borderAlign = drawable.borderAlign;
	}

	public void setBorderAlignment(int align){
		borderAlign = align;
	}
	public int getBorderAlignment(){
		return borderAlign;
	}

	public void draw(Batch batch, float x, float y, float width, float height){
		NinePatch patch = getPatch();
		if(Align.isLeft(borderAlign))
			patch.draw(batch, x - patch.getLeftWidth(), y - patch.getBottomHeight(), patch.getLeftWidth() + width + patch.getRightWidth(), patch.getBottomHeight() + height + patch.getTopHeight());
		else if(Align.isRight(borderAlign))
			patch.draw(batch, x, y, width, height);
		else
			patch.draw(batch, x - patch.getLeftWidth() / 2, y - patch.getBottomHeight() / 2, patch.getLeftWidth() / 2 + width + patch.getRightWidth() / 2, patch.getBottomHeight() / 2 + height + patch.getTopHeight() / 2);
	}
	public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
		NinePatch patch = getPatch();
		if(Align.isLeft(borderAlign))
			patch.draw(batch, x - patch.getLeftWidth(), y - patch.getBottomHeight(), originX, originY, patch.getLeftWidth() + width + patch.getRightWidth(), patch.getBottomHeight() + height + patch.getTopHeight(), scaleX, scaleY, rotation);
		else if(Align.isRight(borderAlign))
			patch.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
		else
			patch.draw(batch, x - patch.getLeftWidth() / 2, y - patch.getBottomHeight() / 2, originX, originY, patch.getLeftWidth() / 2 + width + patch.getRightWidth() / 2, patch.getBottomHeight() / 2 + height + patch.getTopHeight() / 2, scaleX, scaleY, rotation);
	}

	/** Creates a new drawable that renders the same as this drawable tinted the specified color. */
	public NinePatchDrawableAligned tint(Color tint){
		NinePatchDrawableAligned drawable = new NinePatchDrawableAligned(this);
		drawable.setPatch(new NinePatch(drawable.getPatch(), tint));
		return drawable;
	}
}
