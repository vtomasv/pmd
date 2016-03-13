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
@BenchmarkMethodChart(filePrefix = "ExternalMergeSort-benchmark-barchart")
public class ExternalMergeSortTest {

	private static final String nameFileinput_02 = "es-wiki-abstracts-2grams.txt.gz";
	private static final String nameFileinput_03 = "es-wiki-abstracts-3grams.txt.gz";
	private static final String nameFileinput_04 = "es-wiki-abstracts-4grams.txt.gz";
	
	private static final String nameFileOutput_02 = "es-wiki-abstracts-2grams-s.txt.gz";
	private static final String nameFileOutput_03 = "es-wiki-abstracts-3grams-s.txt.gz";
	private static final String nameFileOutput_04 = "es-wiki-abstracts-4grams-s.txt.gz";
	
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
	

	//@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	//@Test
	public void externalMergeSort_2() throws Exception {
		
		String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(ExternalMergeSortTest.nameFileinput_02).getPath();

		String fileInput = new URI(pathFileInput).normalize().getPath();		
		
		String[] args = {"-i", fileInput , "-igz", 
				"-o", ExternalMergeSortTest.nameFileOutput_02, "-ogz",
				"-tmp",	".",
				"-b", "10000000"}; 		
		
		ExternalMergeSort.main(args);
		
	}
	
	
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void externalMergeSort_3() throws Exception {
		
		String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(ExternalMergeSortTest.nameFileinput_03).getPath();

		String fileInput = new URI(pathFileInput).normalize().getPath();		
		
		String[] args = {"-i", fileInput , "-igz", 
				"-o", ExternalMergeSortTest.nameFileOutput_03, "-ogz",
				"-tmp",	".",
				"-b", "10000000"}; 	
		
		ExternalMergeSort.main(args);
		
	}
	
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void externalMergeSort_4() throws Exception {
		
		String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(ExternalMergeSortTest.nameFileinput_04).getPath();

		String fileInput = new URI(pathFileInput).normalize().getPath();		
		
		String[] args = {"-i", fileInput , "-igz", 
				"-o", ExternalMergeSortTest.nameFileOutput_04, "-ogz",
				"-tmp",	".",
				"-b", "10000000"}; 	
		
		ExternalMergeSort.main(args);
		
	}

}
