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
import org.mdp.wc.NGramParserIterator;


/**
 * Main method to extract n-grams from a file and write them to an output file.
 * 
 * @author Aidan
 */
public class ExtractNGrams {
	
	public static int TICKS = 10000;
	
	public static final int DEFAULT_N = 2;
	
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
		
		Option kO = new Option("k", "output k bi-grams (omit to output all)");
		kO.setArgs(1);
		
		Option nO = new Option("n", "extract n-grams of length n (default n="+DEFAULT_N+")");
		nO.setArgs(1);
		
		Option helpO = new Option("h", "print help");
				
		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
		options.addOption(outO);
		options.addOption(outgzO);
		options.addOption(kO);
		options.addOption(nO);
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
		
		// open input
		String in = cmd.getOptionValue(inO.getOpt());
		InputStream is = new FileInputStream(in);
		if(cmd.hasOption(ingzO.getOpt())){
			is = new GZIPInputStream(is);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
		
		System.err.println("Reading from "+in);
		
		// open output
		String out = cmd.getOptionValue(outO.getOpt());
		OutputStream os = new FileOutputStream(out);
		if(cmd.hasOption(outgzO.getOpt())){
			os = new GZIPOutputStream(os);
		}
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os),"utf-8"));
		
		System.err.println("Writing to "+out);
		
		int n = DEFAULT_N;
		if(cmd.hasOption(nO.getOpt())){
			n = Integer.parseInt(cmd.getOptionValue(nO.getOpt()));
			System.err.println("Writing "+n+"-grams");
		}
		
		int k = -1;
		if(cmd.hasOption(kO.getOpt())){
			k = Integer.parseInt(cmd.getOptionValue(kO.getOpt()));
			System.err.println("Writing a maximum of "+k+" "+n+"-grams");
		}
		
		long b4 = System.currentTimeMillis();
		
		
		// open a parser that will produce n-grams
		NGramParserIterator ngpi = new NGramParserIterator(br,n);
		int count = 0;
		while(ngpi.hasNext()){
			String ngram = ngpi.next();
			
			count++;
			if(count%TICKS==0){
				System.err.println("... written "+count+" "+n+"-grams");
			}
			
			pw.println(ngram);
			
			if(k>0 && k==count){
				break;
			}
		}
		
		System.err.println("Finished! Read "+count+" "+n+"-grams");
		
		pw.close();
		br.close();
		
		System.err.println("Runtime: "+(System.currentTimeMillis()-b4)/1000+" seconds");
	}
}