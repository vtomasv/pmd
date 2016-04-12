package org.mdp.hadoop.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * 
 * @author vtomasv
 * Clase que permite obtener todos los actores que trabaron juntos generando un archivo que 
 * muestra 
 */
public class ActoresTrabajandoJuntosCount {
	
	public static String SPLIT_REGEX = "\t";
	
	
	/**
	 * 
	 * @author vtomasv
	 */
	public static class MapperDeActoresPelicula extends Mapper<Object, Text, Text, IntWritable>{

		private Text actoresKey = new Text();
		private IntWritable one = new IntWritable(1);

		
		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();
			String[] atributos = line.split(SPLIT_REGEX);
			
			actoresKey.set(atributos[0]);
			
			
			context.write(actoresKey, one);

		}

	}
	
	
	public static class ReducerDeActoresPelicula extends Reducer<Text, IntWritable, Text, IntWritable> {

		private IntWritable contador = new IntWritable(1);
		
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			int sum = 0;
			for(IntWritable value: values) {
				sum += value.get();
			}
			context.write(key, new IntWritable(sum));

		}
			
	}

	public static void main(String[] args)  throws Exception{
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: CitationCount <in> <out>");
			System.exit(2);
		}
		String inputLocation = otherArgs[0];
		String outputLocation = otherArgs[1];
		
		Job job = Job.getInstance(new Configuration());
	     
	    FileInputFormat.setInputPaths(job, new Path(inputLocation));
	    FileOutputFormat.setOutputPath(job, new Path(outputLocation));
	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(IntWritable.class);
	    
	    job.setMapperClass(MapperDeActoresPelicula.class);
	    //job.setCombinerClass(ReducerDeActoresPelicula.class);
	    job.setReducerClass(ReducerDeActoresPelicula.class);
	     
	    job.setJarByClass(ActoresTrabajandoJuntosCount.class);
		job.waitForCompletion(true);
	}

}
