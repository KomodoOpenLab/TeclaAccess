package ca.idi.tekla.ime;

import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.*;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import ca.idi.tekla.ime.MorseDictionary;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class TeclaMorse {
	
	private String CLASS_TAG = "TeclaMorse: ";
	
	private MorseDictionary mMorseDictionary;
	private HashMap<String, String> mCandidates;
	private static StringBuilder mCurrentChar;
	
	
	public TeclaMorse() {
		//Resources res = context.getResources();
		//String s = res.getString(R.xml.kbd_qwerty);
		//Log.d(TeclApp.TAG, CLASS_TAG + "Retrieved from XML: " + s);
		
		mCurrentChar = new StringBuilder();
		mMorseDictionary = new MorseDictionary();
		mCandidates = new HashMap<String,String>();
		MorseDictionary.createMapping(mCandidates);
	}
	
	public void clearCharInProgress(){
		mCurrentChar.setLength(0);
	}

	
	public void updateCandidates() {
		Iterator<String> it = mCandidates.keySet().iterator();
		while (it.hasNext()){
			String key = it.next();
			if (!key.startsWith(mCurrentChar.toString()))
				it.remove();
		}
	}
	
	public String getCurrentChar() {
		return mCurrentChar.toString();
	}
	
	public MorseDictionary getMorseDictionary(){
		return mMorseDictionary;
	}
	
	
	public void addDit() {
		mCurrentChar.append("•");
		updateCandidates();
	}
	
	public void addDah() {
		mCurrentChar.append("-");
		updateCandidates();
	}
	
	public int letterReturn() {
		String letter = mMorseDictionary.getKey(mCurrentChar.toString());
		if (letter != null) {
			mCurrentChar = new StringBuilder();
			mCandidates.clear();
			MorseDictionary.createMapping(mCandidates);
			return getKeycodeFromString(letter);
		}
		//throw new Exception("No such symbol found");
		return getKeycodeFromString("");
	}
	
	public int getKeycodeFromString(String s) {
		if (s.equals(""))
			return 0;
		
		return 113;
	}
	
	
	
}