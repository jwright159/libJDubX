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
	}

	public static abstract class WShape extends WActor{
		protected ShapeRenderer renderer;
		public WShape(){
			renderer = new ShapeRenderer();
		}
		
		@Override
		public void dispose(){
			super.dispose();
			renderer.dispose();
		}

		@Override
		public void draw(Batch batch, float parentAlpha){
			batch.end();
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			renderer.begin(ShapeRenderer.ShapeType.Filled);
			drawShapes();
			renderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
			batch.begin();
		}

		public abstract void drawShapes();
	}

	public static class WRect extends WShape{
		private Color fillColor,borderColor;
		private float borderWidth;
		private int borderAlign;
		
		public WRect(Rectangle r, Color fillColor, Color borderColor, float borderWidth, int borderAlign){
			setRect(r);
			this.fillColor = fillColor;
			this.borderColor = borderColor;
			this.borderWidth = borderWidth;
			this.borderAlign = borderAlign;
		}
		public WRect(Rectangle r, Color fillColor, Color borderColor, float borderWidth){
			this(r, fillColor, borderColor, borderWidth, Align.center);
		}
		public WRect(Rectangle r, Color fillColor){
			this(r, fillColor, Color.CLEAR, 0);
		}

		@Override
		public void drawShapes(){
			if(fillColor != Color.CLEAR){
				renderer.setColor(fillColor);
				renderer.rect(getX(), getY(), getWidth(), getHeight());
			}
			
			if(borderWidth != 0 && borderColor != Color.CLEAR){
				renderer.end();
				renderer.begin(ShapeRenderer.ShapeType.Line);
				renderer.setColor(borderColor);
				Gdx.gl.glLineWidth(borderWidth);
				
				//bottom, left, top, right
				if(borderAlign == Align.left){
					renderer.line(getX()-borderWidth, getY()-borderWidth/2, getX()+getWidth()+borderWidth, getY()-borderWidth/2);
					renderer.line(getX()-borderWidth/2, getY(), getX()-borderWidth/2, getY()+getHeight());
					renderer.line(getX()-borderWidth, getY()+getHeight()+borderWidth/2, getX()+getWidth()+borderWidth, getY()+getHeight()+borderWidth/2);
					renderer.line(getX()+getWidth()+borderWidth/2, getY(), getX()+getWidth()+borderWidth/2, getY()+getHeight());
				}else if(borderAlign == Align.center){
					renderer.line(getX()-borderWidth/2, getY(), getX()+getWidth()+borderWidth/2, getY());
					renderer.line(getX(), getY(), getX(), getY()+getHeight());
					renderer.line(getX()-borderWidth/2, getY()+getHeight(), getX()+getWidth()+borderWidth/2, getY()+getHeight());
					renderer.line(getX()+getWidth(), getY(), getX()+getWidth(), getY()+getHeight());
				}else if(borderAlign == Align.right){
					renderer.line(getX(), getY()+borderWidth/2, getX()+getWidth(), getY()+borderWidth/2);
					renderer.line(getX()+borderWidth/2, getY(), getX()+borderWidth/2, getY()+getHeight());
					renderer.line(getX(), getY()+getHeight()-borderWidth/2, getX()+getWidth(), getY()+getHeight()-borderWidth/2);
					renderer.line(getX()+getWidth()-borderWidth/2, getY(), getX()+getWidth()-borderWidth/2, getY()+getHeight());
				}else
					throw new IllegalArgumentException("WRect border alignments can only be left (outside), center, or right (inside)");
			}//dont end it, done next in wshape
		}
	}
}
