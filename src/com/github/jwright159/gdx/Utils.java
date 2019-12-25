package com.github.jwright159.gdx;

import com.badlogic.gdx.files.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.*;
import java.util.*;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.scenes.scene2d.*;

public abstract class Utils{
	public static String[] trimArray(String[] array){
		String[] rtn = new String[array.length];
		for(int i = 0; i < array.length; i++)
			rtn[i] = array[i].trim();
		return rtn;
	}
	public static boolean endsInPunctuation(String string){
		return string.matches(".*[.?!;:,]");
	}
	public static boolean endsInWhitespace(String string){
		return string.matches(".*\\s");
	}
	
	public static FileHandle getRelativeFileHandle(FileHandle file, String path){
		StringTokenizer tokenizer = new StringTokenizer(path, "\\/");
		FileHandle result = file.parent();
		while(tokenizer.hasMoreElements()){
			String token = tokenizer.nextToken();
			if(token.equals(".."))
				result = result.parent();
			else{
				result = result.child(token);
			}
		}
		return result;
	}
	
	public static RectangleMapObject copyRectangleMapObject(RectangleMapObject o){
		RectangleMapObject r = new RectangleMapObject();
		r.setColor(o.getColor());
		r.setName(o.getName());
		r.setOpacity(o.getOpacity());
		r.setVisible(o.isVisible());
		r.getRectangle().set(o.getRectangle());
		r.getProperties().putAll(o.getProperties());
		return r;
	}
	
	public static void setActorFromActor(Actor to, Actor from){
		to.setBounds(from.getX(), from.getY(), from.getWidth(), from.getHeight());
		to.setScale(from.getScaleX(), from.getScaleY());
		to.setName(from.getName());
		to.setTouchable(from.getTouchable());
		to.setOrigin(from.getOriginX(), from.getOriginY());
		to.setDebug(from.getDebug());
		to.setVisible(from.isVisible());
		to.setColor(from.getColor());
		to.setUserObject(from.getUserObject());
		to.setRotation(from.getRotation());
	}
	
	public static <T> void insertAllFirst(Array<T> a, Array<T> b){
		Array<T> c = new Array<T>(a);
		a.clear();
		a.addAll(b);
		a.addAll(c);
	}
	
	public static Interpolation constant = new Interpolation(){
		@Override
		public float apply(float alpha){
			return alpha < 1 ? 0 : 1;
		}
	};
	public static Interpolation getInterpolation(String interp){
		if(interp != null)
			try{
				return (Interpolation)ClassReflection.getField(Interpolation.class, interp).get(null);
			}catch(ReflectionException e){
				Log.error("Invalid interpolation", interp);
			}
		return constant;
	}
	
	private static long lastTimeNanos;
	public static float time(){
		float rtn = (float)TimeUtils.timeSinceNanos(lastTimeNanos) / 1000000f;
		lastTimeNanos = TimeUtils.nanoTime();
		return rtn;
	}
	
	public static String filename(String fullfile){
		return fullfile.contains(".") ? fullfile.substring(0, fullfile.lastIndexOf('.')) : fullfile;
	}
	
	public static final double VERTICALMIN = 0.02;
	public static Vector3 rotateVertically(Vector3 vector, double rotation){
		rotation *= Math.PI / 180.0;
		double r = vector.len();
		double phi = getPhi(vector);
		double theta = getTheta(vector);
		theta -= rotation;
		theta = MathUtils.clamp(theta, VERTICALMIN, Math.PI - VERTICALMIN);
		return vector.set(getXComponent(r, theta, phi), getYComponent(r, theta, phi), getZComponent(r, theta, phi));
	}
	public static Vector3 rotateHorizontally(Vector3 vector, double rotation){
		rotation *= Math.PI / 180.0;
		double r = vector.len();
		double phi = getPhi(vector);
		phi += rotation;
		double theta = getTheta(vector);
		return vector.set(getXComponent(r, theta, phi), getYComponent(r, theta, phi), getZComponent(r, theta, phi));
	}

	// <r, phi, theta> == <mag, x-z plane, y-xz plane>
	public static double getPhi(Vector3 vector){
		return (vector.x == 0 ? (vector.z > 0 ? Math.PI / 2.0 : -Math.PI / 2.0) :
			Math.atan(vector.z / vector.x)) + (vector.x < 0 ? Math.PI : 0);
	}
	public static double getTheta(Vector3 vector){
		return Math.acos(vector.y / vector.len());
	}
	public static float getXComponent(double r, double theta, double phi){
		return (float)(r * Math.sin(theta) * Math.cos(phi));
	}
	public static float getYComponent(double r, double theta, double phi){
		return (float)(r * Math.cos(theta));
	}
	public static float getZComponent(double r, double theta, double phi){
		return (float)(r * Math.sin(theta) * Math.sin(phi));
	}
}
