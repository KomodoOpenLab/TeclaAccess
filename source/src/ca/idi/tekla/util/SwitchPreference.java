package ca.idi.tekla.util;

import java.util.HashMap;

import ca.idi.tekla.TeclaApp;

import android.preference.ListPreference;
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
	
	public void onPreferenceChanged(String key) {
		HashMap<String,String[]> mSwitchMap = TeclaApp.persistence.getSwitchMap();
		mSwitchMap.remove(key.substring(0, key.lastIndexOf("_")));
		mSwitchMap.put(key.substring(0, key.lastIndexOf("_")), new String[]{this.tecla.getValue(), this.morse.getValue()});
		this.refreshSummaries();
	}

	public void setDefaultValues(int a, int b) {
		this.tecla.setValueIndex(a);
		this.morse.setValueIndex(b);
	}

	public static void addToMap(SwitchPreference switchPref) {
		HashMap<String,String[]> mSwitchMap = TeclaApp.persistence.getSwitchMap();
		mSwitchMap.put(switchPref.prefScreen.getKey(), new String[]{switchPref.tecla.getValue(), switchPref.morse.getValue()});
	}

	public void refreshSummaries() {
		this.tecla.setSummary(this.tecla.getEntry());
		this.morse.setSummary(this.morse.getEntry());
		this.prefScreen.setSummary(this.tecla.getEntry() + " / " + this.morse.getEntry());
	}
	

}
