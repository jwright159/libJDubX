package wrightway.gdx;

import com.badlogic.gdx.utils.*;
import java.util.*;

public final class JVSParser{
	public static JVSValue.Function parseCode(String code, String[] args){
		return new JVSParser(code, args).parse();
	}

	private int ch, pos = -1;
	private final String code;
	//private JVSValue.Scope scope;//If i really want to, i can put this in each func, but should be unnecessary
	private String[] args;

	public JVSParser(String code, String[] args){
		this.code = code;
		this.args = args;
	}

	private void nextChar(){
		ch = (++pos < code.length()) ? code.charAt(pos) : -1;
	}

	private void prevChar(){
		ch = (--pos >= 0) ? code.charAt(pos) : -1;
	}

	private void prevTo(String to){
		while(!code.substring(pos).startsWith(to))
			prevChar();
	}
	private boolean eat(String strToEat){
		String kw = parseKeyword();
		if(kw.equals(strToEat)){
			return true;
		}else{
			prevTo(kw);
			return false;
		}
	}

	private boolean eat(int charToEat){
		return eat(charToEat, false);
	}
	private boolean eat(int charToEat, boolean skipEncaps){
		if(poll(charToEat, skipEncaps)){
			nextChar();
			return true;
		}
		return false;
	}

	private boolean poll(int charToEat){
		return poll(charToEat, false);
	}
	private boolean poll(int charToEat, boolean skipEncaps){
		while(Character.isWhitespace(ch)) nextChar();
		if(ch == '\\'){
			nextChar();
			nextChar();
			return poll(charToEat);
		}

		if(ch == '/' && (code.charAt(pos + 1) == '/' || code.charAt(pos + 1) == '*')){
			Log.parseVerbose("Discovered a comment!");
			if(code.charAt(pos + 1) == '/'){
				//Log.parseVerbose("Single line");
				while(ch != '\n' && ch != -1)
					nextChar();
			}else if(code.charAt(pos + 1) == '*'){
				Log.parseVerbose("Multi line");
				nextChar();
				nextChar();
				nextChar();
				while(!(code.charAt(pos - 1) == '*' && ch == '/') && ch != -1){
					//Log.debug(String.valueOf(code.charAt(pos-1))+ch);
					nextChar();
				}
				nextChar();
			}
			eat(' ');
			//Log.parseLog("Resuming at " + Character.toChars(charToEat)[0]);
			return poll(charToEat);
		}

		if(charToEat == ' '){//clears whitespace
			if(ch == ' '){
				nextChar();
				poll(' ');
			}
			return false;//if you use eat instead of poll, this keeps it from nextCharing
		}

		if(skipEncaps && ch != charToEat && charToEat != '"' && (ch == '(' || ch == '[' || ch == '{')){
			Log.parseVerbose("Nest! " + Character.toChars(ch)[0]);
			int c = ch;
			do nextChar(); while(!poll(c == '(' ? ')' : c == '[' ? ']' : c == '{' ? '}' : -1, true));
			Log.parseVerbose("Unnest! " + Character.toChars(ch)[0]);
			nextChar();
			return poll(charToEat, skipEncaps);
		}

		Log.parseVerbose("Polling at " + pos + ", expected '" + Character.toChars(charToEat)[0] + "', got '" + (ch != -1 ? Character.toChars(ch)[0] + "" : "[-1]") + "'");
		if(ch == -1 && skipEncaps)
			throw new RuntimeException("Unexpected end of file! Perhaps didn't close all parentheses/brackets/braces?");
		if(ch == charToEat)
			return true;
		return false;
	}

	public JVSValue.Function parse(){
		Log.parseLog("\nParsing code:\n" + code + "\n");
		nextChar();
		JVSValue.Function x = new JVSValue.Function(args);
		//scope = x.getScope();//new one created by x, was a placeholder for the parent before
		//try{
		while(pos < code.length() - 1){
			x.add(parseFunction());
			eat(' ');
		}
		/*}catch(Return r){
		 if(r.level())
		 x = r.rtn;
		 else
		 throw r;
		 }*/
		Log.parseLog("Parsed contents of function, got " + x);
		if(pos < code.length()) throw new RuntimeException("Unexpected: " + (char)ch + " at " + pos);
		return x;
	}

