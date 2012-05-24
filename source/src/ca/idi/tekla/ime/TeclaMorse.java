package ca.idi.tekla.ime;

import java.util.*;

public class TeclaMorse{
	
	
	private HashMap<String,String> mMorseChart;
	private HashMap<String, String> mCandidates;
	private static StringBuffer mCurrentLetter;
	
	
	public TeclaMorse(){
		mCurrentLetter = new StringBuffer();
		mMorseChart = new HashMap<String,String>();
		mCandidates = new HashMap<String,String>();
		createMapping(mMorseChart);
		createMapping(mCandidates);
	}
	
	public void createMapping(HashMap<String,String> map){
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
		
		//special characters
	}
	
	public void updateCandidates(){
		for(String key : mCandidates.keySet()){
			if(!key.startsWith(mCurrentLetter.toString()))
				mCandidates.remove(key);
		}
	}
	
	public String getCurrentLetter(){
		return mCurrentLetter.toString();
	}
	
	
	public void addDit(){
		mCurrentLetter.append("0");
		updateCandidates();
	}
	
	public void addDah(){
		mCurrentLetter.append("1");
		updateCandidates();
	}
	
	public String letterReturn() throws Exception{
		String letter = mMorseChart.get(mCurrentLetter.toString());
		if(letter != null){
			mCurrentLetter = new StringBuffer();
			mCandidates.clear();
			createMapping(mCandidates);
			return letter;
		}
		//throw new Exception("No such symbol found");
		return "";
	}
	
	
	
}