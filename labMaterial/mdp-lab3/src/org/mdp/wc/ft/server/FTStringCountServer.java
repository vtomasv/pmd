package org.mdp.wc.ft.server;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mdp.dir.util.User;
import org.mdp.wc.util.ConcurrentCountMap;

/**
 * An RMI server for doing a string count assuming replication.
 * More specifically, assumes multiple users can create multiple
 * replicas of the same data, e.g., to send the strings from 
 * the same part of a file. On calling the count methods, the
 * server will choose the most reliable count for each file part
 * (the one corresponding to the most users) and use that.
 * 
 * @author ahogan
 *
 */
public class FTStringCountServer implements FTStringCountStub {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5428231417227321988L;
	
	
	// maps job IDs to user names to count object
	private final ConcurrentHashMap<String,ConcurrentHashMap<String,ConcurrentCountMap<String>>> jobToUserToCounts;
	// keeps track of whether or not job is finalised
	private final ConcurrentHashMap<String,ConcurrentHashMap<String,Boolean>> jobToUserToFinalised;
	// used to synchronise counts per user
	private final UserLock userLocks;
	// cache of final count to avoid recomputing it every time :)
	private ConcurrentCountMap<String> overallCountCached = null;

	public FTStringCountServer(){
		jobToUserToCounts = new ConcurrentHashMap<String,ConcurrentHashMap<String,ConcurrentCountMap<String>>>();
		jobToUserToFinalised = new ConcurrentHashMap<String,ConcurrentHashMap<String,Boolean>>();
		userLocks = new UserLock();
	}

	public boolean countAndIndexStrings(User from, String jobid, Collection<String> strings) throws RemoteException {
		// this will change so need to remove
		synchronized(this){
			overallCountCached = null;
		}
		
		ConcurrentCountMap<String> counts = null;
		boolean added = false;
		
		String lock = userLocks.getOrCreateUserLock(from.getUsername());
		synchronized(lock){
			ConcurrentHashMap<String,ConcurrentCountMap<String>> userToCounts = jobToUserToCounts.get(jobid);
			if(userToCounts==null){
				userToCounts = new ConcurrentHashMap<String,ConcurrentCountMap<String>>();
				jobToUserToCounts.put(from.getUsername(), userToCounts);
			} else{
				counts = userToCounts.get(from.getUsername());
			}

			// check if this is a new user-job
			if(counts == null){
				added = true;
				counts = new ConcurrentCountMap<String>();
				userToCounts.put(from.getUsername(),counts);
			}
		}

		for(String s:strings){
			counts.add(s);
		}

		return added;
	}

	@Override
	public boolean clearJob(User from, String jobid) throws RemoteException {
		// this will change so need to remove
		synchronized(this){
			overallCountCached = null;
		}
		
		String lock = userLocks.getOrCreateUserLock(from.getUsername());
		synchronized(lock){
			boolean removed = false;
			ConcurrentHashMap<String,ConcurrentCountMap<String>> userToCounts = jobToUserToCounts.get(jobid);
			if(userToCounts!=null){
				removed = userToCounts.remove(from.getUsername())!=null;
			}

			ConcurrentHashMap<String,Boolean> userToFinalised = jobToUserToFinalised.get(jobid);
			if(userToFinalised!=null){
				userToFinalised.remove(from.getUsername());
			}
			return removed;
		}
	}

	@Override
	public boolean finaliseJob(User from, String jobid) throws RemoteException {
		String lock = userLocks.getOrCreateUserLock(from.getUsername());
		synchronized(lock){
			ConcurrentHashMap<String,Boolean> userToFinalised = jobToUserToFinalised.get(jobid);
			if(userToFinalised==null){
				return false;
			}
			return userToFinalised.put(from.getUsername(),true);
		}
	}

	@Override
	public ConcurrentCountMap<String> getCountsForUserJob(User from, String jobid) throws RemoteException {
		String lock = userLocks.getOrCreateUserLock(from.getUsername());
		synchronized(lock){
			ConcurrentHashMap<String,ConcurrentCountMap<String>> userToCounts = jobToUserToCounts.get(jobid);
			if(userToCounts!=null){
				return userToCounts.get(from.getUsername());
			}
			return null;
		}
	}

	@Override
	public synchronized ConcurrentCountMap<String> getCounts() throws RemoteException {
		return getTopKCounts(Integer.MAX_VALUE);
	}

	@Override
	public synchronized ConcurrentCountMap<String> getTopKCounts(int k) throws RemoteException {
		// start a blank count
		ConcurrentCountMap<String> overallCount = new ConcurrentCountMap<String>();
		
		if(overallCountCached!=null){
			return overallCountCached;
		}
			
		
		// for each job
		for(Map.Entry<String,ConcurrentHashMap<String,ConcurrentCountMap<String>>> jobToUserToCount: jobToUserToCounts.entrySet()){
			// for each user with that job
			if(jobToUserToCount.getValue()!=null){
				// get the counts for that user-job:
				// then we're going to count the count maps! :)
				// we'll return the most frequent one or a
				// random one if there's no winner
				//
				// the idea is that, for each task, we will
				// use the count map for which
				// the most users (replicas) agree
				ConcurrentCountMap<ConcurrentCountMap<String>> replicaCount = new ConcurrentCountMap<ConcurrentCountMap<String>>();
				
				for(Map.Entry<String,ConcurrentCountMap<String>> userToCount : jobToUserToCount.getValue().entrySet()){
					if(userToCount.getValue()!=null){
						replicaCount.add(userToCount.getValue());
					}
				}
				
				if(replicaCount.size()>0){
					// we have at least one count for the task
					Map.Entry<ConcurrentCountMap<String>,Integer> topReplica = replicaCount.getTopKMap(1).entrySet().iterator().next();
					
					// add the most common replica for the task
					// to the overall count
					// (random if tied)
					overallCount.addAll(topReplica.getKey());
					
					// print a message for consensus.
					// in a stricter system, we may require
					// a minimum number of replicas to agree
					if(replicaCount.size()>1){
						// means there was disagreement across replicas
						System.err.println("Found disagreement in job id "+jobToUserToCount.getKey()+". Picking most common replica on which "+topReplica.getValue()+" of "+replicaCount.size()+" replicas agree");
					} else{
						// perfect agreement across replicas
						System.err.println("No disagreement in job id "+jobToUserToCount.getKey()+". All "+replicaCount.size()+" replicas agree.");
					}
				} else{
					System.err.println("No counts found for job id "+jobToUserToCount.getKey());
				}
			}
		}
		
		overallCountCached = overallCount;
		return overallCount.getTopKMap(k);
	}

	/**
	 * Creates a lock object per user: a consistent reference to the
	 * username string.
	 * 
	 * @author ahogan
	 *
	 */
	public static final class UserLock extends ConcurrentHashMap<String,String>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6358655423612365194L;

		public UserLock(){
			super();
		}

		public synchronized String getOrCreateUserLock(String username){
			String lock = get(username);
			if(lock==null){
				put(username,username);
				lock = username;
			}
			return lock;
		}
	}

}
