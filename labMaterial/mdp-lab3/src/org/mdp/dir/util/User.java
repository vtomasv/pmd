package org.mdp.dir.util;

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + port;
		result = prime * result + ((realname == null) ? 0 : realname.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (port != other.port)
			return false;
		if (realname == null) {
			if (other.realname != null)
				return false;
		} else if (!realname.equals(other.realname))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
