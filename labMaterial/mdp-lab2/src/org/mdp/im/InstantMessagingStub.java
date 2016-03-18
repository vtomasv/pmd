package org.mdp.im;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.mdp.dir.User;

public interface InstantMessagingStub extends Remote, Serializable {
	/**
	 * Send a message to the user.
	 * 
	 * @param from - Who are you
	 * @param msg - What's your message
	 * 
	 * @return time received at server (System.currentTimeMillis()).
	 * @throws RemoteException
	 */
	public long message(User from, String msg) throws RemoteException;
}
