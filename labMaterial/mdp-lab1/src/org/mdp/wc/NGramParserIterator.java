package org.mdp.wc;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Parses words from a file into n-grams: n consecutive words
 * separated by a space.
 * 
 * Note: doesn't see/respect line-breaks.
 * 
 * @author Aidan
 *
 */
public class NGramParserIterator implements Iterator<String> {
	final WordParserIterator wpi;
	ArrayList<String> ngram = null;
	final int n;
	
	
	public NGramParserIterator(BufferedReader br, int n){
		if(n<1){
			throw new IllegalArgumentException("n must be greater than 0");
		}
		
		wpi = new WordParserIterator(br);
		this.n = n;
		loadNext();
	}
	
	private void loadNext(){
		if(ngram == null){
			ngram = new ArrayList<String>();
		} else if(ngram.size() == n){
			ngram.remove(0);
		}
		
		while(ngram.size()<n){
			if(!wpi.hasNext()){
				ngram = null;
				break;
			}
			
			ngram.add(wpi.next());
		}
	}

	@Override
	public boolean hasNext() {
		return ngram!=null;
	}

	@Override
	public String next() {
		if(!hasNext())
			throw new NoSuchElementException();
		String next = ngram.get(0);
		
		for(int i=1; i<ngram.size(); i++){
			next += " "+ngram.get(i);
		}
		
		loadNext();
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
