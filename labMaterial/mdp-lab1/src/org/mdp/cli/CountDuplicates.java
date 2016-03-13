package org.mdp.cli;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mdp.utils.StringWithNumber;


/**
 * Main method to count subsequent duplicates in an input file
 * 
 * @author Aidan
 */
public class CountDuplicates {
	
	public static int TICKS = 10000;
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException {
		Option inO = new Option("i", "input file");
		inO.setArgs(1);
		inO.setRequired(true);
		
		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);
		
		Option outO = new Option("o", "output file");
		outO.setArgs(1);
		outO.setRequired(true);
		
		Option outgzO = new Option("ogz", "output file should be GZipped");
		outgzO.setArgs(0);

		Option helpO = new Option("h", "print help");
				
		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
		options.addOption(outO);
		options.addOption(outgzO);
		options.addOption(helpO);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}
		
		// print help options and return
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}
		
		// open the input
		String in = cmd.getOptionValue(inO.getOpt());
		boolean gzIn = cmd.hasOption(ingzO.getOpt());
		
		// open the output
		String out = cmd.getOptionValue(outO.getOpt());
		boolean gzOut = cmd.hasOption(outgzO.getOpt());
		
		long b4 = System.currentTimeMillis();
		processDuplicates(in, gzIn, out, gzOut);
		System.err.println("Runtime: "+(System.currentTimeMillis()-b4)/1000+" seconds");
	}
	
	/**
	 * Performs a count of consecutive duplicate lines from in
	 * and prints line and count to out
	 * 
	 * @param in
	 * @param gzIn
	 * @param out
	 * @param gzOut
	 * @throws IOException
	 */
	public static void processDuplicates(String in, boolean gzIn, String out, boolean gzOut) throws IOException{
		// open the input
		InputStream is = new FileInputStream(in);
		if(gzIn){
			is = new GZIPInputStream(is);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
		System.err.println("Reading from "+in);
		
		// open the output
		OutputStream os = new FileOutputStream(out);
		if(gzOut){
			os = new GZIPOutputStream(os);
		}
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os),"utf-8"));
		System.err.println("Writing to "+out);
		
		// does the actual work
		countDuplicates(br, pw);
		
		br.close();
		pw.close();
	}
	
	/**
	 * Performs a count of consecutive duplicate lines from in
	 * and prints line and count to out
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private static void countDuplicates(BufferedReader in, PrintWriter out) throws IOException {
		String line = null;
		String prev = null;
		int dupes = 1;
		int read = 0, written = 0;
		
		System.err.println("Counting duplicates ...");
		do{
			line = in.readLine();
			read++;
			if(read % TICKS==0){
				System.err.println("... read "+read+" and written "+written);
			}
			
			if(prev!=null){
				if(line!=null && prev.equals(line)){
					dupes++;
				} else{
					String sortNum = StringWithNumber.getSortableNumber(dupes);
					out.println(sortNum+"\t"+prev);
					dupes = 1;
					written ++;
				}
			}
			prev = line;
		} while(line!=null);
		
		System.err.println("Finished! Read "+read+" and written "+written);
	}

}