package org.mdp.im;

import java.rmi.RemoteException;

import org.mdp.dir.User;

public class InstantMessagingServer implements InstantMessagingStub {
	
	public static final String DEFAULT_REG_NAME = InstantMessagingServer.class.getSimpleName();

	/**
	 * 
	 */
	private static final long serialVersionUID = -6682365848634470441L;

	public long message(User from, String msg) throws RemoteException {
		//TODO print the message, who is was from, etc.
		
		//return the local time the message was received
		return System.currentTimeMillis();
	}
	
}
