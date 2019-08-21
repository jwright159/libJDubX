package wrightway.gdx.actor;

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
	public void setColor(Color color){
		super.setColor(color);
		patch.setColor(color);
	}
	@Override
	public void setColor(float r, float g, float b, float a){
		super.setColor(r, g, b, a);
		patch.setColor(new Color(r, g, b, a));
	}

	@Override
	public void draw(Batch batch, float parentAlpha){
		if(Align.isLeft(borderAlign))
			patch.draw(batch, getX()-patch.getLeftWidth(), getY()-patch.getBottomHeight(), patch.getLeftWidth()+getWidth()*getScaleX()+patch.getRightWidth(), patch.getBottomHeight()+getHeight()*getScaleY()+patch.getTopHeight());
		else if(Align.isRight(borderAlign))
			patch.draw(batch, getX(), getY(), getWidth()*getScaleX(), getHeight()*getScaleY());
		else
			patch.draw(batch, getX()-patch.getLeftWidth()/2, getY()-patch.getBottomHeight()/2, patch.getLeftWidth()/2+getWidth()*getScaleX()+patch.getRightWidth()/2, patch.getBottomHeight()/2+getHeight()*getScaleY()+patch.getTopHeight()/2);
	}

	@Override
	public void dispose(){
		super.dispose();
		patch.getTexture().dispose();
	}
}
