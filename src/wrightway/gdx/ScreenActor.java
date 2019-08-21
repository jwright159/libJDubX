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

public abstract class ScreenActor extends Actor implements Disposable{
	public ScreenActor(){
		addListener(new ScreenActorGestureListener());
	}
	public ScreenActor(Actor actor){
		this();
		setBounds(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
		setScale(actor.getScaleX(), actor.getScaleY());
		setName(actor.getName());
		setTouchable(actor.getTouchable());
		setOrigin(actor.getOriginX(), actor.getOriginY());
		setDebug(actor.getDebug());
		setVisible(actor.isVisible());
		setColor(actor.getColor());
		setUserObject(actor.getUserObject());
		setRotation(actor.getRotation());
	}

	public void setRect(Rectangle rect){
		setBounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
	private Rectangle rectBuffer = new Rectangle();
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
	public float getX(int alignment){
		if(Align.isLeft(alignment))
			return getX();
		else if(Align.isRight(alignment))
			return getX() + getTrueWidth();
		else
			return getX() + getTrueWidth() / 2;
	}
	@Override
	public float getY(int alignment){
		if(Align.isBottom(alignment))
			return getY();
		else if(Align.isTop(alignment))
			return getY() + getTrueHeight();
		else
			return getY() + getTrueHeight() / 2;
	}
	
	public void scaleSizeBy(float scale){
		setWidth(getWidth()*scale);
		setHeight(getHeight()*scale);
	}
	
	public boolean clamp(float xmin, float xmax, float ymin, float ymax){
		boolean did = false;
		if(getX() < xmin){
			setX(xmin);
			did = true;
		}else if(getX()+getWidth() > xmax){
			setX(xmax - getWidth());
			did = true;
		}if(getY() < ymin){
			setY(ymin);
			did = true;
		}else if(getY()+getHeight() > ymax){
			setY(ymax - getHeight());
			did = true;
		}
		return did;
	}

	@Override
	public abstract void draw(Batch batch, float parentAlpha)
	
	@Override
	public void dispose(){
		//Log.debug("Disposing of WActor! "+this);
		remove();
	}

	public void touchDown(float x, float y, int pointer, int button){}
	public void touchUp(float x, float y, int pointer, int button){}
	
	public void pan(float x, float y, float deltaX, float deltaY){}
	public void tap(float x, float y, int count, int button){}

	public class ScreenActorGestureListener extends ActorGestureListener{
		@Override
		public void touchDown(InputEvent event, float x, float y, int pointer, int button){
			ScreenActor.this.touchDown(x, y, pointer, button);
		}
		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button){
			ScreenActor.this.touchUp(x, y, pointer, button);
		}
		@Override
		public void pan(InputEvent event, float x, float y, float deltaX, float deltaY){
			ScreenActor.this.pan(x, y, deltaX, deltaY);
		}
		@Override
		public void tap(InputEvent event, float x, float y, int count, int button){
			ScreenActor.this.tap(x, y, count, button);
		}
	}
}
