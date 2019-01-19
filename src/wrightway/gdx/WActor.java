package wrightway.gdx;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;

public abstract class WActor extends Actor implements Disposable{
	public WActor(){
		addListener(new WActorGestureListener());
		rectBuffer = new Rectangle();
	}

	public void setRect(Rectangle rect){
		setBounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
	private Rectangle rectBuffer;
	public Rectangle toRect(){
		rectBuffer.set(getX(), getY(), getTrueWidth(), getTrueHeight());
		return rectBuffer;
	}
	
	public float getTrueWidth(){
		return getWidth() * getScaleX();
	}
	public float getTrueHeight(){
		return getHeight() * getScaleY();
	}

	@Override
	public abstract void draw(Batch batch, float parentAlpha)

	
	@Override
	public void dispose(){
		//Tenebrae.debug("Disposing of WActor! "+this);
		remove();
	}

	public void touchDown(float x, float y, int pointer, int button){}
	public void touchUp(float x, float y, int pointer, int button){}
	
	public void pan(float x, float y, float deltaX, float deltaY){}
	public void tap(float x, float y, int count, int button){}

	public class WActorGestureListener extends ActorGestureListener{
		@Override
		public void touchDown(InputEvent event, float x, float y, int pointer, int button){
			WActor.this.touchDown(x, y, pointer, button);
		}
		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button){
			WActor.this.touchUp(x, y, pointer, button);
		}
		@Override
		public void pan(InputEvent event, float x, float y, float deltaX, float deltaY){
			WActor.this.pan(x, y, deltaX, deltaY);
		}
		@Override
		public void tap(InputEvent event, float x, float y, int count, int button){
			WActor.this.tap(x, y, count, button);
		}
	}

	public static class WTexture extends WActor{
		private LayeredTextureRegion region;
		public WTexture(){
			super();
			region = new LayeredTextureRegion();
		}
		public WTexture(TextureRegion region){
			this();
			setRegion(0, region);
		}
		public WTexture(LayeredTextureRegion region){
			super();
			this.region = region;
			setBounds(getX(), getY(), this.region.getRegionWidth(), this.region.getRegionHeight());
		}
		public WTexture(WTexture sprite){
			this(sprite.region);
			setBounds(getX(), getY(), sprite.getWidth(), sprite.getHeight());
			setScale(sprite.getScaleX(), sprite.getScaleY());
		}

		public void setRegion(int i, TextureRegion region){
			this.region.set(i, region);
			setBounds(getX(), getY(), this.region.getRegionWidth(), this.region.getRegionHeight());
		}
		public TextureRegion getRegion(int i){
			return region.get(i);
		}
		public TextureRegion removeRegion(int i){
			TextureRegion rtn = region.removeIndex(i);
			setBounds(getX(), getY(), region.getRegionWidth(), region.getRegionHeight());
			return rtn;
		}
		public LayeredTextureRegion getRegions(){
			return region;
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha){
			region.draw(batch, getX(), getY(), getWidth(), getHeight(), getScaleX(), getScaleY());
		}

		@Override
		public void dispose(){
			super.dispose();
			region.dispose();
		}
	}

	public static class WRect extends WActor{
		private TextureRegion fill, border;
		private int borderAlign;
		private float borderWidth;
		
		public WRect(Rectangle r, Color fillColor, Color borderColor, float borderWidth, int borderAlign){
			setRect(r);
			setFill(fillColor);
			setBorder(borderColor);
			this.borderWidth = borderWidth;
			this.borderAlign = borderAlign;
		}
		public WRect(Rectangle r, Color fillColor, Color borderColor, float borderWidth){
			this(r, fillColor, borderColor, borderWidth, Align.center);
		}
		public WRect(Rectangle r, Color fillColor){
			this(r, fillColor, Color.CLEAR, 0);
		}
		
		public void setFill(Color color){
			if(fill != null)
				fill.getTexture().dispose();
			Pixmap c = new Pixmap(1,1,Pixmap.Format.RGBA8888);
			c.setColor(color); c.fill();
			fill = new TextureRegion(new Texture(c));
		}
		public void setBorder(Color color){
			if(border != null)
				border.getTexture().dispose();
			Pixmap c = new Pixmap(1,1,Pixmap.Format.RGBA8888);
			c.setColor(color); c.fill();
			border = new TextureRegion(new Texture(c));
		}

		@Override
		public void draw(Batch batch, float parentAlpha){
			if(Align.isLeft(borderAlign)){
				batch.draw(border, getX()-borderWidth, getY()-borderWidth, getWidth()+borderWidth*2, getHeight()+borderWidth*2);
				batch.draw(fill, getX(), getY(), getWidth(), getHeight());
			}else if(Align.isCenterVertical(borderAlign)){
				batch.draw(border, getX()-borderWidth/2, getY()-borderWidth/2, getWidth()+borderWidth, getHeight()+borderWidth);
				batch.draw(fill, getX()+borderWidth/2, getY()+borderWidth/2, getWidth()-borderWidth, getHeight()-borderWidth);
			}else if(Align.isRight(borderAlign)){
				batch.draw(border, getX(), getY(), getWidth(), getHeight());
				batch.draw(fill, getX()+borderWidth, getY()+borderWidth, getWidth()-borderWidth*2, getHeight()-borderWidth*2);
			}
		}

		@Override
		public void dispose(){
			super.dispose();
			fill.getTexture().dispose();
			border.getTexture().dispose();
		}
	}
}
