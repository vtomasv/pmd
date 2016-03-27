package org.mdp.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mdp.RMIUtils;
import org.mdp.dir.server.UserDirectoryServer;
import org.mdp.dir.server.UserDirectoryStub;
import org.mdp.dir.util.User;
import org.mdp.wc.naive.server.NaiveStringCountServer;
import org.mdp.wc.naive.server.NaiveStringCountStub;
import org.mdp.wc.util.ConcurrentCountMap;
import org.mdp.wc.util.NGramParserIterator;


public class DistributedNgramCountApp {

	static String LOGO =
			"=============================================================\n" +
			"=============================================================\n" +
			" ______     ______     __   __     ______   ______     ______\n" +
			"/\\  ___\\   /\\  __ \\   /\\ \"-.\\ \\   /\\__  _\\ /\\  __ \\   /\\  == \\ \n" +  
			"\\ \\ \\____  \\ \\ \\/\\ \\  \\ \\ \\-.  \\  \\/_/\\ \\/ \\ \\  __ \\  \\ \\  __<  \n" + 
			" \\ \\_____\\  \\ \\_____\\  \\ \\_\\\\\"\\_\\    \\ \\_\\  \\ \\_\\ \\_\\  \\ \\_\\ \\_\\ \n" +
			"  \\/_____/   \\/_____/   \\/_/ \\/_/     \\/_/   \\/_/\\/_/   \\/_/ /_/\n" +
			"=============================================================\n";

	static String INTRO = LOGO +"... 1 2 3 ...";
	
	
	public static final int DEFAULT_N = 3;
	public static final int DEFAULT_K = 10;
	
	public static final int BATCH_SIZE = 50000;
	
	public static final int TICKS = 500000;
	
	static TreeSet<String> RESTRICTED;
	static {
		RESTRICTED = new TreeSet<String>();
		RESTRICTED.add("");
	}

	/** 
	 * @param args
	 * @throws IOException
	 * @throws AlreadyBoundException
	 * @throws NotBoundException 
	 */
	public static void main(String args[]) throws IOException, AlreadyBoundException, NotBoundException{
		Option dhostnameO = new Option("dh", "hostname or ip of directory (defaults to localhost)");
		dhostnameO.setArgs(1);
		
		Option dportO = new Option("dp", "port of directory (defaults to "+RMIUtils.DEFAULT_REG_PORT+")");
		dportO.setArgs(1);
		
		Option iO = new Option("i", "input file to count words from");
		iO.setArgs(1);
		iO.setRequired(true);
		
		Option igzO = new Option("igz", "input file is gzipped (default not)");
		igzO.setArgs(0);

		Option nO = new Option("n", "length of n-grams to count (default "+DEFAULT_N+")");
		nO.setArgs(1);
		
		Option kO = new Option("k", "top-k counts to return (default "+DEFAULT_K+")");
		kO.setArgs(1);

		Option helpO = new Option("h", "print help (e.g., to see server types)");

		Options options = new Options();
		options.addOption(dhostnameO);
		options.addOption(dportO);
		options.addOption(iO);
		options.addOption(igzO);
		options.addOption(nO);
		options.addOption(kO);
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
		String dir_hostname = null;
		if (cmd.hasOption("dh")) {
			dir_hostname = cmd.getOptionValue("dh");
		}

		int dir_port = RMIUtils.DEFAULT_REG_PORT;
		if (cmd.hasOption("dp")) {
			dir_port = Integer.parseInt(cmd.getOptionValue("dp"));
		}
		
		String inputFile = cmd.getOptionValue("i");
		boolean gz = cmd.hasOption("igz");
		
		// get the top-k for the word count
		// should print only top-k most popular words
		int k = DEFAULT_K;
		if(cmd.hasOption(kO.getOpt())){
			k = Integer.parseInt(cmd.getOptionValue(kO.getOpt()));
		}

		int n = DEFAULT_N;
		if(cmd.hasOption(kO.getOpt())){
			n = Integer.parseInt(cmd.getOptionValue(nO.getOpt()));
		}

		distributedNgramCount(inputFile, gz, k, n, dir_port, dir_hostname);
	}

