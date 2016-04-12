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
public class ActoresTrabajandoJuntos {
	
	public static String SPLIT_REGEX = "\t";
	
	
	/**
	 * 
	 * @author vtomasv
	 */
	public static class MapperDeActoresPelicula extends Mapper<Object, Text, Text, Text>{

		private Text peliculaKey = new Text();
		private Text actoreValue = new Text();

		
		@Override
		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();
			String[] atributos = line.split(SPLIT_REGEX);
			
			
			if ( (atributos.length > 5) && "THEATRICAL_MOVIE".equals(atributos[4]) ) 
			{
				String actor = atributos[0];
				String pelicula = atributos[1];
				String anio = atributos[2];
				String numeroPelicula = atributos[3];
				
				peliculaKey.set(pelicula.trim().toLowerCase() + "##" 
				+ anio.trim().toLowerCase() + "##" 
				+ numeroPelicula.trim().toLowerCase()) ;
				actoreValue.set(actor);

				context.write(peliculaKey, actoreValue);

			}		
		}

	}
	
	
	public static class ReducerDeActoresPelicula extends Reducer<Text, Text, Text, IntWritable> {

		private IntWritable contador = new IntWritable(1);
		
		protected void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			
			ArrayList<String> listaDeActores = new ArrayList<String>();

			
			for(Text value: values) 
			{
				listaDeActores.add(value.toString());
			}
			
			Collections.sort(listaDeActores);
			
			for (int i=0;i<listaDeActores.size();i++) {
				for (int j=i+1;j<listaDeActores.size();j++)
					
					context.write(new Text(listaDeActores.get(i) +"##" + listaDeActores.get(j)), contador);
			}
			
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
	    job.setMapOutputValueClass(Text.class);
	    
	    job.setMapperClass(MapperDeActoresPelicula.class);
	    //job.setCombinerClass(ReducerDeActoresPelicula.class);
	    job.setReducerClass(ReducerDeActoresPelicula.class);
	     
	    job.setJarByClass(ActoresTrabajandoJuntos.class);
		job.waitForCompletion(true);
	}

}
