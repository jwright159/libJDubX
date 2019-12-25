package com.github.jwright159.gdx.actor;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

public class SizableProgressBar extends ProgressBar{
	private boolean round;
	private float position;
	
	public SizableProgressBar(float min, float max, float step, boolean vertical, ProgressBarStyle style){
		super(min, max, step, vertical, style);
	}
	
	@Override
	public float getPrefWidth(){
		return 0;
	}
	@Override
	public float getPrefHeight(){
		return 0;
	}
	
	@Override
	public void setRound(boolean round){
		super.setRound(round);
		this.round = round;
	}
	
	@Override
	protected float getKnobPosition(){
		return position;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha){
		ProgressBarStyle style = getStyle();
		boolean disabled = isDisabled();
		final Drawable knob = getKnobDrawable();
		final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
		final Drawable knobBefore = (disabled && style.disabledKnobBefore != null) ? style.disabledKnobBefore : style.knobBefore;
		final Drawable knobAfter = (disabled && style.disabledKnobAfter != null) ? style.disabledKnobAfter : style.knobAfter;

		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();
		float percent = getVisualPercent();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		if(isVertical()){
			float positionHeight = height;
			float knobHeight = knob == null ? 0 : knob.getMinHeight();

			float bgTopHeight = 0, bgBottomHeight = 0;
			if(bg != null){
				bg.draw(batch, x, y, width, height);
				bgTopHeight = bg.getTopHeight();
				bgBottomHeight = bg.getBottomHeight();
				positionHeight -= bgTopHeight + bgBottomHeight;
			}

			float knobHeightHalf = 0;
			if(knob == null){
				knobHeightHalf = knobBefore == null ? 0 : knobBefore.getMinHeight() * 0.5f;
				position = (positionHeight - knobHeightHalf) * percent;
				position = Math.min(positionHeight - knobHeightHalf, position);
			}else{
				knobHeightHalf = knobHeight * 0.5f;
				position = (positionHeight - knobHeight) * percent;
				position = Math.min(positionHeight - knobHeight, position) + bgBottomHeight;
			}
			position = Math.max(Math.min(0, bgBottomHeight), position);

			if(knobBefore != null){
				knobBefore.draw(batch, x, y + bgTopHeight, width, position + knobHeightHalf);
			}
			if(knobAfter != null){
				knobAfter.draw(batch, x, y + position + knobHeightHalf, width, height - position - knobHeightHalf);
			}
			if(knob != null){
				knob.draw(batch, x, y + position, width, knobHeight);
			}
		}else{
			float positionWidth = width;
			float knobWidth = knob == null ? 0 : knob.getMinWidth();

			float bgLeftWidth = 0, bgRightWidth = 0;
			if(bg != null){
				bg.draw(batch, x, y, width, height);
				bgLeftWidth = bg.getLeftWidth();
				bgRightWidth = bg.getRightWidth();
				positionWidth -= bgLeftWidth + bgRightWidth;
			}

			float knobWidthHalf = 0;
			if(knob == null){
				knobWidthHalf = knobBefore == null ? 0 : knobBefore.getMinWidth() * 0.5f;
				position = (positionWidth - knobWidthHalf) * percent;
				position = Math.min(positionWidth - knobWidthHalf, position);
			}else{
				knobWidthHalf = knobWidth * 0.5f;
				position = (positionWidth - knobWidth) * percent;
				position = Math.min(positionWidth - knobWidth, position) + bgLeftWidth;
			}
			position = Math.max(Math.min(0, bgLeftWidth), position);

			if(knobBefore != null){
				knobBefore.draw(batch, x + bgLeftWidth, y, position + knobWidthHalf, height);
			}
			if(knobAfter != null){
				knobAfter.draw(batch, x + position + knobWidthHalf, y, width - position - knobWidthHalf, height);
			}
			if(knob != null){
				knob.draw(batch, x + position, y, knobWidth, height);
			}
		}
	}
}
