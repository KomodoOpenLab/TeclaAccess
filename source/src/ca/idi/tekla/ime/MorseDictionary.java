package ca.idi.tekla.ime;

import java.util.*;

public class MorseDictionary {
	
	private String CLASS_TAG = "MorseDictionary: ";
	private static HashMap<String,String> mMorseChart;
	
	public MorseDictionary() {
		mMorseChart = new HashMap<String,String>();
		createMapping(mMorseChart);
	}
	
	public static void createMapping(HashMap<String,String> map) {
		//dit-first letters
		map.put("0", "e"); map.put("00", "i"); map.put("01", "a");
		map.put("000", "s"); map.put("001", "u"); map.put("011", "w");
		map.put("010", "r"); map.put("0000", "h"); map.put("0001", "v");
		map.put("0010", "f"); map.put("0110", "p"); map.put("0111", "j");
		map.put("0100", "l"); 
		
		//dah-first letters
		map.put("1", "t"); map.put("11", "m"); map.put("10", "n");
		map.put("111", "o"); map.put("110", "g"); map.put("100", "d");
		map.put("101", "k"); map.put("1100", "z"); map.put("1101", "q");
		map.put("1000", "b"); map.put("1001", "x"); map.put("1010", "c");
		map.put("1011", "y");
		
		//numbers
		map.put("01111", "1"); map.put("00111", "2"); map.put("00011", "3");
		map.put("00001", "4"); map.put("00000", "5"); map.put("01111", "6");
		map.put("11000", "7"); map.put("11110", "8"); map.put("11110", "9");
		map.put("11111", "0");
		
		//special characters
	}
	
	public String getKey(String key) {
		return mMorseChart.get(key);
	}
	
}