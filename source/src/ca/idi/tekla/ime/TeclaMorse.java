package ca.idi.tekla.ime;

import java.util.AbstractMap.;

public class TeclaMorse{
	
	
	private Hashmap<String,String> mMorseChart;
	private static StringBuffer mCurrentLetter;
	
	
	public TeclaMorse(){
		mCurrentLetter = new StringBuffer();
		mMorseChart = new Hashmap<String,String>();
		createMapping();
	}
	
	public void createMapping(){
		//dit-first letters
		mMorseChart.put("0", 'e'); mMorseChart.put("00", 'i'); mMorseChart.put("01", 'a');
		mMorseChart.put("000", 's'); mMorseChart.put("001", 'u'); mMorseChart.put("011", 'w');
		mMorseChart.put("010", 'r'); mMorseChart.put("0000", 'h'); mMorseChart.put("0001", 'v');
		mMorseChart.put("0010", 'f'); mMorseChart.put("0110", 'p'); mMorseChart.put("0111", 'j');
		mMorseChart.put("0100", 'l'); 
		
		//dah-first letters
		mMorseChart.put("1", 't'); mMorseChart.put("11", 'm'); mMorseChart.put("10", 'n');
		mMorseChart.put("111", 'o'); mMorseChart.put("110", 'g'); mMorseChart.put("100", 'd');
		mMorseChart.put("101", 'k'); mMorseChart.put("1100", 'z'); mMorseChart.put("1101", 'q');
		mMorseChart.put("1000", 'b'); mMorseChart.put("1001", 'x'); mMorseChart.put("1010", 'c');
		mMorseChart.put("1011", 'y');
		
		//numbers
		
		//special characters
	}
	
	public void addDit(){
		mCurrentLetter.append("0");
	}
	
	public void addDah(){
		mCurrentLetter.append("1");
	}
	
	public String letterReturn(){
		String letter = mMorseChart.get(mCurrentLetter).toString();
		if(letter != null){
			mCurrentLetter = "";
			return letter;
		}
		throw new Exception("No such symbol found");
	}

	
	
}