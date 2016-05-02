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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.util.NxUtil;

/**
 * Main method to extract plain-text abstracts and titles from DBpedia.
 * 
 * 
 * YOU CAN IGNORE THIS CLASS :)
 * 
 * @author Aidan
 */
public class ExtractTitleAndAbstract {
	
	public static Resource ABSTRACT_PREDICATE = new Resource("http://dbpedia.org/ontology/abstract");
	public static String OLD_PREFIX = "http://es.dbpedia.org/resource/";
	public static String NEW_PREFIX = "http://es.wikipedia.org/wiki/";
	
	public static int TICKS = 100000;
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
		Option inO = new Option("i", "input file");
		inO.setArgs(1);
		inO.setRequired(true);
		
		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);
		
		Option outO = new Option("o", "output file");
		outO.setArgs(1);
		
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
		
		String in = cmd.getOptionValue(inO.getOpt());
		InputStream is = new FileInputStream(in);
		if(cmd.hasOption(ingzO.getOpt())){
			is = new GZIPInputStream(is);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
		
		System.err.println("Reading from "+in);
		
		NxParser nxp = new NxParser(br);
		
		String out = cmd.getOptionValue(outO.getOpt());
		OutputStream os = new FileOutputStream(out);
		if(cmd.hasOption(outgzO.getOpt())){
			os = new GZIPOutputStream(os);
		}
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os),StandardCharsets.UTF_8));
		
		System.err.println("Writing to "+out);
		
		int read = 0;
		int written = 0;
		int skipped = 0;
		while(nxp.hasNext()){
			read++;
			Node[] next = nxp.next();
			if(next[1].equals(ABSTRACT_PREDICATE)){
				String suffix = getSuffix(next[0].toString());
				if(suffix!=null){
					String title = getTitle(suffix);
					String abst = unescapeNxExceptTab(next[2].toString());
					pw.println(NEW_PREFIX+suffix+"\t"+title+"\t"+abst);
					written++;
				} else{
					skipped++;
				}
			}
			
			if(read%TICKS==0){
				System.err.println("Read "+read+" and written "+written+" and skipped "+skipped);
			}
		}
		
		System.err.println("Finished! Read "+read+" and written "+written+" and skipped "+skipped);
		
		pw.close();
		br.close();
	}
	
	public static String unescapeNxExceptTab(String str){
		return NxUtil.unescape(str).replaceAll("\t", "\\t");
	}
	
	public static String getSuffix(String uri){
		if(uri.startsWith(OLD_PREFIX)){
			return unescapeNxExceptTab(uri.substring(OLD_PREFIX.length()));
		}
		return null;
	}
	
	public static String getTitle(String suffix) throws UnsupportedEncodingException{
		return URLDecoder.decode(suffix,StandardCharsets.UTF_8.displayName()).replaceAll("_"," ");
	}
}