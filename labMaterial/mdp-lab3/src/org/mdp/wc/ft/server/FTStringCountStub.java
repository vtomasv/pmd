package org.mdp.wc.ft.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import org.mdp.dir.util.User;
import org.mdp.wc.util.ConcurrentCountMap;

/**
 * An RMI stub for doing a string count assuming replication.
 * More specifically, assumes multiple users can create multiple
 * replicas of the same data, e.g., to send the strings from 
 * the same part of a file. On calling the count methods, the
 * server will choose the most reliable count for each file part
 * (the one corresponding to the most users) and use that.
 * 
 * @author ahogan
 *
 */
public interface FTStringCountStub extends Remote, Serializable {
	/**
	 * Get the server to index counts for some strings.
	 * 
	 * @param from - Details of client sending strings
	 * 
	 * @param jobid - A key to identify the job (e.g., part of a file). 
	 * Multiple calls with same jobid and same user will be added together
	 * Mutliple jobs from different users with same jobid will be considered as replicas.
	 * 
	 * @param strings - The raw strings to count
	 * 
	 * @return true if new user-job, false otherwise
	 * @throws RemoteException
	 */
	public boolean countAndIndexStrings(User from, String jobid, Collection<String> strings) throws RemoteException;
	
	/**
	 * Remove a job previously indexed, including all batches
	 * 
	 * @param from - Details of client sending strings
	 * 
	 * @param jobid - A key to identify the job to remove (e.g., part of a file). 
	 * @return true if removed, false otherwise
	 * @throws RemoteException
	 */
	public boolean clearJob(User from, String jobid) throws RemoteException;
	
	/**
	 * Let the server know the client has sent all strings.
	 * 
	 * @param from - Details of client sending strings
	 * 
	 * @param jobid - A key to identify the job (e.g., part of a file). 
	 * Multiple calls with same jobid and same user will be added together
	 * Multiple jobs from different users with same jobid will be considered as replicas.
	 * 
	 * @return true if finalised, false if already finalised or job not found
	 * @throws RemoteException
	 */
	public boolean finaliseJob(User from, String jobid) throws RemoteException;
	
	/**
	 * Gets the counts of strings for a specific user.
	 * 
	 * @param from - Details of client who sent strings
	 * 
	 * @param jobId - A key to identify the job (e.g., part of a file). 
	 * Multiple calls with same jobid and same user will be added together
	 * Multiple jobs from different users with same jobid will be considered as replicas.
	 * 
	 * @return the counts, or null if not found
	 * @throws RemoteException
	 */
	public ConcurrentCountMap<String> getCountsForUserJob(User from, String jobId)  throws RemoteException;
	
	/**
	 * Gets the counts of strings across all jobs. Will cross-check multiple users for each
	 * job to ensure replicas are consistent and return only if there's a majority of users
	 * for which counts are the same.
	 * 
	 * @return the counts, or null if not found
	 * @throws RemoteException
	 */
	public ConcurrentCountMap<String> getCounts()  throws RemoteException;
	
	/**
	 * Gets the top-k counts of strings across all jobs. Will cross-check multiple users for each
	 * job to ensure replicas are consistent and return only if there's a majority of users
	 * for which counts are the same.
	 * 
	 * @param k - The top k number of results to return.
	 * 
	 * @return the counts, or null if not found
	 * @throws RemoteException
	 */
	public ConcurrentCountMap<String> getTopKCounts(int k)  throws RemoteException;
}
