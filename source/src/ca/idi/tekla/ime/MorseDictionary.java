package ca.idi.tekla.ime;

import java.util.*;
import java.util.Map.Entry;

public class MorseDictionary {
	
	private String CLASS_TAG = "MorseDictionary: ";
	public static LinkedHashMap<String,String> mMorseChart;
	private static int mMaxCodeLength;
	
	public MorseDictionary() {
		mMorseChart = new LinkedHashMap<String,String>();
		createMapping(mMorseChart);
		setMaxCodeLength();
	}
	
	public static void createMapping(LinkedHashMap<String,String> map) {
		//alphabetic order
		map.put("•-", "a"); map.put("-•••", "b"); map.put("-•-•", "c");
		map.put("-••", "d"); map.put("•", "e"); map.put("••-•", "f");
		map.put("--•", "g"); map.put("••••", "h"); map.put("••", "i");
		map.put("•---", "j"); map.put("-•-", "k"); map.put("•-••", "l");
		map.put("--", "m"); map.put("-•", "n"); map.put("---", "o");
		map.put("•--•", "p"); map.put("--•-", "q"); map.put("•-•", "r");
		map.put("•••", "s"); map.put("-", "t"); map.put("••-", "u");
		map.put("•••-", "v"); map.put("•--", "w");	map.put("-••-", "x");
		map.put("-•--", "y"); map.put("--••", "z"); 
		
		//dit-first letters
		/*map.put("•", "e"); map.put("••", "i"); map.put("•-", "a");
		map.put("•••", "s"); map.put("••-", "u"); map.put("•--", "w");
		map.put("•-•", "r"); map.put("••••", "h"); map.put("•••-", "v");
		map.put("••-•", "f"); map.put("•--•", "p"); map.put("•---", "j");
		map.put("•-••", "l"); */
		
		//dah-first letters
		/*map.put("-", "t"); map.put("--", "m"); map.put("-•", "n");
		map.put("---", "o"); map.put("--•", "g"); map.put("-••", "d");
		map.put("-•-", "k"); map.put("--••", "z"); map.put("--•-", "q");
		map.put("-•••", "b"); map.put("-••-", "x"); map.put("-•-•", "c");
		map.put("-•--", "y");*/
		
		//numbers
		map.put("•----", "1"); map.put("••---", "2"); map.put("•••--", "3");
		map.put("••••-", "4"); map.put("•••••", "5"); map.put("-••••", "6");
		map.put("--•••", "7"); map.put("---••", "8"); map.put("----•", "9");
		map.put("-----", "0");
		
		//special characters
		map.put("•-•-•-", "."); map.put("--••--", ","); map.put("•-••--", "!");
		map.put("-•-•-•", ":"); map.put("•••-•", ";"); map.put("•---•", "@");
		map.put("-•---", "#"); map.put("-•••-•", "$"); map.put("•--•-•", "%");
		map.put("-••--", "&"); map.put("•-•••", "*"); map.put("•--••", "+");
		map.put("---•", "_"); map.put("•--•-", "="); map.put("--••-", "/");
		map.put("-•••••", "\\"); map.put("•-•--•", "\'"); map.put("--•--", "\"");
		map.put("•••--•", "("); map.put("-••--•", ")"); map.put("--••-•", ">");
		//map.put("-•--•-", "<");
		map.put("-••••-", "-");
		map.put("••--••", "?");
		
		//function keys
		map.put("•-•-", "↵"); //Enter
		map.put("-••-•", "DEL"); //Delete
		
	}
	
	public void setMaxCodeLength(){
		int max = 0;
		for (String key : mMorseChart.keySet()) {
			if (key.length() > max) {
				max = key.length();
			}
		}
		mMaxCodeLength = max;
	}
	
	public int getMaxCodeLength(){
		return mMaxCodeLength;
	}
	
	public String getKey(String key) {
		return mMorseChart.get(key);
	}
	
	public LinkedHashMap<String,String> startsWith(char c) {
		LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		Iterator<Entry<String,String>> it = mMorseChart.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> entry = it.next();
			if (entry.getKey().startsWith(Character.toString(c)))
				map.put(entry.getKey(), entry.getValue());
		}
		
		return map;
	}
	
	
}