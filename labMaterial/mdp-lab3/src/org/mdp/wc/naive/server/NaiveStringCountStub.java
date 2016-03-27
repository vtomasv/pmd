package org.mdp.wc.naive.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import org.mdp.dir.util.User;
import org.mdp.wc.util.ConcurrentCountMap;

/**
 * An RMI stub for doing a simple string count. Assumes everything
 * goes smoothly with no faults.
 * 
 * @author ahogan
 *
 */
public interface NaiveStringCountStub extends Remote, Serializable {
	/**
	 * Get the server to index counts for some strings.
	 * 
	 * @param strings - The raw strings to count
	 * 
	 * @return number of new unique strings added
	 * @throws RemoteException
	 */
	public int countAndIndexStrings(Collection<String> strings) throws RemoteException;
	
	/**
	 * Gets the counts of strings.
	 * 
	 * @return the counts, or null if not found
	 * @throws RemoteException
	 */
	public ConcurrentCountMap<String> getCounts()  throws RemoteException;
	
	/**
	 * Gets the top-k counts of strings.
	 * 
	 * @param k - The top k number of results to return.
	 * 
	 * @return the counts, or null if not found
	 * @throws RemoteException
	 */
	public ConcurrentCountMap<String> getTopKCounts(int k)  throws RemoteException;
	
	/**
	 * This is just to test connectivity.
	 * 
	 * @return true
	 * @throws RemoteException
	 */
	public boolean test(User u) throws RemoteException;
	
	/**
	 * This is notify the slave that all
	 * data has been transmitted.
	 * 
	 * @return true
	 * @throws RemoteException
	 */
	public boolean finalise(User u) throws RemoteException;
}
