package org.mdp.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mdp.wc.CountMap;
import org.mdp.wc.WordParserIterator;

/**
 * Offers main method to count words in a file in memory.
 * 
 * @author ahogan
 *
 */
public class RunWordCountInMemory {
	
	public static int TICKS = 1000000;
	
	public static void main(String[] args) throws IOException{
		// start time
		long b4 = System.currentTimeMillis();
		
		
		// command line options
		Option inO = new Option("i", "input file");
		inO.setArgs(1);
		inO.setRequired(true);
		
		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);
		
		Option helpO = new Option("h", "print help");
		
		Option kO = new Option("k", "do a top-k count (default all)");
		kO.setArgs(1);
				
		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
		options.addOption(kO);
		options.addOption(helpO);

		// parse command line options
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
		
		// get the top-k for the word count
		// should print only top-k most popular words
		int k = Integer.MAX_VALUE;
		if(cmd.hasOption(kO.getOpt())){
			k = Integer.parseInt(cmd.getOptionValue(kO.getOpt()));
		}
		
		// open input
		String in = cmd.getOptionValue(inO.getOpt());
		InputStream is = new FileInputStream(in);
		if(cmd.hasOption(ingzO.getOpt())){
			is = new GZIPInputStream(is);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));

		// start parsing words
		CountMap<String> words = new CountMap<String>();
		WordParserIterator wpi = new WordParserIterator(br);
		int nonUnique = 0;
		while(wpi.hasNext()){
			// always important to print progress
			// otherwise you'll never know where you
			// are ... you could be at 2% or 99%
			// or in an infinite loop
			nonUnique++;
			if(nonUnique % TICKS == 0){
				System.err.println("Read "+nonUnique+" non-unique words");
				System.err.println("Read "+words.size()+" unique words");
			}
			
			words.add(wpi.next());
		}
		
		// print overall stats to std err
		System.err.println("Finished!");
		System.err.println("Read "+nonUnique+" non-unique words");
		System.err.println("Read "+words.size()+" unique words");
		
		// print top-k words found to std out
		
		words.printOrderedStats(k);
		
		// close stream
		br.close();
		
		// print runtime
		System.err.println("Finished in "+(double)(System.currentTimeMillis()-b4)/1000+" seconds");
	}
}

