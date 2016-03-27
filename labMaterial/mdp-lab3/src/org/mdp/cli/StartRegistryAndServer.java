package org.mdp.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mdp.RMIUtils;
import org.mdp.dir.server.UserDirectoryServer;
import org.mdp.wc.ft.server.FTStringCountServer;
import org.mdp.wc.naive.server.NaiveStringCountServer;

/**
 * Main method to start a RMI registry and a pre-loaded server.
 * 
 * @author Aidan
 */
public class StartRegistryAndServer {
	
	static Remote server;
	
	// what kinds of servers can this main method open?
	public static final Map<Integer,Class<? extends Remote>> SERVER_TYPES = new HashMap<Integer,Class<? extends Remote>>();
	static {
		SERVER_TYPES.put(1,UserDirectoryServer.class);
		SERVER_TYPES.put(2,NaiveStringCountServer.class);
		SERVER_TYPES.put(3,FTStringCountServer.class);
	}
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
		long time = System.currentTimeMillis();
		
		Option hostnameO = new Option("n", "hostname (defaults to localhost)");
		hostnameO.setArgs(1);
		
		Option registryO = new Option("r", "start the RMI registry");
		registryO.setArgs(0);
		
		Option setpropO = new Option("sp", "set the 'java.rmi.server.hostname' to the value for 'n'");
		setpropO.setArgs(0);
		
		Option portO = new Option("p", "what port to start the RMI registry on (defaults to "+RMIUtils.DEFAULT_REG_PORT+")");
		portO.setArgs(1);
		
		Option serverO = new Option("s", "what kind of server to start (id)?");
		serverO.setArgs(1);
		serverO.setRequired(true);

		Option helpO = new Option("h", "print help (e.g., to see server types)");
				
		Options options = new Options();
		options.addOption(hostnameO);
		options.addOption(portO);
		options.addOption(registryO);
		options.addOption(setpropO);
		options.addOption(serverO);
		options.addOption(helpO);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
			printHelp(options);
			return;
		}
		
		// print help options and return
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}

		// set hostname ... if null, set to localhost
		String hostname = null;
		if (cmd.hasOption("n")) {
			hostname = cmd.getOptionValue("n");
			if(cmd.hasOption("sp")){
				System.setProperty("java.rmi.server.hostname", hostname);
				System.err.println("Set property java.rmi.server.hostname to "+hostname);
			}
		}
		
		int port = RMIUtils.DEFAULT_REG_PORT;
		if (cmd.hasOption("p")) {
			port = Integer.parseInt(cmd.getOptionValue("p"));
		}
		
		// start a registry or connect to an existing one?
		boolean startReg = false;
		if (cmd.hasOption("r")) {
			startReg = true;
		}
		
		Registry registry;
		if(startReg){
			// creates a local registry
			registry = LocateRegistry.createRegistry(port);
			System.err.println("Registry setup on port " + port);
		} else{
			// connects to a potentially remote registry
			
			if(hostname==null)
				registry = LocateRegistry.getRegistry(port);
			else
				registry = LocateRegistry.getRegistry(hostname, port);
		}
				
		// sees what kinds of skeleton implementations can be started
		Class<? extends Remote> serverClass = SERVER_TYPES.get(Integer.parseInt(cmd.getOptionValue("s")));
		if(serverClass==null){
			System.err.println("***ERROR: Unrecognised server id type: " + cmd.getOptionValue("s"));
			printHelp(options);
			return;
		}
		
		// create a remote stub to make it 
		// ready for incoming calls
		// important to keep the reference to stub!
		System.err.println("Creating static server reference ...");
		server = serverClass.newInstance();
		Remote stub = UnicastRemoteObject.exportObject(server,0);
		String stubname = serverClass.getSimpleName();
		registry.bind(stubname, stub);

		
		long time1 = System.currentTimeMillis();
	    
	    System.err.println("Server ready in " + (time1-time) + " ms.");
	    
	    if(startReg){
	    	// keep the registry alive by waiting
	    	// on a user input
	    	
	    	System.err.println("Keeping alive (registry)... Enter k to kill:");
	    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    	String line;
	    	while((line = br.readLine())!=null && !line.trim().equals("k")){
	    		System.err.println("Type k to kill: "+line);
	    	}
	    }
	}
	
	
	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("parameters:", options);
		System.out.println("Server options: ");
		for(Map.Entry<Integer,Class<? extends Remote>> s: SERVER_TYPES.entrySet()){
			System.out.println(s.getKey()+" "+s.getValue().toString());
		}
	}
}