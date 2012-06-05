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
	private LinkedHashMap<String, String> mCandidates;
	private static StringBuilder mCurrentChar;
	private static MorseChart mMorseChart;
	
	
	
	public TeclaMorse(Context context) {
		mMorseChart = new MorseChart(context, this);
		mCurrentChar = new StringBuilder();
		mMorseDictionary = new MorseDictionary();
		mCandidates = new LinkedHashMap<String,String>();
		MorseDictionary.createMapping(mCandidates);
	}
	
	public void clearCharInProgress() {
		mCurrentChar.setLength(0);
	}

	
	private void updateCandidates() {
		Iterator<String> it = mCandidates.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!key.startsWith(mCurrentChar.toString()))
				it.remove();
		}
	}
	
	public String getCurrentChar() {
		return mCurrentChar.toString();
	}
	
	public String morseToChar(String morseChar) {
		return getMorseDictionary().getKey(morseChar);
	}
	
	public MorseDictionary getMorseDictionary() {
		return mMorseDictionary;
	}
	
	public HashMap<String,String> getCandidates() {
		return mCandidates;
	}
	
	public MorseChart getMorseChart() {
		mMorseChart.update();
		return mMorseChart;
	}
	
	
	public void addDit() {
		mCurrentChar.append("•");
		updateCandidates();
	}
	
	public void addDah() {
		mCurrentChar.append("-");
		updateCandidates();
	}
	
	public void reset() {
		mCurrentChar = new StringBuilder();
		mCandidates.clear();
		MorseDictionary.createMapping(mCandidates);
	}
	
	
	
	
}