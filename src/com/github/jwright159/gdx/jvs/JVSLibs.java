package com.github.jwright159.gdx.jvs;

import com.github.jwright159.gdx.jvs.JVSValue.*;
import com.badlogic.gdx.math.*;

public class JVSLibs extends WObject{
	public JVSLibs(){
		super();
		put("math", new WMath());
	}
	
	public static class WMath implements JVSValue{
		private Scope vars;
		public WMath(){
			vars = new Scope(null, "math");
			vars.put("random", new Function(new String[]{}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return MathUtils.random();
							}
						}}));
			vars.put("sin", new Function(new String[]{"value"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return MathUtils.sin(scope.getVal("value", Float.class, null));
							}
						}}));
			vars.put("cos", new Function(new String[]{"value"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return MathUtils.cos(scope.getVal("value", Float.class, null));
							}
						}}));
			vars.put("tan", new Function(new String[]{"value"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return (float)(double)Math.tan(scope.getVal("value", Float.class, null));
							}
						}}));
			vars.put("PI", new Number(MathUtils.PI));
		}
		
		@Override
		public Object get(Scope scope){
			return vars;
		}
	}
}
