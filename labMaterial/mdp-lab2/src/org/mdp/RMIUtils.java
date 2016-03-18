package org.mdp;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RMIUtils {
	static{
		System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", Integer.toString(5*60*1000));
		System.setProperty("sun.rmi.transport.connectionTimeout", Integer.toString(60*1000));
	}
	
	public static final int DEFAULT_REG_PORT = 1099;
	
	/**
	 * Create directories recursively
	 * @param dir
	 * @return
	 */
	public static boolean mkdirs(String dir){
		File f = new File(dir);
		return f.mkdirs();
	}
	
	/**
	 * Create directories recursively needed for a file
	 * @param dir
	 * @return
	 */	
	public static boolean mkdirsForFile(String file){
		File f = new File(file);
		return f.getParentFile().mkdirs();
	}
	
	/**
	 * Log to a file
	 * @param file
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static void setLogFile(String file) throws SecurityException, IOException{
		if(file!=null){
			System.err.println("Setting logger to "+file);
			mkdirsForFile(file);
		
			Logger root = Logger.getLogger("");
			if(root.getHandlers()!=null) for(Handler h:root.getHandlers())
				root.removeHandler(h);
			FileHandler fh = new FileHandler(file);
			fh.setFormatter(new SimpleFormatter());
			
			root.addHandler(fh);
		}
	}
}
