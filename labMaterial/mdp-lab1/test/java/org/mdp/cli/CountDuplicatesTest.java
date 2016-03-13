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
@BenchmarkMethodChart(filePrefix = "CountDuplicates-benchmark-barchart")
public class CountDuplicatesTest {

	private static final String nameFileinput_02 = "es-wiki-abstracts-2grams-s.txt.gz";
	private static final String nameFileinput_03 = "es-wiki-abstracts-3grams-s.txt.gz";
	private static final String nameFileinput_04 = "es-wiki-abstracts-4grams-s.txt.gz";
	
	private static final String nameFileOutput_02 = "es-wiki-abstracts-2grams-s-c.txt.gz";
	private static final String nameFileOutput_03 = "es-wiki-abstracts-3grams-s-c.txt.gz";
	private static final String nameFileOutput_04 = "es-wiki-abstracts-4grams-s-c.txt.gz";
	
	private static final String nameFileOutput_orderCount_02 = "es-wiki-abstracts-2grams-s-c-o.txt.gz";
	private static final String nameFileOutput_orderCount_03 = "es-wiki-abstracts-3grams-s-c-o.txt.gz";
	private static final String nameFileOutput_orderCount_04 = "es-wiki-abstracts-4grams-s-c-o.txt.gz";

	
	private static final String nameFileOutput_order_last_02 = "es-wiki-abstracts-2grams-s-c-o-l.txt.gz";
	private static final String nameFileOutput_order_last_03 = "es-wiki-abstracts-3grams-s-c-o-l.txt.gz";
	private static final String nameFileOutput_order_last_04 = "es-wiki-abstracts-4grams-s-c-o-l.txt.gz";

	
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
	public void countDuplicatesTest_2() throws Exception {
		
		String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(CountDuplicatesTest.nameFileinput_02).getPath();

		String fileInput = new URI(pathFileInput).normalize().getPath();		
		
		String[] args = {"-i", fileInput , "-igz", 
				"-o", CountDuplicatesTest.nameFileOutput_02, "-ogz"}; 		
		
		CountDuplicates.main(args);
		
		
	}
	
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void countDuplicatesTest_3() throws Exception {
		
		//String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(CountDuplicatesTest.nameFileinput_03).getPath();

		//String fileInput = new URI(pathFileInput).normalize().getPath();		
		
		//String[] args = {"-i", fileInput , "-igz", 
		//		"-o", CountDuplicatesTest.nameFileOutput_03, "-ogz"}; 		
		
		//CountDuplicates.main(args);
		
		String pathFileInput_merge = Thread.currentThread().getContextClassLoader().getResource(CountDuplicatesTest.nameFileOutput_03).getPath();

		String fileInput_merge = new URI(pathFileInput_merge).normalize().getPath();	
		
		String[] args_Sort = {"-i", fileInput_merge , "-igz", 
				"-o", CountDuplicatesTest.nameFileOutput_orderCount_03, "-ogz",
				"-tmp",	".",
				"-r",
				"-b", "10000000"}; 		
		
		//ExternalMergeSort.main(args_Sort);
		
		
		String pathFileInput_merge_o = Thread.currentThread().getContextClassLoader().getResource(CountDuplicatesTest.nameFileOutput_orderCount_03).getPath();

		String fileInput_merge_o = new URI(pathFileInput_merge_o).normalize().getPath();	
		
		String[] args_Sort_o = {"-i", fileInput_merge_o , "-igz", 
				"-o", CountDuplicatesTest.nameFileOutput_order_last_03, "-ogz",
				"-k", "10"}; 	
		
		Head.main(args_Sort_o);
		
		
	}
	
	//@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	//@Test
	public void countDuplicatesTest_4() throws Exception {
		
		String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(CountDuplicatesTest.nameFileinput_04).getPath();

		String fileInput = new URI(pathFileInput).normalize().getPath();		
		
		String[] args = {"-i", fileInput , "-igz", 
				"-o", CountDuplicatesTest.nameFileOutput_order_last_03, "-ogz",
				"-k", "10"}; 		
		
		Head.main(args);
		
	}

}
