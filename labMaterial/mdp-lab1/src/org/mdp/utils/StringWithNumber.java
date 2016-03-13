package org.mdp.utils;

import org.mdp.utils.StringWithNumber;

/**
 * A utility class to store a string and a number.
 * 
 * @author Aidan
 *
 */
public class StringWithNumber implements Comparable<StringWithNumber> {
	public static int NUMBER_OF_DIGITS = Integer.toString(Integer.MAX_VALUE).length();
	
	private String string;
	private int number;
	
	public StringWithNumber(String str, int num){
		this.string = str;
		this.number = num;
	}
	
	@Override
	public int compareTo(StringWithNumber swm) {
		int comp = string.compareTo(swm.string);
		
		if(comp!=0)
			return comp;
		
		return number - swm.number;
	}
	
	public boolean equals(Object o){
		if(o==null) return false;
		
		if(o==this) return true;
		
		if(!(o instanceof StringWithNumber)) return false;
		
		StringWithNumber swn = (StringWithNumber)o;
		
		return string.equals(swn.string) && number == swn.number;
	}
	
	public int hashCode(){
		return string.hashCode() + number;
	}
	
	public String getString() {
		return string;
	}

	public int getNumber() {
		return number;
	}
	
	/**
	 * Will return a sortable string for the number
	 * by prepending zeros. For example, 4 returns 
	 * as 000000000004.
	 * 
	 * @param number
	 * @return sortable string for number 
	 */
	public static String getSortableNumber(int number){
		String numStr = Integer.toString(number);
		while(numStr.length()!=NUMBER_OF_DIGITS){
			numStr = "0"+numStr;
		}
		return numStr;
	}
}
