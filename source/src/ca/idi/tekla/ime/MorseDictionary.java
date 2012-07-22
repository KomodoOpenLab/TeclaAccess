package ca.idi.tekla.ime;

import java.util.*;
import java.util.Map.Entry;

public class MorseDictionary {
	
	public static LinkedHashMap<String,String> mMorseMap;
	public static LinkedHashMap<String,String> mDitFirst;
	public static LinkedHashMap<String,String> mDahFirst;
	
	private static int mMaxCodeLength;
	
	public MorseDictionary() {
		mMorseMap = new LinkedHashMap<String,String>();
		createMapping(mMorseMap);
		mDitFirst = initCharts('•');
		mDahFirst = initCharts('-');
		setMaxCodeLength();
	}
	
	/**
	 * Initializes the contents of a Morse map
	 * @param map
	 */
	public static void createMapping(LinkedHashMap<String,String> map) {
		//Alphabetic order
		map.put("•-", "a"); map.put("-•••", "b"); map.put("-•-•", "c");
		map.put("-••", "d"); map.put("•", "e"); map.put("••-•", "f");
		map.put("--•", "g"); map.put("••••", "h"); map.put("••", "i");
		map.put("•---", "j"); map.put("-•-", "k"); map.put("•-••", "l");
		map.put("--", "m"); map.put("-•", "n"); map.put("---", "o");
		map.put("•--•", "p"); map.put("--•-", "q"); map.put("•-•", "r");
		map.put("•••", "s"); map.put("-", "t"); map.put("••-", "u");
		map.put("•••-", "v"); map.put("•--", "w");	map.put("-••-", "x");
		map.put("-•--", "y"); map.put("--••", "z"); 
		
		//Numbers
		map.put("•----", "1"); map.put("••---", "2"); map.put("•••--", "3");
		map.put("••••-", "4"); map.put("•••••", "5"); map.put("-••••", "6");
		map.put("--•••", "7"); map.put("---••", "8"); map.put("----•", "9");
		map.put("-----", "0");
		
		//Special characters
		map.put("•-•-•-", "."); map.put("--••--", ","); map.put("•-••--", "!");
		map.put("-•-•-•", ":"); map.put("•••-•", ";"); map.put("•---•", "@");
		map.put("-•---", "#"); map.put("-•••-•", "$"); map.put("•--•-•", "%");
		map.put("-••--", "&"); map.put("•-•••", "*"); map.put("•--••", "+");
		map.put("---•", "_"); map.put("•--•-", "="); map.put("--••-", "/");
		map.put("-•••••", "\\"); map.put("•-•--•", "\'"); map.put("--•--", "\"");
		map.put("•••--•", "("); map.put("-••--•", ")"); map.put("••--••", "?");
		map.put("--••-•", ">"); map.put("-•--•-", "<"); map.put("-••••-", "-");
		
		//Command keys
		map.put("-•-••", "✓"); //Done (minimizes keyboard)
		map.put("••--", "space"); //Space ⎵
		map.put("•-•-", "↵"); //Enter
		map.put("••--•", "⇪"); //Toggle caps lock
		map.put("----", "DEL"); //Delete
		map.put("-•••-", "↶"); //Back key
		map.put("•-•-•", "\\n"); //New line
	}
	
	/**
	 * Sets the maximum code length of the longest Morse sequence
	 */
	public void setMaxCodeLength(){
		int max = 0;
		for (String key : mMorseMap.keySet()) {
			if (key.length() > max) {
				max = key.length();
			}
		}
		mMaxCodeLength = max;
	}
	
	/**
	 * Returns the maximum code length of the longest Morse sequence
	 * @return
	 */
	public int getMaxCodeLength(){
		return mMaxCodeLength;
	}
	
	/**
	 * Returns the character corresponding to the Morse sequence
	 * @param key the Morse sequence to convert into character
	 * @return
	 */
	public String getKey(String key) {
		return mMorseMap.get(key);
	}
	
	/**
	 * Used to retrieve one of the dit/dah charts, based on
	 * the first signal
	 * @param s
	 * @return
	 */
	public LinkedHashMap<String,String> getChartStartsWith(String s) {
		return s.equals("•") ? mDitFirst : mDahFirst;
	}
	
	/**
	 * Initializes a chart based on the first signal
	 * @param c represents either a dit or a dah
	 * @return
	 */
	public LinkedHashMap<String,String> initCharts(char c) {
		LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		Iterator<Entry<String,String>> it = mMorseMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> entry = it.next();
			if (entry.getKey().startsWith(Character.toString(c)))
				map.put(entry.getKey(), entry.getValue());
		}
		
		return map;
	}

	public LinkedHashMap<String, String> getCommandsSet() {
		LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		map.put("-•-••", "✓"); //Done (minimizes keyboard)
		map.put("•-•-", "↵"); //Enter
		map.put("••--", "space"); //Space
		map.put("••--•", "⇪"); //Toggle caps lock
		map.put("----", "DEL"); //Delete
		map.put("-•••-", "↶"); //Back key
		map.put("•-•-•", "\\n"); //New line
		return map;
	}
	
	
}