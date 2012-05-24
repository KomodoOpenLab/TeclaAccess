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
	private static StringBuffer mCurrentLetter;
	
	
	public TeclaMorse(Context context) {
		Resources res = context.getResources();
		//String s = res.getString(R.xml.kbd_qwerty);
		//Log.d(TeclApp.TAG, CLASS_TAG + "Retrieved from XML: " + s);
		
		mCurrentLetter = new StringBuffer();
		mMorseDictionary = new MorseDictionary();
		mCandidates = new HashMap<String,String>();
		MorseDictionary.createMapping(mCandidates);
	}
	

	
	public void updateCandidates() {
		Iterator<String> it = mCandidates.keySet().iterator();
		while (it.hasNext()){
			String key = it.next();
			if (!key.startsWith(mCurrentLetter.toString()))
				it.remove();
		}
	}
	
	public String getCurrentLetter() {
		return mCurrentLetter.toString();
	}
	
	
	public void addDit() {
		mCurrentLetter.append("0");
		updateCandidates();
	}
	
	public void addDah() {
		mCurrentLetter.append("1");
		updateCandidates();
	}
	
	public int letterReturn() {
		String letter = mMorseDictionary.getKey(mCurrentLetter.toString());
		if (letter != null) {
			mCurrentLetter = new StringBuffer();
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