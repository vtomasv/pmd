package org.mdp.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mdp.RMIUtils;
import org.mdp.dir.User;
import org.mdp.dir.UserDirectoryServer;
import org.mdp.dir.UserDirectoryStub;
import org.mdp.im.InstantMessagingServer;
import org.mdp.im.InstantMessagingStub;


public class InstantMessagingApp {

	
	static private Map<String,InstantMessagingStub> directoryOfRepositorys = new ConcurrentHashMap<String,InstantMessagingStub>();
	
	static String LOGO = 
			"========================================\n"+
					" _ __ ___   ___ _ __  ___  __ _ (_) ___\n"+ 
					"| '_ ` _ \\ / _ \\ '_ \\/ __|/ _` || |/ _ \n"+
					"| | | | | |  __/ | | \\__ \\ (_| || |  __/\n"+
					"|_| |_| |_|\\___|_| |_|___/\\__,_|/ |\\___|\n"+
					"                               |__/     \n"+
					"========================================\n";

	static String INTRO = LOGO +"... vivir mejor conectado (con suerte)\n";

	static String LIST = "list";
	static String ADD = "add";
	static String RM = "rm";
	static String MSG = "msg";
	static String ALL = "all";

	static TreeSet<String> RESTRICTED;
	static {
		RESTRICTED = new TreeSet<String>();
		RESTRICTED.add(ADD);
		RESTRICTED.add(RM);
		RESTRICTED.add(MSG);
		RESTRICTED.add(ALL);
		RESTRICTED.add("");
	}


	/** 
	 * @param args
	 * @throws IOException
	 * @throws AlreadyBoundException
	 * @throws NotBoundException 
	 */
	public static void main(String args[]) throws IOException, AlreadyBoundException, NotBoundException{
		Option hostnameO = new Option("n", "hostname or ip of directory (defaults to localhost)");
		hostnameO.setArgs(1);

		Option portO = new Option("p", "port of directory (defaults to "+RMIUtils.DEFAULT_REG_PORT+")");
		portO.setArgs(1);

		Option helpO = new Option("h", "print help (e.g., to see server types)");

		Options options = new Options();
		options.addOption(hostnameO);
		options.addOption(portO);
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

		// set hostname ... if null, same as localhost
		String dir_hostname = "localhostt";
		if (cmd.hasOption("n")) {
			dir_hostname = cmd.getOptionValue("n");
		}

		int dir_port = RMIUtils.DEFAULT_REG_PORT;
		if (cmd.hasOption("p")) {
			dir_port = Integer.parseInt(cmd.getOptionValue("p"));
		}

		System.out.println(INTRO);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String uname, name, hostIp;
		int uport;

		while(true){
			System.out.println("\n\nFirst enter a username ...");
			String line = br.readLine().trim();
			if(RESTRICTED.contains(line)){
				System.err.println("\n\nRestricted username ... try another");
			} else{
				uname = line;
				break;
			}
		}

		while(true){
			System.out.println("\n\nNext enter your real name ...");
			String line = br.readLine().trim();
			if(RESTRICTED.contains(line)){
				System.err.println("\n\nRestricted username ... try another");
			} else{
				name = line;
				break;
			}
		}

		while(true){
			System.out.println("\n\nNext your hostname or local IP ...");
			String line = br.readLine().trim();
			hostIp = line;
			break;
		}

		while(true){
			System.out.println("\n\nFinally your port (any number between 1001 and 1999 ...)");
			String line = br.readLine().trim();
			try{
				uport = Integer.parseInt(line);
				if(uport>1000 && uport<2000){
					break;
				} else{
					System.err.println("\n\nInvalid port (should be between 1001 and 1999)");
				}
			} catch(Exception e){
				System.err.println("\n\nNot a number (should be between 1001 and 1999)");
			}
		}

		User me = new User(uname, name, hostIp, uport);

		// Then start your own RMI registry ...
		// so people messaging you can find your server
		Registry reg = startRegistry(me.getPort());

		// and start your message server to start receiving messages ...
		// need to keep the reference to server even if not used!
		@SuppressWarnings("unused")
		Remote server = bindSkeleton(reg);

		// Connect to the directory
		UserDirectoryStub uds = connectToDirectory(dir_hostname, dir_port);

		Map<String,User> dir = uds.getDirectory();

		while(true){
			System.out.println("\n\nType '"+LIST+"' to up-to-date directory, '"+ADD+"' to add yourself to the directory, '"+RM+"' to remove yourself from directory, or '"+MSG+"' to send a message\n");

			String line = br.readLine().trim();

			if(line.equals(LIST)){
				dir = uds.getDirectory();
				System.out.println("\n\nDirectory of usernames ...");
				if(dir.isEmpty()){
					System.out.println("[empty]");
				}
				else for(String s: dir.keySet()){
					System.err.println(s+" ");
				}
				
			} else if(line.equals(RM)){
				System.out.print("Removing myself from directory ...");
				uds.removeUserWithName(me.getUsername());
				System.out.println(" [done].");
			} else if(line.equals(ADD)){
				System.out.print("Adding details to directory ...");
				uds.createUser(me);
				System.out.println(" [done].");
			} else if(line.equals(MSG)){
				System.out.println("\n\nWhich user would you like to message (or type 'all') ...");

				line = br.readLine().trim();

				String target = line;
				ArrayList<String> usernames = new ArrayList<String>();

				if(target.equals(ALL)){
					usernames.addAll(dir.keySet());
				} else{
					usernames.add(target);
				}

				System.out.println("\n\nEnter your message ...");
				line = br.readLine().trim();
				
				for(String username:usernames){

					User user = dir.get(username);
					if(user==null){
						System.err.println("\n\nCould not find the user "+username+" in the directory. (Type 'list' to refresh directory.)");
					} else{
						try{
							long ms = messageUser(user,me,line);
							System.out.println("\n\nMessage received by "+username+" at time "+ms);
						} catch(Exception e){
//							e.printStackTrace();
							System.out.println("\n\nCould not message user "+username+": maybe they logged off?");
						}
					}
				}
			} else{
				System.err.println("Unrecognised command "+line);
			}
		}

	}

