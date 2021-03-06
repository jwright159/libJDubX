package com.github.jwright159.gdx;

import java.io.*;
import com.badlogic.gdx.files.*;
import com.badlogic.gdx.*;

public abstract class Log{
	private static Writer logFile;
	//static final byte verbosity = 0b010_1111;//error=000_0001, user=000_0010, debug=000_0100, ui=000_1000, graphics=001_0000, audio=010_0000, gameplay=100_0000 (only 7 bits bc signed)
	private static byte verbosity;
	
	public static void setVerbosity(String v){
		setVerbosity(Byte.parseByte(v, 2));
	}
	public static void setVerbosity(byte v){
		verbosity = v;
	}
	public static byte getVerbosity(){
		return verbosity;
	}
	public static void setLogFile(FileHandle file){
		if(logFile != null){
			debug("Switching log file to", file.path());
			try{
				logFile.close();
			}catch(IOException e){}
			logFile = null;
		}
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
	public static void ui(Object... msg){
		log((byte)0b000_1000, msg);
	}
	public static void graphics(Object... msg){
		log((byte)0b001_0000, msg);
	}
	public static void audio(Object... msg){
		log((byte)0b010_0000, msg);
	}
	public static void gameplay(Object... msg){
		log((byte)0b100_0000, msg);
	}
	public static void log(byte type, Object... msg){
		if(verbosity < 0)
			return;
		if(logFile == null)
			if((type & 0b000_0001) != 0)//if its an error
				setLogFile(Gdx.files.local("error.log"));
			else
				throw new NullPointerException("Attempt to write to log when log writer is null.");
		if(verbosity == 0 && (type & 0b000_0001) == 0)//if verbo not set and its not an error
			throw new NullPointerException("Attempt to write to log when verbosity is 0. Use negative verbosity to not write to log.");
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
			tag += "USINT,";
		if((t & 0b001_0000) != 0)
			tag += "GRAPH,";
		if((t & 0b010_0000) != 0)
			tag += "AUDIO,";
		if((t & 0b100_0000) != 0)
			tag += "GMPLY,";
		if(tag.isEmpty())
			return;

		try{
			logFile.write("\n" + tag.substring(0, tag.length() - 1) + ": " + stringify(msg));
			if((t & 0b000_1111) != 0)
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