	private JVSValue parseFunction(){
		Log.parseVerbose("Parsing function!");
		JVSValue x = null;//scope;
		String kw;
		do{
			Log.parseVerbose("Next func part");
			kw = parseKeyword();
			//if(x == null)
			//	throw new NullPointerException("Tried to get field \"" + kw + "\" from null pointer at " + pos);//I dont know now???

			switch(kw){
				case "return":
					/*if(eat('}'))
					 x = new JVSValue.Return(JVSValue.nulll);
					 else*///shouldnt the context take care of this?
					x = new JVSValue.Return(parseExpression());
					break;

				case "if":
					JVSValue cond = parseArguments()[0];
					JVSValue tru = parseFunctionConstruction();
					JVSValue fal = null;
					if(eat("else"))
						fal = parseFunctionConstruction();
					x = new JVSValue.If(cond, tru, fal);
					break;

				case "do":
				case "while":
					boolean dowhile = kw.equals("do");
					if(dowhile){
						JVSValue whil = parseFunctionConstruction();
						eat("while");
						cond = parseArguments()[0];//not defining as jvsv bc already exists from if
						x = new JVSValue.While(cond, whil, true);
						break;
					}else{
						cond = parseArguments()[0];
						JVSValue whil = parseFunctionConstruction();
						x = new JVSValue.While(cond, whil);
						break;
					}

				case "for":
					if(!eat('('))
						throw new RuntimeException("Expected a ( for a for");
					String key = parseKeyword(), val = null;
					if(eat(','))
						val = parseKeyword();
					if(!eat(':'))
						throw new RuntimeException("Expected a : for a for");
					JVSValue arr = parseExpression();
					if(!eat(')'))
						throw new RuntimeException("Expected a ) for a for");
					JVSValue.Function inner = parseFunctionConstruction();
					x = new JVSValue.For(key, val, arr, inner);
					break;
				
				case ""://??? for parends and brackets after other parends or brackets or braces???
					//	x = parseValue();
					break;

				default:
					if(eat('=')){
						if(poll('=')){//getter
							prevChar();
							prevTo("=");
							x = new JVSValue.Getter(x, kw);
							Log.parseVerbose("Got a var", kw, x);
						}else{//putter
							Log.parseLog("Putting", kw, "in", x);
							x = new JVSValue.Putter(x, kw, parseExpression());
						}
					}else{//getter
						x = new JVSValue.Getter(x, kw);
						Log.parseVerbose("Got a var", kw, x);
					}
			}

			//TODO
			//What about the returned object types? Do i need to know what they are?
			//Like if this returns a func that needs called, x would be funccaller not func
			//Wait these wouldnt be funcs anyway; the get() is the func
			//This will have to be determined at runtime
			//or will it?
			//idk
			if(poll('(')){
				if(x == null){
					x = parseExpression();
				}else{
					//Log.parseLog("Calling a function!", x, scope, scope2);
					x = new JVSValue.FunctionCaller(x, parseArguments());
				}
			}else if(poll('[')){
				eat('[');
				JVSValue start = parseExpression(), end = null, step = null;
				if(eat(':')){
					end = parseExpression();
					if(eat(':')){
						step = parseExpression();
					}
				}
				if(!eat(']'))
					throw new RuntimeException("Expected a ] for subset");
				x = new JVSValue.FunctionCaller(new JVSValue.Getter(x, "sub"), new JVSValue[]{start, end, step});
			}
		}while(eat('.'));

		Log.parseVerbose("Value of parsed function is", x);
		return x;
	}

	private String parseKeyword(){
		eat(' ');
		int startpos = pos;
		if(Character.isLetter(ch))
			while(Character.isLetterOrDigit(ch))
				nextChar();
		String x = code.substring(startpos, pos);
		Log.parseLog("Parsed keyword, got " + x);
		return x;
	}

	private JVSValue parseExpression(){
		return parseOr();
	}

