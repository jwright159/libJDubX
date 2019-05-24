package wrightway.gdx;

import com.badlogic.gdx.files.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.*;
import java.util.*;
import com.badlogic.gdx.maps.objects.*;

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
}
