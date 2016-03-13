package org.mdp.cli;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;

@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "ExtractNGrams-benchmark-barchart")
public class ExtractNGramsTest  {
	
	private static final String nameFile = "es-wiki-abstracts.txt.gz";
	private static final String nameFileOut_02 = "es-wiki-abstracts-2grams.txt.gz";
	private static final String nameFileOut_03 = "es-wiki-abstracts-3grams.txt.gz";
	private static final String nameFileOut_04 = "es-wiki-abstracts-4grams.txt.gz";
	
	
	private static final String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(ExtractNGramsTest.nameFile).getPath();


	
	
	@Rule
	public BenchmarkRule benchmarkRun = new BenchmarkRule();
	
	@Before
	public void setUp() throws Exception {
		
		System.out.println(" ****** Inicio de pruebas ******");
		
	}
	
	@After
	public void tearDown() throws Exception {
		
		System.out.println(" ******  FIN de pruebas  ******");
		
	}
	

	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void extractNGrams_2() throws Exception {
		
		String filePath = new URI(ExtractNGramsTest.pathFileInput).normalize().getPath();		
		String[] args = {"-i", filePath , 
						"-igz", "-n", "2", "-o", ExtractNGramsTest.nameFileOut_02, "-ogz"}; 
		
		ExtractNGrams.main(args);
		
	}
	
	
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void extractNGrams_3() throws Exception {
		
		String filePath = new URI(ExtractNGramsTest.pathFileInput).normalize().getPath();		
		String[] args = {"-i", filePath , 
						"-igz", "-n", "3", "-o", ExtractNGramsTest.nameFileOut_03, "-ogz"}; 
		
		ExtractNGrams.main(args);
		
	}
	
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void extractNGrams_4() throws Exception {
		
		String filePath = new URI(ExtractNGramsTest.pathFileInput).normalize().getPath();		
		String[] args = {"-i", filePath , 
						"-igz", "-n", "4", "-o", ExtractNGramsTest.nameFileOut_04, "-ogz"}; 
		
		ExtractNGrams.main(args);
		
	}

}
