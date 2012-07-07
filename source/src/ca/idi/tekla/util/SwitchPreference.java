package ca.idi.tekla.util;

import java.util.HashMap;

import ca.idi.tekla.TeclaApp;
import ca.idi.tecla.lib.ListPreference;

import android.preference.PreferenceScreen;

public class SwitchPreference {
	
	public PreferenceScreen prefScreen;
	public ListPreference tecla;
	public ListPreference morse;
	
	
	public SwitchPreference(PreferenceScreen ps, ListPreference teclaPref, ListPreference morsePref) {
		prefScreen = ps;
		tecla = teclaPref;
		morse = morsePref;
	}
	
	/**
	 * Updates the action mapping of the preference
	 * @param key
	 */
	public void onPreferenceChanged(String key) {
		HashMap<String,String[]> mSwitchMap = TeclaApp.persistence.getSwitchMap();
		mSwitchMap.remove(key.substring(0, key.lastIndexOf("_")));
		mSwitchMap.put(key.substring(0, key.lastIndexOf("_")), new String[]{this.tecla.getValue(), ""/*this.morse.getValue()*/}); // FIXME: Uncomment when adding morse

		this.refreshSummaries();
	}

	/**
	 * Resets the values of a switch preference
	 * @param a index of the Tecla action pref
	 * @param b index of the Morse action pref
	 */
	public void setDefaultValues(int a, int b) {
		this.tecla.setValueIndex(a);
//		this.morse.setValueIndex(b); // FIXME: Uncomment when adding morse
	}

	/**
	 * Creates a mapping of a switch and its actions,
	 * and store it in the map
	 * @param switchPref
	 */
	public static void addToMap(SwitchPreference switchPref) {
		HashMap<String,String[]> mSwitchMap = TeclaApp.persistence.getSwitchMap();
		mSwitchMap.put(switchPref.prefScreen.getKey(), new String[]{switchPref.tecla.getValue(), ""/*switchPref.morse.getValue()*/}); // FIXME: Uncomment when adding morse
	}

	/**
	 * Updates the states of the pref summaries
	 */
	public void refreshSummaries() {
		this.tecla.setSummary(this.tecla.getEntry());
//		this.morse.setSummary(this.morse.getEntry()); // FIXME: Uncomment when adding morse
		this.prefScreen.setSummary(this.tecla.getEntry() /*+ " / " + this.morse.getEntry()*/); // FIXME: Uncomment when adding morse
	}
	

}
