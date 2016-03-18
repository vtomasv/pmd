package org.mdp.dir;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * This is the interface that will be registered in the server.
 * In RMI, a remote interface is called a stub (on the client-side)
 * or a skeleton (on the server-side).
 * 
 * An implementation is created and registered on the server.
 * 
 * Remote machines can then call the methods of the interface.
 * 
 * Note: every method *must* throw RemoteException!
 * 
 * Note: every object passed or returned *must* be Serializable!
 * 
 * @author Aidan
 *
 */
public interface UserDirectoryStub extends Remote, Serializable{
	public boolean createUser(User u) throws RemoteException;
	
	public Map<String,User> getDirectory() throws RemoteException;
	
	public User removeUserWithName(String un) throws RemoteException;
}
