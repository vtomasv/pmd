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


/**
 * Main method to copy lines from the head of an input file
 * 
 * @author Aidan
 */
public class Split {
	
	public static int TICKS = 10000;
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException {
		Option inO = new Option("i", "input file");
		inO.setArgs(1);
		inO.setRequired(true);
		
		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);
		
		Option outO = new Option("o", "output file [use $ to be replace by split number]");
		outO.setArgs(1);
		outO.setRequired(true);
		
		Option outgzO = new Option("ogz", "output file should be GZipped");
		outgzO.setArgs(0);
		
		Option lO = new Option("l", "number of lines in file");
		lO.setArgs(1);
		
		Option sO = new Option("s", "number of splits");
		sO.setArgs(1);
		sO.setRequired(true);
		
		Option helpO = new Option("h", "print help");
				
		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
		options.addOption(outO);
		options.addOption(outgzO);
		options.addOption(lO);
		options.addOption(sO);
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
		
		int splits = Integer.parseInt(cmd.getOptionValue("s"));
		
		String out = cmd.getOptionValue(outO.getOpt());
		
		if(!out.contains("$")){
			throw new RuntimeException("Output path should contain $");
		}
		
		
		int lines = -1; 
		if(cmd.hasOption("l")){
			lines = Integer.parseInt(cmd.getOptionValue("l"));
		} else{
			String in = cmd.getOptionValue(inO.getOpt());
			InputStream is = new FileInputStream(in);
			if(cmd.hasOption(ingzO.getOpt())){
				is = new GZIPInputStream(is);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
			
			System.err.println("Reading from "+in+" to count lines");
			
			lines = 0;
			while(br.readLine()!=null){
				lines ++;
				if(lines % TICKS == 10000){
					System.err.println("... read "+lines);
				}
			}
			
			br.close();
		}
		System.err.println("File has "+lines+" lines.");
		
		int splitSize = lines/splits;
		System.err.println("Each split will contain roughly "+splitSize+" lines.");
		

		
		String in = cmd.getOptionValue(inO.getOpt());
		InputStream is = new FileInputStream(in);
		if(cmd.hasOption(ingzO.getOpt())){
			is = new GZIPInputStream(is);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
		
		System.err.println("Reading from "+in);
		
		PrintWriter[] pwSplits = new PrintWriter[splits];
		
		int padding = Integer.toString(splits).length();
		
		for(int i=0; i< splits; i++){
			String padded = String.format("%0"+padding+"d", i);
			String outSplit = out.replace("$", padded);
			
			OutputStream os = new FileOutputStream(outSplit);
			if(cmd.hasOption(outgzO.getOpt())){
				os = new GZIPOutputStream(os);
			}
			pwSplits[i] = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os),"utf-8"));
		}
		
		System.err.println("Writing the splits");
		
		String line = null;
		int written = 0;
		int split = 0;
		int writtenSplit = 0;
		while((line=br.readLine())!=null){
			written ++;
			if(written % TICKS == 10000){
				System.err.println("... output "+written);
			}
			
			pwSplits[split].write(line);
			
			writtenSplit++;
			
			if((writtenSplit>splitSize && split!=(splits-1))){
				System.err.println("... written split size "+writtenSplit);
				writtenSplit = 0;
				split ++;
			}
		}
		System.err.println("... last split size "+writtenSplit);
		
		System.err.println("Finished! Output "+written+" lines");
		
		br.close();
		
		for(PrintWriter pwSplit: pwSplits){
			pwSplit.close();
		}
	}
}