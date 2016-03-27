package org.mdp.cli;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import org.mdp.RMIUtils;
import org.mdp.dir.server.UserDirectoryServer;
import org.mdp.dir.server.UserDirectoryStub;
import org.mdp.dir.util.User;

/**
 * This is a simple client example for UserDirectory*.
 * 
 * First the remote registry on the server is found.
 * 
 * Next the stub is retrieved from the registry. The registry is simply
 * a map from stub-names (String) to stubs (an interface extending Remote).
 * 
 * Once the stub is retrieved by the client, it can invoke
 * remote methods to be executed on the server. 
 * 
 * @author Aidan
 *
 */
public class UserDirectoryClientApp {
	//TODO you need to replace this if not using localhost.
	// Details will be given in the class.
	//
	// If you are doing this at home, you can start
	// a directory on your local machine and leave these
	// details are they are.
	public static final String DIR_HOSTNAME = "cluster.dcc.uchile.cl";
	public static final int DIR_PORT = RMIUtils.DEFAULT_REG_PORT;
	
	//TODO replace with your details here
	public static final String USER_NAME = "ahogan";
	public static final String NAME = "Aidan Hogan";
	public static final String IP = "10.0.114.59";
	public static final int PORT = 1985;
	
	/**
	 * An example to test central the directory.
	 * 
	 * Will connect to directory, upload some user details,
	 * get list of users, remove user, get list of users again.
	 * 
	 * @param args
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static void main(String[] args) throws AccessException, RemoteException, NotBoundException{
		
		// first need to connect to the remote registry on the given
		// IP and port
		Registry registry = LocateRegistry.getRegistry(DIR_HOSTNAME, DIR_PORT);
		
		// then need to find the interface we're looking for 
		UserDirectoryStub stub = (UserDirectoryStub) registry.lookup(UserDirectoryServer.class.getSimpleName());
		
		// now we can use the stub to call remote methods!!
		Map<String,User> users = stub.getDirectory();
		System.out.println("Current users in directory ...");
		System.out.println(users.toString());
		
		User u = new User(USER_NAME, NAME, IP, PORT);
		
		System.out.println("Adding self ...");
		stub.createUser(u);
			
		System.out.println("Current users in directory (2) ...");
		users = stub.getDirectory();
		System.out.println(users.toString());
		
//		System.out.println("Removing self ...");
//		stub.removeUserWithName(USER_NAME);
//		
//		System.out.println("Current users in directory (3) ...");
//		users = stub.getDirectory();
//		System.out.println(users.toString());
	}
}