	/*private JVSValue parseWhatever(){
	 Object x = parseWhatever2();
	 if(x instanceof ?)
	 for(;;){

	 }
	 return x;
	 }*/
	private JVSValue parseOr(){
		JVSValue x = parseXOr();
		for(;;){
			if(eat('|'))
				x = new JVSValue.Or(x, parseXOr());
			else
				return x;
		}
	}
	private JVSValue parseXOr(){
		JVSValue x = parseAnd();
		for(;;){
			if(eat('^'))
				x = new JVSValue.XOr(x, parseAnd());
			else
				return x;
		}
	}
	private JVSValue parseAnd(){
		JVSValue x = parseEquality();
		for(;;){
			if(eat('&'))
				x = new JVSValue.And(x, parseEquality());
			else
				return x;
		}
	}
	private JVSValue parseEquality(){
		JVSValue x = parseInequality();
		for(;;){
			boolean not = eat('!');
			if(eat('=')){
				if(not || eat('=')){
					if(!not){
						if(eat('='))
							x = new JVSValue.Is(x, parseInequality());
						else
							x = new JVSValue.Equals(x, parseInequality());
					}else{
						if(eat('='))
							x = new JVSValue.Not(new JVSValue.Is(x, parseInequality()));
						else
							x = new JVSValue.Not(new JVSValue.Equals(x, parseInequality()));
					}
				}else{
					prevTo("=");
					return x;
				}
			}else{
				if(not){
					prevChar();
					prevTo("!");
				}
				return x;
			}
		}
	}
	private JVSValue parseInequality(){
		JVSValue x = parseBitshifting();
		for(;;){
			if(eat('<')){
				if(poll('<')){
					prevChar();
					prevTo("<");
					return x;
				}
				boolean equal = eat('=');
				x = new JVSValue.LessThan(x, parseBitshifting(), equal);
			}else if(eat('>')){
				if(poll('>')){
					prevChar();
					prevTo(">");
					return x;
				}
				boolean equal = eat('=');
				x = new JVSValue.MoreThan(x, parseBitshifting(), equal);
			}else{
				return x;
			}
		}
	}
	private JVSValue parseBitshifting(){
		JVSValue x = parseAddition();
		for(;;){
			if(eat('<')){
				if(eat('<'))
					x = new JVSValue.LeftShift(x, parseAddition());
				else{
					prevTo("<");
					return x;
				}
			}else if(eat('>')){
				if(eat('>')){
					boolean keepSign = !eat('>');
					x = new JVSValue.RightShift(x, parseAddition(), keepSign);
				}else{
					prevTo(">");
					return x;
				}
			}else
				return x;
		}
	}
	private JVSValue parseAddition(){
		JVSValue x = parseMultiplication();
		for(;;){
			if(eat('+')){
				x = new JVSValue.Add(x, parseMultiplication());
			}else if(eat('-')){
				x = new JVSValue.Subtract(x, parseMultiplication());
			}else{
				Log.parseVerbose("Sum is", x);
				return x;
			}
		}
	}
	private JVSValue parseMultiplication(){
		JVSValue x = parseExponentiation();
		for(;;){
			if(eat('*')){
				x = new JVSValue.Multiply(x, parseMultiplication());
			}else if(eat('/')){
				x = new JVSValue.Divide(x, parseMultiplication());
			}else if(eat('%')){
				x = new JVSValue.Modulize(x, parseMultiplication());
			}else{
				Log.parseVerbose("Product is", x);
				return x;
			}
		}
	}
	private JVSValue parseExponentiation(){
		JVSValue x = parseExpTerm();
		for(;;){
			if(eat('*')){
				if(eat('*'))
					x = new JVSValue.Exponentiate(x, parseExpTerm());
				else{
					Log.parseVerbose("Exp is", x);
					prevTo("*");
					return x;
				}
			}else{
				Log.parseVerbose("Exp is", x);
				return x;
			}
		}
	}
	private JVSValue parseExpTerm(){
		return parseValue();
	}

	private JVSValue parseValue(){
		if(eat('-'))
			return new JVSValue.Multiply(parseValue(), new JVSValue.Number(-1));
		if(eat('+'))// unary plus? okay then
			return parseValue();
		if(eat('!'))
			return new JVSValue.Not(parseValue());
		if(eat('~'))
			return new JVSValue.NotBit(parseValue());

		if(eat('(')){
			Log.parseVerbose("Found a parenthesis");
			JVSValue x = parseExpression();
			if(!eat(')'))
				throw new RuntimeException("Mismatched parends or trying to call an object as a func");
			return x;
		}
		if(poll('[')){
			Log.parseVerbose("Found a bracket");
			return parseArray();
		}
		if(poll('{')){
			Log.parseVerbose("Found a brace");
			return parseFunctionConstruction();
		}

		if(eat('"')){
			int startpos = pos;
			while(!eat('"')) nextChar();
			String s = code.substring(startpos, pos - 1);
			for(int i = 0; i < s.length(); i++)
				if(s.charAt(i) == '\\')
					s = s.substring(0, i) + s.substring(i + 1, s.length());
			return new JVSValue.Text(s);
		}

		//if(eat('}'))//for empty return statements//shouldnt the context take care of this?
		//	return null;


		int startpos = pos;
		if(Character.isDigit(ch)){
			while(Character.isDigit(ch) || ch == '.')
				nextChar();
			JVSValue x = new JVSValue.Number(Float.valueOf(code.substring(startpos, pos)));
			Log.parseLog("Parsed factor, got number", x);
			return x;
		}
		if(Character.isLetter(ch)){
			String kw = parseKeyword();
			JVSValue x = null;
			if(kw.equals("function"))
				x = parseFunctionConstruction();
			else if(kw.equals("true"))
				x = JVSValue.tru;
			else if(kw.equals("false"))
				x = JVSValue.fal;
			else if(kw.equals("null"))
				x = JVSValue.nulll;
			else{
				prevTo(kw);
				x = parseFunction();
			}
			return x;
		}

		//throw new RuntimeException("Unexpected " + Character.toChars(ch)[0]);//for empty returns and such
		return JVSValue.nulll;
	}

