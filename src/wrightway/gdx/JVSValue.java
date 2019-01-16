package wrightway.gdx;

import com.badlogic.gdx.utils.*;
import java.util.*;
import static wrightway.gdx.JVSParser.isTruthy;
import wrightway.gdx.JVSValue.*;

public interface JVSValue{
	public Object get(Scope scope)

	static public class JVSVal<T> implements JVSValue{
		private T v;
		public JVSVal(T v){
			this.v = v;
		}
		@Override
		public Object get(Scope scope){
			return v;
		}
		public boolean isNull(){
			return v == null;
		}
		@Override
		public String toString(){
			return String.valueOf(v);
		}
	}
	
	
	/*vars.put(, new Function(new String[]{}, new JVSValue[]{new JVSValue(){
	 @Override
	 public Object get(Scope scope){

	 }
	 }}));*/
	/*vars.put(, new JVSValue(){
	 @Override
	 public Object get(Scope scope){

	 }
	 });*/


	static public class Number extends JVSVal<Float>{
		public Number(float v){
			super(v);
		}
	}
	static public class Bool extends JVSVal<Boolean>{
		public Bool(boolean v){
			super(v);
		}
	}
	static public Bool tru = new Bool(true);
	static public Bool fal = new Bool(false);
	static public class Text implements JVSValue,Dict<JVSValue>{
		private String v;
		private Scope vars;
		public Text(String val){
			this.v = val;
			this.vars = new Scope(null, "txt");
			vars.put("sub", new Function(new String[]{"start", "end", "step"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								String rtn = new String();
								int start = scope.getVal("start", Integer.class, null), end = scope.getVal("end", Integer.class, 0), step = scope.getVal("step", Integer.class, 1);
								if(start < 0) start = v.length() - start;
								if(end <= 0) end = v.length() - end;
								boolean rev = step < 0;
								if(rev)
									for(int i = end - 1; i >= start; i += step)
										rtn.concat(String.valueOf(v.charAt(i)));
								else
									for(int i = start; i < end; i += step)
										rtn.concat(String.valueOf(v.charAt(i)));
								return new Text(rtn);
							}
						}}));
			vars.put("multiply", new Function(new String[]{"amt"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								String rtn = new String();
								int amt = scope.getVal("amt", Integer.class, null);
								for(int i = 0; i < amt; i++)
									rtn = rtn.concat(v);
								return new Text(rtn);
							}
						}}));
			vars.put("concat", new Function(new String[]{"text"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								v = v.concat(scope.getVal("text", String.class, null));
								return null;
							}
						}}));
			vars.put("contains", new Function(new String[]{"text"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return new Bool(v.contains(scope.getVal("text", String.class, null)));
							}
						}}));
			vars.put("split", new Function(new String[]{"text"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								String[] split = v.split(scope.getVal("text", String.class, null));
								WObject rtn = new WObjectArr();
								for(int i = 0; i < split.length; i++)
									rtn.put(String.valueOf(i), new Text(split[i]));
								return rtn;
							}
						}}));
			vars.put("length", new JVSValue(){
					@Override
					public Object get(Scope scope){
						return new Number(v.length());
					}
				});
		}
		@Override
		public Object get(Scope scope){
			return v;
		}
		@Override
		public JVSValue get(String key){
			return vars.get(key);
		}
		@Override
		public JVSValue put(String key, JVSValue value){
			return vars.put(key, value);
		}
		@Override
		public String toString(){
			return v;
		}
	}

	static public class Function implements JVSValue{
		private Array<JVSValue> v;
		private String[] keys;
		//Ignore this: //Note: PARENT AND ENCOMP ARE THE SAME THING TRUST ME. PARENT SHOULD NOT BE WHERE THE FUNC IS GOING
		public Function(String[] keys, JVSValue[] body){
			this.v = new Array<JVSValue>(JVSValue.class);
			if(body != null)
				for(JVSValue v : body)
					this.v.add(v);
			this.keys = keys;
		}
		public Function(String[] keys){
			this(keys, null);
		}
		public Function(){
			this(null);
		}
		@Override
		public Object get(Scope scope){
			return this;
		}
		public Object get(Scope scope, JVSValue[] values){
			scope = new Scope(scope, "fof "+scope.id);
			Log.parseLog("Funcing", Arrays.toString(keys), Arrays.toString(values), scope);
			if(keys != null && values != null)
				for(int i = 0; i < values.length; i++)
					scope.forcePut(keys[i], new JVSVal<Object>(values[i].get(scope.parent)));

			Object o = null;
			for(JVSValue v : this.v){
				Log.parseLog("Func", v, scope);
				o = v.get(scope);
				Log.parseLog("Func got", o);
				if(v instanceof Return)
					return new Return(o);
			}
			return o;
		}
		public Object getFromScope(Scope scope, Scope values){
			Log.parseLog("getfromscope", this, Arrays.toString(keys), values, scope);
			return get(scope, values.getVals(keys));
		}
		public void add(JVSValue v){
			this.v.add(v);
		}
		public boolean isEmpty(){
			return v == null || v.isEmpty();
		}
		@Override
		public String toString(){
			return super.toString()+((Log.getVerbosity() & 0b100_0000) != 0 ? "§"+Log.stringify("\n\t", v.toArray()) : "");
		}
		public class Caller extends FunctionCaller{
			public Caller(JVSValue[] args){
				super(Function.this, args);
			}
		}
	}
	static public class FunctionCaller implements JVSValue{
		private JVSValue func;
		private JVSValue[] args;
		public FunctionCaller(JVSValue func, JVSValue[] args){
			this.func = func;
			this.args = args;
		}
		@Override
		public Object get(Scope scope){
			Function func = (Function)this.func.get(scope);
			if(func == null)
				throw new NullPointerException("Attempt to call a null function "+this.func+" in scope "+scope);
			Log.parseLog("FuncCaller", scope, this.func, func, Arrays.toString(args));
			Object v = func.get(scope, args);
			return v;
		}
		@Override
		public String toString(){
			return super.toString()+"§"+func;
		}
	}

	static public class WObject<V> implements JVSValue,Dict,Iterable<ObjectMap.Entry<String,Object>>{
		private OrderedMap<String,V> vars;
		private boolean sorted;
		public WObject(){
			vars = new OrderedMap<String,V>();
		}
		@Override
		public Object get(Scope scope){
			return this;
		}
		@Override
		public V get(String key){
			Log.parseLog("Getting", key, "from", this);
			V x = vars.get(key);
			if(x instanceof WValueI){
				Log.parseLog("Is a WValueI");
				try{
					return (V)new JVSVal(((WValueI)x).get());
				}catch(UnsupportedOperationException e){
					throw new UnsupportedOperationException("Tried to get private "+key, e);
				}
			}else
				return x;
		}
		public String getKey(Object value){
			return vars.findKey(value, false);
		}
		@Override
		public V put(String key, V value){
			Log.parseLog("Putting", key, value, "was", vars.get(key), "in", this);
			if(vars.get(key) instanceof WValueI){
				Log.parseLog("Is a WValueI");
				try{
					((WValueI)vars.get(key)).put(value instanceof JVSVal ? ((JVSVal)value).get(null) : value);
				}catch(UnsupportedOperationException e){
					throw new UnsupportedOperationException("Tried to put private "+key+" and "+value, e);
				}
			}else
				vars.put(key, value);
			onChanged();
			return value;
		}
		public void putAll(WObject<V> other){
			vars.putAll(other.vars);
			onChanged();
		}
		public V remove(String k){
			if(!containsKey(k))
				return null;
			V rtn = vars.remove(k);
			onChanged();
			return rtn;
		}
		public boolean containsKey(String key){
			return vars.containsKey(key);
		}
		public boolean containsValue(V value){
			return vars.containsValue(value, false);
		}
		protected void onChanged(){
			sorted = false;
		}
		public void sort(){
			vars.orderedKeys().sort();
			sorted = true;
		}
		public void collapse(){
			if(!sorted)
				sort();
			Array<V> arr = new Array<V>();
			for(ObjectMap.Entry<String,V> e : this){
				try{
					Integer.parseInt(e.key);
				}catch(NumberFormatException ex){
					continue;
				}
				arr.add(e.value);
			}
			for(int i = 0; i < arr.size; i++)
				vars.put(String.valueOf(i), arr.get(i));
		}
		public int length(){
			return vars.size;
		}
		public boolean isEmpty(){
			return vars.isEmpty();
		}
		@Override
		public Iterator<ObjectMap.Entry<String,Object>> iterator(){
			sort();
			return (Iterator<ObjectMap.Entry<String,Object>>)vars.iterator();
		}
		@Override
		public String toString(){
			if((Log.getVerbosity() & 0b100_0000) == 0)
				return super.toString();
			String rtn = super.toString() + "[";
			for(ObjectMap.Entry<String,Object> e : this)
				rtn += "<" + e.key + "," + e.value + ">, ";
			return rtn.substring(0, rtn.length() - 2) + "]";
		}
	}
	static public class WObjectMap extends WObject<Object>{
		private int size;
		public WObjectMap(){
			put("put", new Function(new String[]{"key", "value"}, new JVSValue[]{new JVSValue(){
				@Override
				public Object get(Scope scope){
					put(scope.getVal("key", String.class, null), scope.getVal("value", Object.class, null));
						return null;
				}
					}}));
			put("putAll", new Function(new String[]{"other"}, new JVSValue[]{new JVSValue(){
				@Override
				public Object get(Scope scope){
					putAll((WObject<Object>)scope.getVal("other", WObject.class, null));
						return null;
				}
					}}));
			put("get", new Function(new String[]{"key"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return WObjectMap.this.get(scope.getVal("key", String.class, null));
							}
						}}));
			put("containsKey", new Function(new String[]{"key"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return containsKey(scope.getVal("key", String.class, null));
					}
					}}));
			put("containsValue", new Function(new String[]{"value"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return containsValue(scope.getVal("value", Object.class, null));
					}
					}}));
			put("removeKey", new Function(new String[]{"key"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return remove(scope.getVal("key", String.class, null));
					}
					}}));
			put("removeValue", new Function(new String[]{"value"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return remove(getKey(scope.getVal("value", Object.class, null)));
					}
					}}));
			put("length", new JVSValue(){
				@Override
				public Object get(Scope scope){
					return length() - size;
				}
			});
			size = length();
		}
	}
	static public class WObjectArr extends WObject<Object>{
		Array<Object> arr;
		public WObjectArr(){
			put("sub", new Function(new String[]{"start", "end", "step"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								WObject rtn = new WObjectArr();
								int length;
								for(length = 0; containsKey(String.valueOf(length)); length++);
								if(length == 0)
									return rtn;

								int start = scope.getVal("start", Integer.class, null), end = scope.getVal("end", Integer.class, 0), step = scope.getVal("step", Integer.class, 1);
								if(start < 0) start = length - start;
								if(end <= 0) end = length - end;
								boolean rev = step < 0;
								int itr = 0;
								if(rev)
									for(int i = end - 1; i >= start; i += step)
										rtn.put(String.valueOf(itr++), WObjectArr.this.get(String.valueOf(i)));
								else
									for(int i = start; i < end; i += step)
										rtn.put(String.valueOf(itr++), WObjectArr.this.get(String.valueOf(i)));
								return rtn;
							}
						}}));
			put("add", new Function(new String[]{"value"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								int i;
								for(i = 0; containsKey(String.valueOf(i)); i++);
								put(String.valueOf(i), scope.getVal("value", Object.class, null));
								return null;
							}
						}}));
			put("addAll", new Function(new String[]{"other"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								int i = 0;
								WObject<Object> other = scope.getVal("other", WObject.class, null);
								for(ObjectMap.Entry<String,Object> e : other){
									try{
										Integer.parseInt(e.key);
									}catch(NumberFormatException ex){
										continue;
									}
									while(containsKey(String.valueOf(i))) i++;
									put(String.valueOf(i), e.value);
								}
								return null;
							}
						}}));
			put("insert", new Function(new String[]{"i", "value"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								insert(Integer.parseInt(scope.getVal("i", String.class, null)), scope.getVal("value", Object.class, null));
								return null;
							}
						}}));
			put("insertAll", new Function(new String[]{"i", "other"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								int i = Integer.parseInt(scope.getVal("i", String.class, null));
								WObject<Object> other = scope.getVal("other", WObject.class, null);
								for(ObjectMap.Entry<String,Object> e : other){
									try{
										Integer.parseInt(e.key);
									}catch(NumberFormatException ex){
										continue;
									}
									insert(i++, e.value);
								}
								return null;
							}
						}}));
			put("get", new Function(new String[]{"i"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return WObjectArr.this.get(scope.getVal("i", String.class, null));
							}
						}}));
			put("contains", new Function(new String[]{"i"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return containsKey(scope.getVal("i", String.class, null));
							}
						}}));
			put("containsValue", new Function(new String[]{"value", "i"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								return containsValue(scope.getVal("value", String.class, null));
							}
						}}));
			put("remove", new Function(new String[]{"i"}, new JVSValue[]{new JVSValue(){
							@Override
							public Object get(Scope scope){
								String i = scope.getVal("i", String.class, null);
								Integer.parseInt(i);//lol err
								Object rtn = remove(i);
								collapse();
								return rtn;
							}
						}}));
			put("length", new JVSValue(){
					@Override
					public Object get(Scope scope){
						int i;
						for(i = 0; containsKey(String.valueOf(i)); i++);
						return i;
					}
				});
		}
		@Override
		protected void onChanged(){
			super.onChanged();
			arr = null;
		}
		public void insert(int i, Object value){
			shiftUp(i);
			put(String.valueOf(i), value);
		}
		private void shiftUp(int i){ //shifts this and up indexes up by 1
			if(!containsKey(String.valueOf(i)))
				return;
			if(containsKey(String.valueOf(i + 1)))
				shiftUp(i + 1);
			put(String.valueOf(i + 1), get(String.valueOf(i)));
			remove(String.valueOf(i));
		}
		public Array<Object> toArray(){
			if(arr == null){
				arr = new Array<>();
				for(ObjectMap.Entry<String,Object> e : this)
					try{Integer.parseInt(e.key); arr.add(e.value);}catch(NumberFormatException ex){continue;}
			}
			return arr;
		}
	}
	static public class Scope extends WObject<JVSValue>{
		private Scope parent;
		public String id;
		public Scope(Scope parent, String id){
			this.parent = parent;
			this.id = id;
		}
		@Override
		public JVSValue get(String key){
			if(super.containsKey(key))
				return super.get(key);
			if(parent != null && parent.containsKey(key))
				return parent.get(key);
			return null;
		}
		public <T> T getVal(String key, Class<T> clazz, T def){
			JVSValue x = get(key);
			if(x != null){
				Object y = x.get(this);
				if(y instanceof JVSVal)
					y = ((JVSVal)y).get(this);
				if(y == nul && def != null)
					return def;
				else if(y == null && def != Object.class)
					throw new NullPointerException("Attempt to access null variable "+key+" in scope "+this);
				return cast(y, clazz);
			}else{
				if(def == null)
					throw new NullPointerException("Attempt to access null variable "+key+" in scope "+this);
				else
					return def;
			}
		}
		public JVSValue[] getVals(String[] keys){
			JVSValue[] vals = new JVSValue[keys.length];
			for(int i = 0; i < keys.length; i++)
				vals[i] = get(keys[i]);
			Log.parseLog("GetVals", Arrays.toString(keys), Arrays.toString(vals));
			return vals;
		}
		@Override
		public JVSValue put(String key, JVSValue value){
			if(value == null || (value instanceof JVSVal && ((JVSVal)value).isNull()))
				throw new NullPointerException("Attempt to assign nonexistant value to "+key+" in scope "+this);
			if(super.containsKey(key))
				super.put(key, value);
			else if(parent != null && parent.containsKey(key))
				parent.put(key, value);
			else
				super.put(key, value);
			return value;
		}
		private void forcePut(String key, JVSValue value){
			Log.parseLog("Forcing", key, value, "in", this);
			if(value == null || (value instanceof JVSVal && ((JVSVal)value).isNull()))
				throw new NullPointerException("Attempt to assign nonexistant value to "+key+" in scope "+this);
			super.put(key, value);
		}
		@Override
		public boolean containsKey(String key){
			return super.containsKey(key) || (parent != null && parent.containsKey(key));
		}
		public Object run(String func){
			Log.verbose("Running", func, "from", this);
			Object f = getVal(func, Object.class, null);
			if(f == null)
				throw new NullPointerException("Attempt to call a null function "+func+" FROM A SYSTEM CONTEXT in scope "+this);
			else if(f == nul)
				return f;
			return ((Function)f).get(this, null);
		}
		@Override
		public String toString(){
			return super.toString()+"§§"+id;
		}
		
		static public <T> T cast(Object x, Class<T> clazz){
			     if(clazz == String.class)  return (T)x.toString();
			else if(clazz == Integer.class) return (T)(Integer)(int)(float)x;
			else return (T)x;
		}
	}

	static public interface Dict<V>{
		public V get(String key)
		public V put(String key, V value)
	}
	static public class Getter implements JVSValue{
		private JVSValue s;
		private String k;
		public Getter(JVSValue s, String k){
			this.s = s;
			this.k = k;
		}
		@Override
		public Object get(Scope scope){
			Log.parseLog("Getter",scope,s,k);
			Object x;
			if(s != null){
				Dict s = (Dict)this.s.get(scope);
				if(s == null)
					throw new NullPointerException("Attempt to access member "+this.s+" from a null object in scope "+scope);
				if(s instanceof Scope)
					x = ((Scope)s).getVal(k, Object.class, null);
				else
					x = s.get(k);
			}else
				x = scope.getVal(k, Object.class, null);
			Log.parseLog("Got", x);
			return x;
		}
		@Override
		public String toString(){
			if(s == null)
				return "<"+k+">";
			else
				return super.toString()+"§<"+s+","+k+">";
		}
	}
	static public class Putter implements JVSValue{
		private JVSValue s, v;
		private String k;
		public Putter(JVSValue s, String k, JVSValue v){
			this.s = s;
			this.k = k;
			this.v = v;
		}
		@Override
		public Object get(Scope scope){
			Log.parseLog("Putter",scope,s,k,v);
			Object x;
			if(s != null)
				x = ((Scope)s.get(scope)).put(k, new JVSVal<Object>(v.get(scope)));
			else
				x = scope.put(k, new JVSVal<Object>(v.get(scope)));
			Log.parseLog("Put", x);
			return x;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+s+","+k+","+v+">";
		}
	}
	static public interface WValueI{
		public Object get()
		public void put(Object value)
	}
	static public class WValue implements WValueI,JVSValue{
		@Override
		public Object get(){
			throw new UnsupportedOperationException("Tried to get a private member");
		}
		@Override
		public void put(Object value){
			throw new UnsupportedOperationException("Tried to set a private member");
		}
		@Override
		public Object get(Scope scope){
			return this;
		}
	}
	//A contructor class to carry object init info putter-like stuff
	static public class WArray implements JVSValue{
		private ArrayMap<String,JVSValue> arr;
		boolean dict = false;
		public WArray(){
			this.arr = new ArrayMap<String,JVSValue>();
		}
		@Override
		public Object get(Scope scope){
			WObject v = dict ? new WObjectMap() : new WObjectArr();
			for(ObjectMap.Entry<String,JVSValue> e : this.arr)
				v.put(e.key, e.value);
			return v;
		}
		public void put(String k, JVSValue v){
			this.arr.put(k, v);
			if(!dict)
				try{
					Integer.parseInt(k);
				}catch(NumberFormatException e){
					dict = true;
				}
		}
	}

	static public class Return implements JVSValue{
		private Object v;
		public Return(Object v){
			this.v = v;
		}
		@Override
		public Object get(Scope scope){
			return v;
		}
		@Override
		public String toString(){
			return super.toString()+"§"+v;
		}
	}

	static public class If implements JVSValue{
		private JVSValue cond, tru, fal;
		public If(JVSValue cond, JVSValue tru, JVSValue fal){
			this.cond = cond;
			this.tru = tru;
			this.fal = fal;
		}
		@Override
		public Object get(Scope scope){
			Log.parseLog("IfCond", cond);
			Function which = (Function)(JVSParser.isTruthy(cond.get(scope)) ? tru.get(scope) : fal == null ? null : fal.get(scope));
			Object v = which == null ? null : which.get(scope, null);
			return v instanceof Return ? v : null;
		}
		@Override
		public String toString(){
			return super.toString()+"§"+cond;
		}
	}

	static public class While implements JVSValue{
		private JVSValue cond, tru;
		private boolean dowhile;
		public While(JVSValue cond, JVSValue tru, boolean dowhile){
			this.cond = cond;
			this.tru = tru;
			this.dowhile = dowhile;
		}
		public While(JVSValue cond, JVSValue tru){
			this(cond, tru, false);
		}
		@Override
		public Object get(Scope scope){
			Function which = (Function)tru.get(scope);
			Object v = dowhile ? which.get(scope, null) : null;
			while(!(v instanceof Return) && JVSParser.isTruthy(cond.get(scope)))
				v = which.get(scope, null);
			return v instanceof Return ? v : null;
		}
		@Override
		public String toString(){
			return super.toString()+"§"+cond;
		}
	}

	static public class For implements JVSValue{
		private String key, val;
		private JVSValue arr, tru;
		public For(String key, String val, JVSValue arr, JVSValue tru){
			this.key = key;
			this.val = val;
			this.arr = arr;
			this.tru = tru;
		}
		@Override
		public Object get(Scope scope){
			Function which = (Function)tru.get(scope);
			Object v = null;
			for(ObjectMap.Entry<String,JVSValue> e : ((WObject)arr.get(scope))){
				Scope nscope = new Scope(scope, "forof "+scope.id);
				nscope.put(key, new Text(e.key));
				if(val != null)
					nscope.put(val, e.value);
				v = which.get(nscope, null);
				if(v instanceof Return)
					break;
			}
			return v instanceof Return ? v : null;
		}
		@Override
		public String toString(){
			return super.toString()+"§"+arr;
		}
	}

	static public class Or implements JVSValue{
		private JVSValue v, w;
		public Or(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope);//, w = this.w.get(scope);
			if(v instanceof Float)
				return (Integer)v | (Integer)w.get(scope);
			else
				return isTruthy(v) || isTruthy(w.get(scope));
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class XOr implements JVSValue{
		private JVSValue v, w;
		public XOr(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope), w = this.w.get(scope);
			if(v instanceof Float)
				return (Integer)v ^ (Integer)w;
			else
				return isTruthy(v) ^ isTruthy(w);
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class And implements JVSValue{
		private JVSValue v, w;
		public And(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope);//, w = this.w.get(scope);
			if(v instanceof Float)
				return (Integer)v & (Integer)w.get(scope);
			else
				return isTruthy(v) && isTruthy(w.get(scope));
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class Not implements JVSValue{
		private JVSValue v;
		public Not(JVSValue v){
			this.v = v;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope);
			return !isTruthy(v);
		}
		@Override
		public String toString(){
			return super.toString()+"§"+v;
		}
	}
	static public class NotBit implements JVSValue{
		private JVSValue v;
		public NotBit(JVSValue v){
			this.v = v;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope);
			return ~Scope.cast(v, Integer.class);
		}
		@Override
		public String toString(){
			return super.toString()+"§"+v;
		}
	}

	static public class Is implements JVSValue{
		private JVSValue v, w;
		public Is(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope), w = this.w.get(scope);
			return v == w;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class Equals implements JVSValue{
		private JVSValue v, w;
		public Equals(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope), w = this.w.get(scope);
			return v.equals(w);
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class LessThan implements JVSValue{
		private JVSValue v, w;
		private boolean eq;
		public LessThan(JVSValue v, JVSValue w, boolean eq){
			this.v = v;
			this.w = w;
			this.eq = eq;
		}
		@Override
		public Object get(Scope scope){
			Comparable v = (Comparable)this.v.get(scope), w = (Comparable)this.w.get(scope);
			return eq ? v.compareTo(w) <= 0 : v.compareTo(w) < 0;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class MoreThan implements JVSValue{
		private JVSValue v, w;
		private boolean eq;
		public MoreThan(JVSValue v, JVSValue w, boolean eq){
			this.v = v;
			this.w = w;
			this.eq = eq;
		}
		@Override
		public Object get(Scope scope){
			Comparable v = (Comparable)this.v.get(scope), w = (Comparable)this.w.get(scope);
			return eq ? v.compareTo(w) >= 0 : v.compareTo(w) > 0;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}

	static public class LeftShift implements JVSValue{
		private JVSValue v, w;
		public LeftShift(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Integer v = (Integer)this.v.get(scope), w = (Integer)this.w.get(scope);
			return v << w;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class RightShift implements JVSValue{
		private JVSValue v, w;
		private boolean keepSign;
		public RightShift(JVSValue v, JVSValue w, boolean keepSign){
			this.v = v;
			this.w = w;
			this.keepSign = keepSign;
		}
		@Override
		public Object get(Scope scope){
			Integer v = (Integer)this.v.get(scope), w = (Integer)this.w.get(scope);
			return keepSign ? v >> w : v >>> w;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}

	static public class Add implements JVSValue{
		private JVSValue v, w;
		public Add(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope), w = this.w.get(scope);
			if(v instanceof Float)
				return (Float)v + (Float)w;
			else
				return v.toString() + w.toString();
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class Subtract implements JVSValue{
		private JVSValue v, w;
		public Subtract(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Float v = (Float)this.v.get(scope), w = (Float)this.w.get(scope);
			return v - w;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class Multiply implements JVSValue{
		private JVSValue v, w;
		public Multiply(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Object v = this.v.get(scope), w = this.w.get(scope);
			if(v instanceof Float)
				return (Float)v * (Float)w;
			else if(w instanceof Float){
				return ((Function)new Text(v.toString()).get("multiply")).get(scope, new JVSValue[]{new Number(w)});
			}else
				return (Float)v;//error lol
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class Divide implements JVSValue{
		private JVSValue v, w;
		public Divide(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Float v = (Float)this.v.get(scope), w = (Float)this.w.get(scope);
			return v / w;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class Modulize implements JVSValue{
		private JVSValue v, w;
		public Modulize(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Float v = (Float)this.v.get(scope), w = (Float)this.w.get(scope);
			return v % w;
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}
	static public class Exponentiate implements JVSValue{
		private JVSValue v, w;
		public Exponentiate(JVSValue v, JVSValue w){
			this.v = v;
			this.w = w;
		}
		@Override
		public Object get(Scope scope){
			Float v = (Float)this.v.get(scope), w = (Float)this.w.get(scope);
			return (float)Math.pow(v, w);
		}
		@Override
		public String toString(){
			return super.toString()+"§<"+v+","+w+">";
		}
	}

	static public class NulWrapper implements JVSValue{
		@Override
		public Object get(Scope scope){
			return nul;
		}
	}
	static public class Nul{}
	static public Nul nul = new Nul();
	static public NulWrapper nulll = new NulWrapper();
}
