package org.mdp.cli;

import java.lang.reflect.Method;

/**
 * Class for running one of many possible command line tasks
 * in the CLI package.
 * 
 * @author Aidan Hogan
 */
public class Main {
	
	private static final String PREFIX = "org.mdp.cli.";
	private static final String USAGE = "usage: "+Main.class.getName();

	/**
	 * Main method
	 * @param args Command line args, first of which is the utility to run
	 */
	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				StringBuffer sb = new StringBuffer();
				sb.append("missing <utility> arg where <utility> one of");
				sb.append("\n\t"+Head.class.getSimpleName()+": Copy first k lines from a file");
				sb.append("\n\t"+CountDuplicates.class.getSimpleName()+": Count consecutive duplicates in a file");
				sb.append("\n\t"+ExternalMergeSort.class.getSimpleName()+": Run an external merge-sort over a file");
				sb.append("\n\t"+ExtractNGrams.class.getSimpleName()+": Extract n-grams from a file");
				sb.append("\n\t"+RunNGramCountInMemory.class.getSimpleName()+": Count n-grams in a file in memory");
				sb.append("\n\t"+RunWordCountInMemory.class.getSimpleName()+": Count words in a file in memory");
				
				usage(sb.toString());
			}


			Class<? extends Object> cls = Class.forName(PREFIX + args[0]);

			Method mainMethod = cls.getMethod("main", new Class[] { String[].class });

			String[] mainArgs = new String[args.length - 1];
			System.arraycopy(args, 1, mainArgs, 0, mainArgs.length);

			long time = System.currentTimeMillis();
			
			mainMethod.invoke(null, new Object[] { mainArgs });

			long time1 = System.currentTimeMillis();

			System.err.println("time elapsed " + (time1-time) + " ms");
		} catch (Throwable e) {
			e.printStackTrace();
			usage(e.toString());
		}
	}

	private static void usage(String msg) {
		System.err.println(USAGE);
		System.err.println(msg);
		System.exit(-1);
	}
}