	/**
	 * I'm very generous and I've done this for you.
	 * 
	 * Starts your registry on the given port.
	 * 
	 * @param port
	 * @return
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 */
	public static Registry startRegistry(int port) throws RemoteException, AlreadyBoundException {
		// open and return the registry on the port
		return LocateRegistry.createRegistry(port);
	}

	/**
	 * Create a skeleton for InstantMessagingServer and bind it to the registry
	 * 
	 * This is what other people will access to message you
	 * 
	 * @param r
	 * @return
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 */
	public static Remote bindSkeleton(Registry r) throws RemoteException, AlreadyBoundException {
		
		// Ahora lo que hacemos es registrar nuestro servidor de mensajeria local
		// en nuestro repositorio para que otro cliente que quiera hablar con nosotros
		// use este objeto de forma remota. 
		Remote skel = new InstantMessagingServer();
		
		InstantMessagingStub stub = (InstantMessagingStub) UnicastRemoteObject.exportObject(skel, 0);
		
		r.bind(InstantMessagingServer.DEFAULT_REG_NAME, stub);

		return skel;
	}

	/**
	 * Message user.
	 * 
	 * Connect to their registry, retrieve the InstantMessagingStub stub.
	 * Call the message method on the stub.
	 * 
	 * @param username
	 * @param msg
	 * @return Time acknowledged by message server
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 * @throws AccessException 
	 */
	public static long messageUser(User to, User from, String msg) throws AccessException, RemoteException, NotBoundException{

		InstantMessagingStub server  = InstantMessagingApp.directoryOfRepositorys.get(to.getUsername());
		
		
		if ( server == null )
		{ 		
			// Lo primero que hacemos es conectarnos al repositorio del usuario que queremos 
			// hablarle 
			Registry registry = LocateRegistry.getRegistry(to.getHostname(), to.getPort());

			// Una vez  nos conectamos traemos el objeto remoto que representa a su servidor 
			// de mensajeria para poder invocarle metodos remotamente. 
			
			server = (InstantMessagingStub) registry.lookup(InstantMessagingServer.DEFAULT_REG_NAME);

			InstantMessagingApp.directoryOfRepositorys.put(to.getUsername(), server);
		}
		

		// Una vez que tenemos el objeto remoto podemos invocar 
		// metodos sin problemas!! 
		long dateInLong  = server.message(from, msg);
		
		return dateInLong;
	}

	/**
	 * Connect to the central directory.
	 * 
	 * (See UserDirectoryClient for example)
	 * @param port 
	 * @param hostname 
	 * 
	 * 
	 * @return A stub to call message method on
	 * @throws RemoteException 
	 * @throws NotBoundException 
	 */
	public static UserDirectoryStub connectToDirectory(String hostname, int port) throws RemoteException, NotBoundException {

		// Lo primero que hacemos es conectarnos al Registro del servidor
		// que contiene a todos los usuarios
		Registry registry = LocateRegistry.getRegistry(UserDirectoryClientApp.DIR_HOSTNAME, UserDirectoryClientApp.DIR_PORT);
		
		// Lo siguiente es obtener el objeto remoto del directorio asi a ese objeto
		// le podemos pedir quien esta conectado! 
		UserDirectoryStub stub = (UserDirectoryStub) registry.lookup(UserDirectoryServer.class.getSimpleName());
		
		
		return stub;
	}

	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("parameters:", options);
	}
}
