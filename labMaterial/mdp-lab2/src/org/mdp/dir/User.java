package org.mdp.dir;

import java.io.Serializable;

/**
 * A user in the IM system.
 * 
 * Immutable objects (once created, cannot be changed). 
 * 
 * Must implement serializable since objects need to be
 * transmitted over the wire!
 * 
 * @author Aidan
 *
 */
public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2333232814155348186L;
	
	private final String username;
	private final String hostname;
	private final String realname;
	private final int port;
	
	/**
	 * Create a new user
	 * 
	 * @param username - a unique username
	 * @param realname - your real name
	 * @param hostname - your IP address
	 * @param port - any free port (1 to 65535, not 80, 21, 8080, 8180, etc.)
	 */
	public User(String username, String realname, String hostname, int port){
		this.username = username;
		this.realname = realname;
		this.hostname = hostname;
		this.port = port;
	}

	public String getUsername() {
		return username;
	}
	
	public String getRealname() {
		return realname;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}
	
	public String toString(){
		return "Username: '"+username+"'\tReal-name: '"+realname+"'\tPort: '"+port+"'\tHostname: '"+hostname+"'";
	}
}
