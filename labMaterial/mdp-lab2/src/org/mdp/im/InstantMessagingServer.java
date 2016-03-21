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

		String leftAlignFormat = "| %-40s | %-10s |%n";

		System.out.format("+------------------------------------------+------------+%n");
		System.out.format("|              Mensaje                     |   Usuario  |%n");
		System.out.format("+------------------------------------------+------------+%n");
		System.out.format(leftAlignFormat, msg , from.getUsername());
		System.out.format("+------------------------------------------+------------+%n");
		
		return System.currentTimeMillis();
	}
	
}
