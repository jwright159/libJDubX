package wrightway.gdx;

import java.io.*;
import com.badlogic.gdx.files.*;

public abstract class Log{
	private static Writer logFile;
	//static final byte verbosity = 0b010_1111;//error=000_0001, user=000_0010, debug=000_0100, verbose=000_1000, verbose2=001_0000, parse=010_0000, parseverbose=100_0000 (only 7 bits bc signed)
	private static byte verbosity;
	
	public static void setVerbosity(String v){
		setVerbosity(Byte.parseByte(v, 2));
	}
	public static void setVerbosity(byte v){
		verbosity = v;
	}
	public static void setLogFile(FileHandle file){
		setLogWriter(file.writer(false));
	}
	public static void setLogWriter(Writer w){
		logFile = w;
		try{
			logFile.write("Begin debugging.");
		}catch(IOException e){}
	}
	public static Writer getLogWriter(){
		return logFile;
	}
	
	public static void error(Object... msg){
		log((byte)0b000_0001, msg);//consider using bitshift (1 << #)
	}
	public static void userLog(Object... msg){
		log((byte)0b000_0010, msg);
	}
	public static void debug(Object... msg){
		log((byte)0b000_0100, msg);
	}
	public static void verbose(Object... msg){
		log((byte)0b000_1000, msg);
	}
	public static void verbose2(Object... msg){
		log((byte)0b001_0000, msg);
	}
	public static void parseLog(Object... msg){
		log((byte)0b010_0000, msg);
	}
	public static void parseVerbose(Object... msg){
		log((byte)0b100_0000, msg);
	}
	public static void log(byte type, Object... msg){
		if(type == 0)
			return;
		byte t = (byte)(type & verbosity);
		String tag = "";
		if((t & 0b000_0001) != 0)
			tag += "ERROR,";
		if((t & 0b000_0010) != 0)
			tag += "USERS,";
		if((t & 0b000_0100) != 0)
			tag += "DEBUG,";
		if((t & 0b000_1000) != 0)
			tag += "VERBO,";
		if((t & 0b001_0000) != 0)
			tag += "VERB2,";
		if((t & 0b010_0000) != 0)
			tag += "PARSE,";
		if((t & 0b100_0000) != 0)
			tag += "PVERB,";
		if(tag.isEmpty())
			return;

		try{
			logFile.write("\n" + tag.substring(0, tag.length() - 1) + ": " + stringify(msg));
			if((t & 0b111_1110) == 0)
				logFile.flush();
		}catch(IOException e){}
	}
	public static String stringify(String seperator, Object[] objs){
		String msg = String.valueOf(objs[0]);
		for(int i = 1; i < objs.length; i++)
			msg += seperator + objs[i];
		return msg;
	}
	public static String stringify(Object[] objs){
		return stringify(" ", objs);
	}
}