	public static void distributedNgramCount(String inputFile, boolean gz, int k, int n, int dir_port, String dir_hostname) throws IOException, NotBoundException, AlreadyBoundException {
		System.out.println(INTRO);
		
		// open input from file
		InputStream is = new FileInputStream(inputFile);
		if(gz){
			is = new GZIPInputStream(is);
		}
		BufferedReader fileInput = new BufferedReader(new InputStreamReader(is,"utf-8"));
		
		// open iterator
		NGramParserIterator ngramIter = new NGramParserIterator(fileInput,n);
		
		// open prompt input
		BufferedReader prompt = new BufferedReader(new InputStreamReader(System.in));
		
		// read inputs on user details
		String uname = null;
		while(true){
			System.out.println("\n\nFirst enter a username ...");
			String line = prompt.readLine().trim();
			if(RESTRICTED.contains(line)){
				System.err.println("\n\nRestricted username ... try another");
			} else{
				uname = line;
				break;
			}
		}

		String name = null;
		while(true){
			System.out.println("\n\nNext enter your real name ...");
			String line = prompt.readLine().trim();
			if(RESTRICTED.contains(line)){
				System.err.println("\n\nRestricted username ... try another");
			} else{
				name = line;
				break;
			}
		}

		String hostIp = null;
		while(true){
			System.out.println("\n\nNext your hostname or local IP ...");
			String line = prompt.readLine().trim();
			hostIp = line;
			break;
		}

		int uport = -1;
		while(true){
			System.out.println("\n\nFinally your port (any number between 1001 and 1999 ...)");
			String line = prompt.readLine().trim();
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
		
		// set the hostname property
		System.setProperty("java.rmi.server.hostname", hostIp);

		// Then start your own RMI registry ...
		// so people sending you n-grams you can find your server
		Registry reg = startRegistry(me.getPort());

		// and start your count server to start receiving n-grams ...
		
		// create a skeleton/stub for the n-gram counting server
		// exportObject just creates an interface that can be called
		// remotely
		NaiveStringCountServer server = new NaiveStringCountServer();
		Remote stub = UnicastRemoteObject.exportObject(server,0);

		// just use the class name for the skeleton (any name would be fine
		// but client needs to know the name to find the stub)
		String skelname = NaiveStringCountServer.class.getSimpleName();

		// bind the skeleton to the registry under the given name
		reg.bind(skelname, stub);

		
		// now connect to directory to find
		// machines to count words for you
		UserDirectoryStub uds = connectToDirectory(dir_hostname, dir_port);
		
		// send it your details
		uds.createUser(me);
		
		// okay now we need to wait until everyone is at this same point
		Map<String,User> userDir = null;
		String line = null;
		do{
			System.out.println("\n\n1) We need to coordinate here and wait until everyone has started a server and registered it in the directory.");
			System.out.println("When everyone is at this point in the lab, type 'next'. Please don't do it before that point.");
			System.out.println("Otherwise you can type enter to refresh the user list");
			
			userDir = uds.getDirectory();
			printDirectory(userDir);
			
			line = prompt.readLine().trim();
		} while(!line.equals("next"));
		
		// refresh users just in case
		userDir = uds.getDirectory();
		printDirectory(userDir);
		
		ArrayList<NaiveStringCountStub> slaves = new ArrayList<NaiveStringCountStub>();
		ArrayList<User> users = new ArrayList<User>();
		
		// let's now open a connection to each
		// machine and get its NaiveStringCountStub
		System.out.println("\n\nConnecting to servers of users ...");
		
		for(User u: userDir.values()){
			try {
				
				// find the registry of that user
				Registry registry = LocateRegistry.getRegistry(u.getHostname(), u.getPort());
		
				// then get the stub we want from the registry
				System.out.println("\nBinding to "+u.getUsername());
				
				NaiveStringCountStub slave = (NaiveStringCountStub) registry.lookup(NaiveStringCountServer.class.getSimpleName());
				
				// let's test we can call the test method
				System.out.println("Slave "+u.getUsername()+" working: "+slave.test(me));
				
				slaves.add(slave);
				users.add(u);
				
			} catch (Exception e) {
				System.out.println("Alguien no esta bien configurado " );
				e.printStackTrace();
			}

			

		}
		
		System.out.println("\n\nConnected to "+slaves.size()+" slaves!");
		
		// this object will store batches of n-grams
		// to be sent to each server
		// 
		// e.g., batches.get(hash) indicates words to be sent to slaves.get(hash)
		// once batches.get(hash).size() reaches BATCH_SIZE, call
		// slaves.get(hash).countAndIndexStrings(batches.get(hash));
		ArrayList<ArrayList<String>> batches = new ArrayList<ArrayList<String>>(slaves.size());
		for(int i=0; i<slaves.size(); i++){
			batches.add(new ArrayList<String>(BATCH_SIZE));
		}
		
		int count = 0;
		int batchesSent = 0;
		while(ngramIter.hasNext()){
			String ngram = ngramIter.next();
			
			count++;
			if(count%TICKS==0){
				System.out.println("We have read "+count+" ngrams from file");
			}
			
			//  decide to which machine to send the ngram
			//  look at instructions for this ...
			int hc = Math.abs(ngram.hashCode()) % slaves.size();
			
			// TODO add the ngram to the batch for that machine
			batches.get(hc).add(ngram);
			
			
			// TODO if we're at max batch size (BATCH_SIZE), send the ngrams
			// AND clear the batch
			// AND increment batches sent
			// AND print a message to say batch has been sent
			if (batches.get(hc).size() == BATCH_SIZE )
			{
				slaves.get(hc).countAndIndexStrings(batches.get(hc));
				batches.get(hc).clear();
				batchesSent++;
				System.out.println("El batch" + hc + " fue enviado");
				
			}
				

		}
		
		System.out.println("Read "+count+" ngrams and sent them in "+batchesSent+" batches.");
		
		
		// TODO push the remaining batches to the slaves
		for (int i = 0; i < batches.size(); i++) {
			if (batches.get(i).size() >= 0)
			{			
				slaves.get(i).countAndIndexStrings(batches.get(i));
				batches.get(i).clear();
			}
		}
		
		// TODO tell all the slaves you're finished sending data
		//  slaves.get(i).finalise(me)
		for (int i = 0; i < slaves.size(); i++) {
			slaves.get(i).finalise(me);
		}

		
		
		// now we need a coordination point to wait until everyone
		// is done ... this time we can check automatically
		
		boolean allFinalised = false;
		do{
			System.out.println("\n\nWe need to coordinate here and wait until everyone has sent their data.");
			System.out.println("We will wait until everyone has finalised automatically. Hit enter to refresh.\n");
			
			Set<User> finalised = server.finalised();
			
			allFinalised = true;
			for(User u:userDir.values()){
				if(finalised.contains(u)){
					System.out.println("Finalised "+u);
				} else{
					System.out.println("Still waiting on "+u);
					allFinalised = false;
				}
			}
			
			line = prompt.readLine().trim();
		} while(!allFinalised);
		
		
		System.out.println("\n\nAll users have finalised. Let's get the top-"+k+" n-grams from each slave");
		// now each slave should have the final counts for its
		// batch, so we can get the top-k from each slave
		// and find the best one
		
		// we'll do this the easy way, using a count object
		ConcurrentCountMap<String> globalTopKs = new ConcurrentCountMap<String>(); 
		for(NaiveStringCountStub slave: slaves){
			globalTopKs.addAll(slave.getTopKCounts(k));
		}
		
		// we should now have m x k n-grams in the globalTopKs object
		// for m the number of machines.
		// finally we can just print the top-k from that map:
		synchronized(server){
			System.out.println("\n\nThe top-"+k+" "+n+"-grams in the data are:");
			globalTopKs.printOrderedStats(k);
		}
		
		// we are done
		// hopefully it worked out okay
		do{
			System.out.println("\n\n2) We need to wait in case other users have yet to ask us for our counts.");
			System.out.println("We could do this automatically but let's wait manually. Type 'end' when everyone is done.");
			
			line = prompt.readLine().trim();
		} while(!line.equals("end"));
		
		System.out.println("\n\nIn total, we counted "+server.getNonUniqueStringsCounted()+" non-unique n-grams and "+server.getCounts().size()+" unique n-grams");
		
		// remove ourselves from the directory
		uds.removeUserWithName(me.getUsername());
		
		fileInput.close();
		prompt.close();
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
	 * Connect to the central directory.
	 * 
	 * @param port of directory 
	 * @param hostname of directory
	 * 
	 * 
	 * @return A stub to call message method on
	 * @throws RemoteException 
	 * @throws NotBoundException 
	 */
	public static UserDirectoryStub connectToDirectory(String hostname, int port) throws RemoteException, NotBoundException {
		// first need to connect to the remote registry on the given
		// IP and port
		// If you like, pass info through the command-line instead
		// of hard-coding (optional)
		Registry registry = LocateRegistry.getRegistry(hostname, port);

		// then need to find the interface we're looking for 
		return (UserDirectoryStub) registry.lookup(UserDirectoryServer.class.getSimpleName());
	}

	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("parameters:", options);
	}
	
	public static void printDirectory(Map<String,User> directory){
		for(User u: directory.values()){
			System.out.println(u);
		}
	}
}
