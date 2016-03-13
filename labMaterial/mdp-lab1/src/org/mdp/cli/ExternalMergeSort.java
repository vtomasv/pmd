package org.mdp.cli;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.TreeSet;
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
 * Main method to run an external merge sort.
 * Batches of lines from the input are read into memory and sorted.
 * Batches are written to individual files.
 * A merge-sort is applied over the individual files, creating the final sorted output.
 * The bigger the batches (in general), the faster the sort.
 * But get too close to the Heap limit and you'll run into trouble.
 * 
 * @author Aidan
 */
public class ExternalMergeSort {
	
	public static String DEFAULT_TEMP_DIR = "tmp";
	public static String DEFAULT_TEMP_SUBDIR_PREFIX = "t";
	
	public static String BATCH_FILE_NAME_PREFIX = "batch-";
	public static String BATCH_FILE_NAME_SUFFIX = ".txt";
	public static String BATCH_FILE_GZIPPED_NAME_SUFFIX = ".gz";
	
	public static boolean GZIP_BATCHES = true;
	
	public static ReverseOrderComparator<String> REVERSE_STRINGS = new ReverseOrderComparator<String>();
	public static ReverseOrderComparator<StringWithNumber> REVERSE_STRINGS_WITH_NUMBER = new ReverseOrderComparator<StringWithNumber>();
	
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
		
		Option bO = new Option("b", "size of batches to use");
		bO.setArgs(1);
		bO.setRequired(true);
		
		Option rO = new Option("r", "reverse (descending) order");
		rO.setArgs(0);
		
		Option tmpO = new Option("tmp", "temporary folder to store batch files (default: 'tmp/')");
		tmpO.setArgs(1);
		
		Option helpO = new Option("h", "print help");
				
		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
		options.addOption(outO);
		options.addOption(outgzO);
		options.addOption(bO);
		options.addOption(rO);
		options.addOption(tmpO);
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
		
		// get the batch size
		int batchSize = Integer.parseInt(cmd.getOptionValue(bO.getOpt()));
		if(batchSize<=0){ 
			batchSize = Integer.MAX_VALUE;
		}
		System.err.println("Using a batch size of "+batchSize+" for the sort");
		
		// get the temporary directory
		// to store batch files in (if given)
		String tmpParent = DEFAULT_TEMP_DIR;
		if(cmd.hasOption(tmpO.getOpt())){
			tmpParent = cmd.getOptionValue(tmpO.getOpt());
		}
		
		// set the reverse flag
		boolean reverseOrder = cmd.hasOption(rO.getOpt());
		
