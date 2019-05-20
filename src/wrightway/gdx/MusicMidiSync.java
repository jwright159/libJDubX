package wrightway.gdx;

import com.badlogic.gdx.files.*;
import com.leff.midi.*;
import com.badlogic.gdx.audio.*;
import java.io.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.leff.midi.util.*;
import com.leff.midi.event.*;
import wrightway.gdx.*;

public class MusicMidiSync implements Disposable{
	private MidiFile midi;
	private Music music;
	private MidiProcessor midipro;
	private float pMs;
	
	public MusicMidiSync(Music music, MidiFile midi, MidiEventListener noteOn, MidiEventListener noteOff){
		this.music = music;
		this.midi = midi;
		midipro = new MidiProcessor(midi);
		if(noteOn != null)
			midipro.registerEventListener(noteOn, NoteOn.class);
		if(noteOff != null)
			midipro.registerEventListener(noteOff, NoteOff.class);
	}
	public MusicMidiSync(Music music, MidiFile midi, MidiEventListener noteOn){
		this(music, midi, noteOn, null);
	}
	
	public void play(){
		music.play();
		midipro.start();
	}
	
	public void stop(){
		music.stop();
		midipro.stop();
		midipro.reset();
	}
	
	public void pause(){
		music.pause();
		midipro.stop();
	}
	
	public void setPosition(float pos){
		music.setPosition(pos);
	}
	public float getPosition(){
		return music.getPosition();
	}
	
	public void setVolume(float vol){
		music.setVolume(vol);
	}
	public float getVolume(){
		return music.getVolume();
	}
	
	public boolean isPlaying(){
		return music.isPlaying();
	}

	public void sync(){
		float ms = music.getPosition();
		if(ms < pMs){
			midipro.reset();
			midipro.start();
		}
		midipro.setMsElapsed((long)(ms*1000));
		pMs = ms;
	}
	
	@Override
	public void dispose(){
		stop();
		midipro.unregisterAllEventListeners();
		music.dispose();
	}
}
