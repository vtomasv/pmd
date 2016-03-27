package org.mdp.wc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Parse words out of a file.
 * 
 * Uses simple/not-perfect regex.
 * 
 * Assumes there's line-breaks in there (loads a line at a time!).
 * 
 * @author Aidan
 *
 */
public class WordParserIterator implements Iterator<String> {
	private String next;
	private String[] tokens;
	private int currentToken;
	private BufferedReader br;
	
	private Exception e;
	
	public WordParserIterator(BufferedReader br){
		this.br = br;
		loadNext();
	}
	
	/**
	 * Get any exception thrown while reading from the 
	 * file.
	 * 
	 * @return
	 */
	public Exception getException(){
		return e;
	}
	
	/**
	 * Load the next word.
	 */
	private void loadNext(){
		next = null;
		if(tokens==null || currentToken==tokens.length){
			tokens = null;
			currentToken = 0;
			
			String line;
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				this.e = e;
				return;
			}
			
			if(line!=null){
				tokens = lowercaseAll(tokenizeWords(line));
				if(tokens.length==0)
					loadNext();
			}
		} 
		
		if(tokens!=null && currentToken<tokens.length){
			next = tokens[currentToken].trim();
			currentToken++;
		}
		
		if(next!=null && next.isEmpty()){
			loadNext();
		}
	}

	/**
	 * Check if there's another word.
	 */
	public boolean hasNext() {
		return next!=null;
	}

	/**
	 * Return the next word.
	 */
	public String next() {
		if(!hasNext()){
			throw new NoSuchElementException("No words left!");
		}
		
		String now = next;
		loadNext();
		return now;
	}

	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}
	
	public static String[] tokenizeWords(String line){
		return line.split("[^\\p{L}']+");
	}
	
	public static String[] lowercaseAll(String[] tokens){
		String[] lc = new String[tokens.length];
		for(int i=0; i<lc.length; i++)
			lc[i] = tokens[i].toLowerCase();
		return lc;
	}
}