		// call the method that does the hard work
		// time it as well!
		long b4 = System.currentTimeMillis();
		externalMergeSort(in, gzIn, out, gzOut, batchSize, reverseOrder, tmpParent);
		System.err.println("Overall Runtime: "+(System.currentTimeMillis()-b4)/1000+" seconds");
	}
	
	public static void externalMergeSort(String in, boolean gzIn, String out, boolean gzOut, 
			int batchSize, boolean reverseOrder, String tmpFolderParent) throws IOException{
		// open a random sub-folder for batches so 
		// that two parallel sorts are unlikely to overwrite
		// each other
		String tmpFolder = createRandomFreshSubdir(tmpFolderParent);
		
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
		
		// batch the data into small sorted files and
		// return the batch file names
		long b4 = System.currentTimeMillis();
		ArrayList<String> batches = writeSortedBatches(br, tmpFolder, batchSize, reverseOrder);
		System.err.println("Batch Runtime: "+(System.currentTimeMillis()-b4)/1000+" seconds");
		br.close();
		
		// merge-sort the batches into the output file
		b4 = System.currentTimeMillis();
		mergeSortedBatches(batches, pw, reverseOrder);
		System.err.println("Merge Runtime: "+(System.currentTimeMillis()-b4)/1000+" seconds");
		pw.close();
	}
	
	/**
	 * Break the input into small sorted files containing
	 * a maximum of batchSize lines each.
	 * 
	 * @param in Reader over input file
	 * @param tmpFolder A folder in which batches can be written
	 * @param batchSize Maximum size for a batch
	 * @param reverseOrder If sorting should be in descending order
	 * @return
	 * @throws IOException
	 */
	private static ArrayList<String> writeSortedBatches(BufferedReader in,
			String tmpFolder, int batchSize, boolean reverseOrder) throws IOException {
		// this stores the file names of the batches produced ...
		ArrayList<String> batchNames = new ArrayList<String>();
		int batchId = 0;

		// this stores the lines of the file for sorting
		ArrayList<String> lines = new ArrayList<String>(batchSize);
		
		boolean done = false;
		while(!done){
			String line = in.readLine();
			if(line!=null){
				lines.add(line);
			} else {
				done = true;
			}
			
			// if the batch is full or its the last line
			// of the input, write the batch to file
			if(lines.size()==batchSize || (done && !lines.isEmpty())){
				batchId ++;
				
				// if reverse order is set, then reverse the order
				if(reverseOrder){
					Collections.sort(lines, new ReverseOrderComparator<String>());
				} else{
					Collections.sort(lines);
				}
				
				// we will return the names of the batch files later
				batchNames.add(writeBatch(lines, tmpFolder, batchId));
				lines.clear();
			}
		}
		
		return batchNames;
	}
	
	/**
	 * Opens a batch file and writes all the lines to it.
	 * @param lines
	 * @param tmpFolder
	 * @param batchId
	 * @return The filename of the batch.
	 * @throws IOException
	 */
	private static String writeBatch(Collection<String> lines, String tmpFolder, int batchId) throws IOException{
		String batchFileName = getBatchFileName(tmpFolder, batchId);
		System.err.println("Opening batch at "+batchFileName+" to write "+lines.size()+" lines");
		PrintWriter batch = openBatchFileForWriting(batchFileName);
		
		for(String l:lines)
			batch.println(l);
		
		batch.close();
		System.err.println("... closing batch.");
		return batchFileName;
	}
	
	/**
	 * Merge sorted batches into one file.
	 * 
	 * @param batches The filenames of the batches to merge
	 * @param out The output to write the merged data
	 * @param reverseOrder If the ordering should be descending
	 * @throws IOException
	 */
	private static void mergeSortedBatches(ArrayList<String> batches,
			PrintWriter out, boolean reverseOrder) throws IOException {
		
		// inputs for all the sorted batches
		BufferedReader[] brs = new BufferedReader[batches.size()];
		for(int i=0; i<brs.length; i++){
			brs[i] = openBatchFileForReading(batches.get(i));
		}
		
		/* *************************************
		 * Con el lema de divide y venceras ya se ha dividido
		 * el gran archivo en archivos pequennos y ademas estos estan ordenados
		 * por lo que solo requeriria sacar uno de cada archivo e insertarlo 
		 * de forma irdenada en una estructura que permita esto :) 
		 * Aca es donde esta la pregunta en que estructura puedo intertar de forma 
		 * ordenada y eficiente? 
		 * Tenemos dos TreeSet y ConcurrentSkipListMap en este caso usaremos TreeSet
		 * dado que no existiran hilos concurrentes tratando de insertar un dato al 
		 * mismo tiempo en la estructura. 
		 * *************************************/

		// 	Inicializamos nuestro arbol 		
		//  Se utiliza StringWithNumber para poder almacenar la linea y la cantidad repeticiones
		TreeSet<StringWithNumber>  ts = null; 
		
		// Si se indica que debe ser ordenado en forma inversa el comparador que utilizamos 
		// es otro ReverseOrderComparator, sino utiliza el metodo StringWithNumber.compareTo
		if (reverseOrder)
		{
			ts = new TreeSet<StringWithNumber>( new ReverseOrderComparator<StringWithNumber>());
		}
		else
		{
			ts = new TreeSet<StringWithNumber>();
			
		}

		// El algoritmo busca en inicio sacar el top de cada archivo es decir la primera linea de cada archivo
		// Luego al ingresarla al arbol tendremos un arbol ordenado.
		// Dentro del arbol se gurada el objeto StringWithNumber que nos indica el string que esta indexado mas
		// el indice del archivo donde saco el string, por lo tanto si saca el primero sabra que ese es el dato 
		// mas ordenado y de que archivo lo saco. 
		// Luego es obvio que debe seguir sacando de ese archivo porque es el mas ordenado (sino del arbol saldria otro)
		// En caso que dos archivos tengan el mismo contenido no importa porque se ordenara nuevamente el arbol al sacar
		// el ultiumo elelemto ordenado del primer archivo. 
		for (int i = 0; i < brs.length; i++) {
			
			String line = brs[i].readLine();
			if (line != null)
			{
				ts.add(new StringWithNumber(line, i));
			}
			
		}
		
		// Una vez lleno el arbol se procede a crear el archivo ordenado (mezclado)
		// por lo tanto se saca un dato desde arbol para luego ser ingresado al archivo,
		// como es el dato mas ordenado de donde salio el archivo tambien es el mas ordenado
		// por lo tanto se saca de ese archivo el siguiente dato y se pone en el arbol para
		// ser nuevamente ordenado. 
		while (!ts.isEmpty())
		{
			StringWithNumber swn = ts.pollFirst();
			// Solo para que veamos que se esta haciendo algo 
			System.out.println("Del Archivo : "+ swn.getNumber() + "  se pondra el dato: " + swn.getString() );
			
			out.println(swn.getString());
			
			// Se pasa a la siguiente linea del archivo ordenado para sacar el siguiente dato. 
			// y luego esa linea se pone en el arbol para ser ordenado nuevamente y proceder a 
			// realizar todo otra vez hasta que no queden mas elementos en ninguno de los archivos. 
			String line = brs[swn.getNumber()].readLine();
			if (line != null)
			{
				
				ts.add(new StringWithNumber(line,  swn.getNumber()));
			}
			
		}
		
		
		
	}

	/**
	 * Get a batch file name with the given directory and batch number
	 * 
	 * @param dir
	 * @param batchNumber
	 * @return
	 */
	private static String getBatchFileName(String dir, int batchNumber){
		String fileName = dir+"/"+BATCH_FILE_NAME_PREFIX+batchNumber+BATCH_FILE_NAME_SUFFIX;
		if(GZIP_BATCHES)
			fileName = fileName+BATCH_FILE_GZIPPED_NAME_SUFFIX;
		return fileName;
	}
	
	/**
	 * Opens a PrintWriter for the batch filename
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static PrintWriter openBatchFileForWriting(String fileName) throws IOException{
		OutputStream os = new FileOutputStream(fileName);
		if(GZIP_BATCHES){
			os = new GZIPOutputStream(os);
		}
		return new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os),"utf-8"));
	}
	
	/**
	 * Opens a BufferedReader to read from a batch.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static BufferedReader openBatchFileForReading(String fileName) throws IOException{
		InputStream os = new FileInputStream(fileName);
		if(GZIP_BATCHES){
			os = new GZIPInputStream(os);
		}
		return new BufferedReader(new InputStreamReader(os,"utf-8"));
	}

	/**
	 * Creates a random sub-directory that doesn't already exist
	 * 
	 * Makes sure different runs don't overwrite each other
	 * 
	 * @param inDir Parent directory
	 * @return
	 */
	public static final String createRandomFreshSubdir(String inDir){
		boolean done = false;
		String subDir = null;
		
		while(!done){
			Random r = new Random();
			int rand = Math.abs(r.nextInt());
			subDir = inDir+"/"+DEFAULT_TEMP_SUBDIR_PREFIX+rand+"/";
			File subDirF = new File(subDir);
			if(!subDirF.exists()){
				subDirF.mkdirs();
				done = true;
			}
		}
		return subDir;
	}
	
	
	public static class ReverseOrderComparator<E extends Comparable<? super E>> implements Comparator<E>{
		/**
		 * Creates a reverse order comparator for any object that implements
		 * Comparable.
		 * 
		 * @author Aidan
		 *
		 * @param <E>
		 */
		
		
		/**
		 * Returns o1.compareTo(o2) * -1
		 */
		public int compare(E o1, E o2) {
			return o1.compareTo(o2)*-1;
		}
		
	}
}