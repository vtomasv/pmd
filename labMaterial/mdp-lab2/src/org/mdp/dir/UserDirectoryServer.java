package org.mdp.dir;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the implementation of UserDirectoryStub.
 * 
 * The stub will be registered in the RMI registry of the server.
 * 
 * When a client calls a method on the stub, the following implementation
 * will be executed on the server.
 * 
 * The main method StartRegistryAndServer can be used to start a server
 * registry and stub on the command line.
 * 
 * @author Aidan
 *
 */
public class UserDirectoryServer implements UserDirectoryStub {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6025896167995177840L;
	private Map<String,User> directory;
	
	public UserDirectoryServer(){
		directory = new ConcurrentHashMap<String,User>();
	}

	/**
	 * Return true if successful, false otherwise.
	 * Will override existing user!
	 * (Does not check that connection is possible.)
	 */
	public boolean createUser(User u) {
		if(u.getUsername()==null)
			return false;
		
		directory.put(u.getUsername(), u);
		
		System.out.println("New user registered! Bienvendio a ...\n\t"+u);
		return true;
	}

	/**
	 * Returns the current directory of users.
	 */
	public Map<String, User> getDirectory() {
		return directory;
	}

	/**
	 * Just an option to clean up if necessary!
	 */
	public User removeUserWithName(String un) {
		System.out.println("Removing username '"+un+"'. Chao!");
		return directory.remove(un);
	}
}
