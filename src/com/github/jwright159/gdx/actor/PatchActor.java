package com.github.jwright159.gdx.actor;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

public class PatchActor extends ScreenActor{
	private int borderAlign = Align.right;
	private NinePatch patch;

	public PatchActor(Rectangle r, NinePatch patch, float borderWidth){
		setRect(r);
		setPatch(patch, borderWidth);
	}
	public PatchActor(Rectangle r, NinePatch patch){
		this(r, patch, 0);
	}

	public void setPatch(NinePatch patch, float borderWidth){
		this.patch = new NinePatch(patch);
		this.patch.setLeftWidth(borderWidth);
		this.patch.setRightWidth(borderWidth);
		this.patch.setTopHeight(borderWidth);
		this.patch.setBottomHeight(borderWidth);
	}
	public NinePatch getPatch(){
		return patch;
	}

	public void setBorderAlignment(int align){
		borderAlign = align;
	}
	public int getBorderAlignment(){
		return borderAlign;
	}

	@Override
	public void draw(Batch batch){
		if(Align.isLeft(borderAlign))
			patch.draw(batch, -patch.getLeftWidth(), -patch.getBottomHeight(), patch.getLeftWidth()+getWidth()+patch.getRightWidth(), patch.getBottomHeight()+getHeight()+patch.getTopHeight());
		else if(Align.isRight(borderAlign))
			patch.draw(batch, 0, 0, getWidth(), getHeight());
		else
			patch.draw(batch, -patch.getLeftWidth()/2, -patch.getBottomHeight()/2, patch.getLeftWidth()/2+getWidth()+patch.getRightWidth()/2, patch.getBottomHeight()/2+getHeight()+patch.getTopHeight()/2);
	}

	@Override
	public void dispose(){
		super.dispose();
		patch.getTexture().dispose();
	}
}
