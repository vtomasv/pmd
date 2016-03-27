package org.mdp.wc.naive.server;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mdp.dir.util.User;
import org.mdp.wc.util.ConcurrentCountMap;

/**
 * An RMI server implementation for doing a simple string count. Assumes everything
 * goes smoothly with no faults.
 * 
 * @author ahogan
 *
 */
public class NaiveStringCountServer implements NaiveStringCountStub {

	/**
	 * 
	 */
	private static final long serialVersionUID = 196351947854887245L;

	
	// contains the count of strings
	private final ConcurrentCountMap<String> countStrings;
	
	// remembers which peers have sent all their data
	private final Set<User> finalised;
	
	// total n-grams processed here
	private volatile int totalNonUnique = 0;

	public NaiveStringCountServer(){
		countStrings = new ConcurrentCountMap<String>();		
		finalised = Collections.newSetFromMap(new ConcurrentHashMap<User,Boolean>());
	}

	@Override
	public synchronized int countAndIndexStrings(Collection<String> strings) throws RemoteException {
		totalNonUnique += strings.size();
		
		int before = countStrings.size();
		for(String s:strings){
			countStrings.add(s);
		}
		int unique = countStrings.size() - before;
		System.out.println("\n[SERVER:] Counted non-unique "+strings.size()+" and found "+unique+" new unique n-grams.");
		return countStrings.size() - before;
	}

	@Override
	public synchronized ConcurrentCountMap<String> getCounts() throws RemoteException {
		System.out.println("\n[SERVER:] Returning all count results.");
		return countStrings;
	}

	@Override
	public synchronized ConcurrentCountMap<String> getTopKCounts(int k) throws RemoteException {
		System.out.println("\n[SERVER:] Returning top-"+k+" count results.");
		return countStrings.getTopKMap(k);
	}

	@Override
	public boolean test(User u) throws RemoteException {
		System.out.println("\n[SERVER:] User is testing connecting to you: " + u);
		return true;
	}
	
	public synchronized boolean finalise(User u) throws RemoteException {
		System.out.println("\n[SERVER:] User is notifying that they've sent you all data: " + u);
		finalised.add(u);
		return true;
	}
	
	public int getNonUniqueStringsCounted(){
		return totalNonUnique;
	}
	
	public synchronized Set<User> finalised(){
		return finalised;
	}
}
