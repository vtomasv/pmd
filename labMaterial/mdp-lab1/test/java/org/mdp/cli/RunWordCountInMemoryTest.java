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
@BenchmarkMethodChart(filePrefix = "RunWordCountInMemory-benchmark-barchart")
public class RunWordCountInMemoryTest {

	private static final String nameFile = "es-wiki-abstracts.txt.gz";
	private static final String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(RunWordCountInMemoryTest.nameFile).getPath();
	
	@Rule
	public BenchmarkRule benchmarkRun = new BenchmarkRule();
	
	@Before
	public void setUp() throws Exception {
		
		System.out.println(" ****** Inicio de pruebas ******");
		
	}
	
	@After
	public void finish() throws Exception {
		
		System.out.println(" ******  FIN de pruebas  ******");
		
	}

	@BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 0, concurrency = 3)
	@Test
	public void runWordCountInMemory() throws Exception  {
		
		String filePath = new URI(RunWordCountInMemoryTest.pathFileInput).normalize().getPath();		
		String[] args = {"-i", filePath , "-igz", "-k", "100"}; 
		
		RunWordCountInMemory.main(args);
	}

}
