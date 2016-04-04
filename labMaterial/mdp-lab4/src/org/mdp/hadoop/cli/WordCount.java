package org.mdp.hadoop.cli;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Java class to run a remote Hadoop word count job.
 * 
 * Contains the main method, an inner Reducer class 
 * and an inner Mapper class.
 * 
 * @author Aidan
 */
public class WordCount {
	
	/**
	 * Use this with line.split(SPLIT_REGEX) to get fairly nice
	 * word splits.
	 */
	public static String SPLIT_REGEX = "[^\\p{L}'-]+";
	

	
	/**
	 * This is the Mapper Class. This sends key-value pairs to different machines
	 * based on the key.
	 * 
	 * Remember that the generic is Mapper<InputKey, InputValue, MapKey, MapValue>
	 * 
	 * InputKey we don't care about (a LongWritable will be passed as the input
	 * file offset, but we don't care; we can also set as Object)
	 * 
	 * InputKey will be Text: a line of the file
	 * 
	 * MapKey will be Text: a word from the file
	 * 
	 * MapValue will be IntWritable: a count: emit 1 for each occurrence of the word
	 * 
	 * @author Aidan
	 *
	 */
	public static class WordCountMapper extends Mapper<Object, Text, Text, IntWritable>{

		
		/**
		 * Una linea leida
		 */
		private final IntWritable one = new IntWritable(1);
		private Text word = new Text();
		
		
		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			
			String line = value.toString();
			String[] words = line.split(SPLIT_REGEX);
			
			for (int i = 0; i < words.length; i++) {
				if (!words[i].trim().toLowerCase().isEmpty()){
					word.set(words[i].trim().toLowerCase());
					context.write(word, one);
				}
			}

			
		}

	}

	/**
	 * This is the Reducer Class.
	 * 
	 * This collects sets of key-value pairs with the same key on one machine. 
	 * 
	 * Remember that the generic is Reducer<MapKey, MapValue, OutputKey, OutputValue>
	 * 
	 * MapKey will be Text: a word from the file
	 * 
	 * MapValue will be IntWritable: a count: emit 1 for each occurrence of the word
	 * 
	 * OutputKey will be Text: the same word
	 * 
	 * OutputValue will be IntWritable: the final count
	 * 
	 * @author Aidan
	 *
	 */
	public static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable>{

		protected void reduce(Text key, Iterable<IntWritable> values, 	Context output)
				throws IOException, InterruptedException {
						
						int sum = 0;
						for(IntWritable value: values) {
							sum += value.get();
						}
						output.write(key, new IntWritable(sum));
		}

	}
	
	
	// Agregamos el super MAP y Su reduce para ordenar el contador 
	
	public static class Map1 extends Mapper<Object, Text, IntWritable, Text> {

	        public void map(Object key, Text value, OutputCollector<IntWritable, Text> collector, Reporter arg3) throws IOException {
	            String line = value.toString();
	            StringTokenizer stringTokenizer = new StringTokenizer(line);
	            {
	                int number = 999;
	                String word = "empty";

	                if (stringTokenizer.hasMoreTokens()) {
	                    String str0 = stringTokenizer.nextToken();
	                    word = str0.trim();
	                }

	                if (stringTokenizer.hasMoreElements()) {
	                    String str1 = stringTokenizer.nextToken();
	                    number = Integer.parseInt(str1.trim());
	                }
	                collector.collect(new IntWritable(number), new Text(word));
	            }

	        }

	    }

	public static  class Reduce1 extends Reducer<IntWritable, Text, IntWritable, Text> {

	        public void reduce(IntWritable key, Iterator<Text> values, OutputCollector<IntWritable, Text> arg2, Reporter arg3) throws IOException {
	            while ((values.hasNext())) {
	                arg2.collect(key, values.next());
	            }
	        }

	    }

	
	// FIN

	/**
	 * Main method that sets up and runs the job
	 * 
	 * @param args First argument is input, second is output
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: WordCount <in> <out>");
			System.exit(2);
		}
		String inputLocation = otherArgs[0];
		String outputLocation = otherArgs[1];
		
		Job job = Job.getInstance(new Configuration());
	     

	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(IntWritable.class);
	    
	    job.setMapperClass(WordCountMapper.class);
	    job.setCombinerClass(WordCountReducer.class);
	    job.setReducerClass(WordCountReducer.class);
	    
	    job.setOutputFormatClass(TextOutputFormat.class);
	     
	    FileInputFormat.setInputPaths(job, new Path(inputLocation));
	    FileOutputFormat.setOutputPath(job, new Path(outputLocation));
	    
	    job.setJarByClass(WordCount.class);
	    job.submit();
		job.waitForCompletion(true);
		
		
		// Ahora configuramos el Job que ordena el resultado anterior 
		
		Job job2 = Job.getInstance(new Configuration());
	     

	    job2.setInputFormatClass(TextInputFormat.class);
	    job2.setOutputKeyClass(Text.class);
	    job2.setOutputValueClass(IntWritable.class);

	    
	    job2.setMapperClass(Map1.class);
	    job2.setCombinerClass(Reduce1.class);
	    job2.setReducerClass(Reduce1.class);
	     
	    FileInputFormat.setInputPaths(job2, new Path(outputLocation+"part-r-00000")); // Se que no es correcto pero solo por simplicidad :) 
	    FileOutputFormat.setOutputPath(job2, new Path(outputLocation+"segundo/"));
	    
	    job2.setJarByClass(WordCount.class);
	    job2.submit();
	    job2.waitForCompletion(true);
	    

	    
	    

		
	}	
}