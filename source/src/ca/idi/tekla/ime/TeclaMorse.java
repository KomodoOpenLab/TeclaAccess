package ca.idi.tekla.ime;

import java.util.*;

import ca.idi.tekla.ime.MorseDictionary;

import android.content.Context;
import android.util.DisplayMetrics;

public class TeclaMorse {
	
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
	
	/**
	 * Resets the current char sequence
	 */
	public void clearCharInProgress() {
		mCurrentChar.setLength(0);
		//mCandidates.clear();
		//MorseDictionary.createMapping(mCandidates);
	}

	/**
	 * Updates the set of possible candidates, based
	 * on the current char sequence
	 */
	private void updateCandidates() {
		Iterator<String> it = mCandidates.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!key.startsWith(mCurrentChar.toString()))
				it.remove();
		}
	}
	
	/**
	 * Returns the current char sequence
	 * @return
	 */
	public String getCurrentChar() {
		return mCurrentChar.toString();
	}
	
	/**
	 * Converts a Morse sequence to its corresponding character
	 * @param morseChar the Morse sequence to convert
	 * @return the corresponding character
	 */
	public String morseToChar(String morseChar) {
		return getMorseDictionary().getKey(morseChar);
	}
	
	/**
	 * Returns the used Morse dictionary
	 * @return
	 */
	public MorseDictionary getMorseDictionary() {
		return mMorseDictionary;
	}
	
	/**
	 * Returns the set of the current candidates
	 * @return
	 */
	public HashMap<String,String> getCandidates() {
		return mCandidates;
	}
	
	/**
	 * Returns the Morse chart used for the HUD display
	 * @return
	 */
	public MorseChart getMorseChart() {
		return mMorseChart;
	}
	
	/**
	 * Add a dit to the current char sequence
	 */
	public void addDit() {
		mCurrentChar.append("•");
		//updateCandidates();
	}
	
	/**
	 * Add a dah to the current char sequence
	 */
	public void addDah() {
		mCurrentChar.append("-");
		//updateCandidates();
	}
	
}