	private String[] parseArgumentList(){
		if(!eat('('))
			throw new RuntimeException("Expected a ( for arguments");

		Array<String> args = new Array<String>(String.class);
		if(!poll(')'))
			for(;;){
				args.add(parseKeyword());
				if(!eat(','))
					break;
			}

		if(!eat(')'))
			throw new RuntimeException("Expected a ) for arguments");

		Object[] y = args.toArray();
		String[] x = new String[y.length];
		for(int i = 0; i < y.length; i++)
			x[i] = (String)y[i];
		Log.parseLog("Parsed arglist, got " + Arrays.toString(x));
		return x;
	}

	private JVSValue[] parseArguments(){
		if(!eat('('))
			throw new RuntimeException("Expected a ( for arguments");

		Array<JVSValue> args = new Array<JVSValue>(JVSValue.class);
		if(!poll(')'))
			for(;;){
				Log.parseVerbose("Got an arg");
				args.add(parseExpression());
				if(!eat(','))
					break;
			}

		if(!eat(')'))
			throw new RuntimeException("Expected a ) for arguments");

		Object[] arr = args.toArray();
		JVSValue[] x = new JVSValue[arr.length];
		System.arraycopy(arr, 0, x, 0, arr.length);

		Log.parseLog("Parsed args, got " + Arrays.toString(x));
		return x;
	}

	private JVSValue parseArray(){
		if(!eat('['))
			throw new RuntimeException("Expected a [");

		JVSValue.WArray arr = new JVSValue.WArray();
		if(!poll(']'))
			for(int i = 0;true;i++){
				Log.parseVerbose("Got a member");
				int startpos = pos;//gotta check if theres a key so can tell if string or expr
				while(!poll(':', true) && !poll(',', true) && !poll(']', true))
					nextChar();
				boolean hasKey = poll(':');
				Log.parseVerbose("Key?", hasKey);
				pos = startpos - 1;
				nextChar();

				String key;
				if(hasKey){
					key = parseKeyword();
					if(!eat(':'))
						throw new RuntimeException("Expected a :");
				}else{
					key = String.valueOf(i);
				}
				Log.parseVerbose("Key", key);
				JVSValue x;
				arr.put(key, x = parseExpression());
				Log.parseVerbose("Done with member", x, ",?", poll(','));
				if(!eat(','))
					break;
			}

		if(!eat(']'))
			throw new RuntimeException("Expected a ]");

		Log.parseLog("Parsed args, got", arr);
		return arr;//need to return arr, but also need putters to happen
	}

	private String parseFunctionCode(){
		Log.parseVerbose("Parsing out function code!");
		/*if(!eat('{'))
		 throw new RuntimeException("Didn't start function construction with '{'");*/
		char cha = eat('{') ? '}' : ';';
		int startpos = pos;
		while(!eat(cha, true))
			nextChar();
		return code.substring(startpos, pos - 1).trim();
	}
	private JVSValue.Function parseFunctionConstruction(){
		String[] args = null;
		if(poll('('))
			args = parseArgumentList();
		return parseCode(parseFunctionCode(), args);
	}

	public static boolean isTruthy(Object x){
		Log.parseLog("Truthy in", x, x == null ? "null" : x.getClass());
		     if(x instanceof Boolean)           x = (boolean)x;
		else if(x instanceof Float)             x = (float)x != 0;
		else if(x instanceof String)            x = ((String)x).length() != 0;
		else if(x instanceof JVSValue.WObject)  x = ((JVSValue.WObject)x).isEmpty();
		else if(x instanceof JVSValue.Function) x = !((JVSValue.Function)x).isEmpty();
		//else if(x instanceof JVSValue)          throw new RuntimeException("Tried to truthy an arbitrary JVSValue");//Don't want the get//But dont forget scopeds are now jvsvs
		else                                    x = x != null;
		Log.parseLog("Truthy out", x);
		return x;
	}
}
