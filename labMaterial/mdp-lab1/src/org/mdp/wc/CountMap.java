package org.mdp.wc;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Counts things. In a map. With integers.
 * @author Aidan
 */
public class CountMap<T extends Comparable<? super T>> extends HashMap<T, Integer> {

	
	private static final long serialVersionUID = -8495375842744348018L;
	long _total=0;
	
	/**
	 * 
	 * @return all non-unique items added
	 */
	public long getTotal() {
		return _total;
	}
	
	public CountMap() {
		super();
	}
	
	/**
	 * Add value to the count of id
	 * @param id
	 * @param value
	 * @return
	 */
	public int add(T id, int value) {
		Integer i = get(id);
		if(i == null) {
			i = Integer.valueOf(0);
		}
		i+=value;
		_total+=value;
		put(id,i);
		return i;
	}
	
	/**
	 * Add one to the count of id
	 * @param id
	 * @param value
	 * @return
	 */
	public int add(T id) {
		Integer i = get(id);
		if(i == null) {
			i = Integer.valueOf(0);
		}
		i++;
		_total++;
		put(id,i);
		return i;
	}
	
	/**
	 * Summate the map into this one
	 * @param all
	 */
	public void addAll(Map<T, Integer> all) {
		for(Map.Entry<T, Integer> pair:all.entrySet()){
			if(pair.getKey()!=null && pair.getValue()!=null){
				add(pair.getKey(), pair.getValue());
			}
		}
	}
	
	public void printStats() {
		printStats(System.out);
	}
	
	public void printStats(PrintStream out) {
		Iterator<Map.Entry<T, Integer>> it = this.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry<T, Integer> e = it.next();
		
			out.println(e.getKey() + "\t" + e.getValue());
		}
		out.flush();
	}
	
	public void printStats(Logger log, Level l) {
		Iterator<Map.Entry<T, Integer>> it = this.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry<T, Integer> e = it.next();
		
			log.log(l, e.getKey() + "\t" + e.getValue());
		}
	}
	
	public void printOrderedStats() {
		printOrderedStats(Integer.MAX_VALUE, System.out);
	}
	
	public void printOrderedStats(PrintStream out) {
		printOrderedStats(Integer.MAX_VALUE, out);
	}
	
	public TreeSet<Map.Entry<T, Integer>> getKeyOrderedEntries(){
		Iterator<Map.Entry<T, Integer>> it = this.entrySet().iterator();
		TreeSet<Map.Entry<T, Integer>> ts = new TreeSet<Map.Entry<T, Integer>>(new KeyComparator<T>());
		
		while(it.hasNext()) {
			ts.add(it.next());
		}
		
		return ts;
	}
	
	/**
	 * Return a new map with only the top k elements from 
	 * this map.
	 * 
	 * @param k
	 * @return
	 */
	public CountMap<T> getTopKMap(int k){
		CountMap<T> topK = new CountMap<T>();
		Iterator<Map.Entry<T, Integer>> it = getOccurrenceOrderedEntries().iterator();
		
		for(int i=0; i<k && it.hasNext(); i++){
			Map.Entry<T,Integer> e = it.next();
			topK.add(e.getKey(), e.getValue());
		}
		return topK;
	}
	
	/**
	 * 
	 * @return entries ordered by descending occurrence
	 */
	public TreeSet<Map.Entry<T, Integer>> getOccurrenceOrderedEntries(){
		return getOccurrenceOrderedEntries(false);
	}
	
	public TreeSet<Map.Entry<T, Integer>> getOccurrenceOrderedEntries(boolean ascending){
		Iterator<Map.Entry<T, Integer>> it = this.entrySet().iterator();
		TreeSet<Map.Entry<T, Integer>> ts = new TreeSet<Map.Entry<T, Integer>>(new OccurrenceComparator<T>(ascending));
		
		while(it.hasNext()) {
			ts.add(it.next());
		}
		
		return ts;
	}
	
	public void printOrderedStats(int topK) {
		printOrderedStats(topK, System.out);
	}
	
	public void printOrderedStats(int topK, PrintStream out) {
		Iterator<Map.Entry<T, Integer>> it = getOccurrenceOrderedEntries().iterator();
		
		int i = 0;
		while(it.hasNext() && i<topK) {
			i++;
			Map.Entry<T, Integer> e = it.next();
		
			out.println(e.getKey() + "\t" + e.getValue());
		}
		out.flush();
	}
	
	public void printOrderedStats(Logger log, Level l) {
		printOrderedStats(Integer.MAX_VALUE, log, l);
	}
	
	public void printOrderedStats(int topK, Logger log, Level l) {
		Iterator<Map.Entry<T, Integer>> it = getOccurrenceOrderedEntries().iterator();
		
		int i = 0;
		while(it.hasNext() && i<topK) {
			i++;
			Map.Entry<T, Integer> e = it.next();
		
			log.log(l, e.getKey() + "\t" + e.getValue());
		}
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		Iterator<Map.Entry<T, Integer>> it = this.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry<T, Integer> e = it.next();
		
			s.append(e.getKey() + "\t" + e.getValue()+"\n");
		}
		return s.toString();
	}
	
	public static class OccurrenceComparator<T extends Comparable<? super T>> implements Comparator<Map.Entry<T, Integer>>, Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8794378599730605754L;
		
		boolean _ascending;
		
		public OccurrenceComparator(){
			this(false);
		}
		
		public OccurrenceComparator(boolean ascending){
			_ascending = ascending;
		}

		public int compare(Map.Entry<T, Integer> m1, Map.Entry<T, Integer> m2){
			int count1 = m1.getValue().intValue();
			int count2 = m2.getValue().intValue();
			
			int diff;
			if(_ascending){
				diff = count1 - count2;
			} else{
				diff = count2 - count1;
			}
			if(diff != 0)
				return diff;
			else{
				T n1  = m1.getKey();
				T n2 = m2.getKey();
				return n1.compareTo(n2);
			}
		}
	}
	
	public static class KeyComparator<T extends Comparable<? super T>> implements Comparator<Map.Entry<T, Integer>>, Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1229701058223158391L;

		public int compare(Map.Entry<T, Integer> m1, Map.Entry<T, Integer> m2){
			T n1  = m1.getKey();
			T n2 = m2.getKey();
			int diff = n1.compareTo(n2);
			
			
			if(diff != 0)
				return diff;
			else{
				int count1 = m1.getValue().intValue();
				int count2 = m2.getValue().intValue();
				return count2 - count1;
			}
		}
	}
	
	public void clear(){
		super.clear();
		_total = 0;
	}
}
