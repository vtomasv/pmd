# Laboratorio 4 

Hola! este es nuevo laboratorio de contar palabras, se que parece un poco aburrido y que esta lleno en internet de este tipo de demos, pero la idea es poder adquirir ciertos conceptos del procesamiento masivo. Para su tranquilidad o ansiedad tener la capacidad de contar palabras en forma eficiente nos da habilidades para poder contar n-gramas de forma eficiente y eso... bingo nos da herramientas para poder predecir por ejemplo como escriben las personas, solo con analizar un conjunto de correos electrónicos o simplemente analizar un conjunto de chat podemos ayudar a que los teclados inteligentes realicen mejor su tarea prediciendo cual es la siguiente palabra que queremos escribir! 

Vemos en este ejercicio como utilizar un conjunto de tres nodos de Hadoop con el fin de entender en forma básica como funciona el sistema de archivos distribuidos y despues como este nos provee herramientas para el procesamiento de datos en forma distribuida! 

## Instalando el ambiente para las pruebas

Si pensamos en el mundo real, poder instalar una infraestructura Hadoop es necesario tener maquinas físicas y algunas pre condiciones que claramente muchos a la hora de partir con este laboratorio a lo mejor no tenemos disponibles, así que vamos a hacerlo mas sencillo, vamos a simular que tenemos una infraestructura, minima para poder correr un file system distribuido! 

#### Docker 

Para esto vamos a usar [docker](https://www.docker.com/) esta plataforma de virtualizacion no permitirá crear un cluster Hadoop de tres nodos casi de forma inmediata. Gracias a la colaboración de la comunidad y en particular de ***alvinhenrick*** tenemos disponibles estas maquinas de docker en el siguiente [repositorio](https://github.com/alvinhenrick/hadoop-mutinode)

### Arquitectura de Hadoop que vamos a utilizar 

Para este lab vamos a usar las siguientes imágenes disponibles: 

1. serf-dnsmasq
2. hadoop-base
3. hadoop-master
4. hadoop-slave.



#### serf-dnsmasq

Es una imagen base con ubuntu 15.04 dentro tiene un servidor de nombres (DNS) el cual nos permite poder tener las maquinas con nombres en vez de usar IP, esto facilita mucho las actividades de armado de cluster. 

#### hadoop-base

Esta imagen es la base de todos los componentes usados por Hadoop, dentro de la imgen esta dnsmasq, openjdk y un servidor ssh para poder conectarnos a las maquinas openssh-server. Adicionalmente esta vim por si necesitamos modificar algun archivo y obvio Hadoop 2.3.0 esta instalado. 

#### hadoop-master

Esta es la imagen configurada con el nodo master de hadoop. 

#### hadoop-slave

Esta es la imagen configurada con el nodo esclavo de hadoop, lo interesante es que podemos tener de estos la cantidad que queramos lo que nos da la facilidad de crear un cluster con la cantidad de nodos que nos plazca! Gracias  [***alvinhenrick***](https://github.com/kiwenlau/). 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/ArquitecturaHadoop.jpg"  width="60%" height="60%">
</p>

### bajar imágenes de Hadoop con Docker

Depende el sistema operativo que esten usando puede ser necesario correr esto como root, sin embargo el comando es el mismo siempre. 

```
docker pull kiwenlau/hadoop-master:0.1.0
docker pull kiwenlau/hadoop-slave:0.1.0
docker pull kiwenlau/hadoop-base:0.1.0
docker pull kiwenlau/serf-dnsmasq:0.1.0
```

Puedes ver si las imágenes se bajaron con el siguiente comando

```
sudo docker images
```

Tendrías que ver algo parecido a esto 

```
REPOSITORY                TAG       IMAGE ID        CREATED         VIRTUAL SIZE
kiwenlau/hadoop-slave     0.1.0     d63869855c03    17 hours ago    777.4 MB
kiwenlau/hadoop-master    0.1.0     7c9d32ede450    17 hours ago    777.4 MB
kiwenlau/hadoop-base      0.1.0     5571bd5de58e    17 hours ago    777.4 MB
kiwenlau/serf-dnsmasq     0.1.0     09ed89c24ee8    17 hours ago    206.7 MB

```

Despues es necesario clonar el siguiente [repo](https://github.com/kiwenlau/hadoop-cluster-docker)

```
git clone https://github.com/kiwenlau/hadoop-cluster-docker
```
En este punto, lo mismo tener en cuenta el sistema operativo, a lo mejor tienen que editarlo y retirar la palabra *sudo* antes de cada instrucción de docker sino lo corren y no da problemas están listos! 

``` 
cd hadoop-cluster-docker
./start-container.sh
```

***Nota: si quisieras iniciar un cluster con mas nodos solo envía el parámetro de la cantidad de nodos al comando start-container.sh x y listo!***

La salida debería ser algo parecido a esto

```
start master container...
start slave1 container...
start slave2 container...
root@master:~#
```





Ahora tenemos 3 contenedores corriendo 1 es el master de hadoop y dos esclavos listos para ser iniciados y así procesar a su placer el codigo que creas necesario! 

### Ver estado del cluster

Ahora podemos ver el estado del cluster, el servidor de nombres se demora alrededor de un 1 minuto en iniciar despues de eso podemos usar el siguiente comando ```serf members```para ver la salud del cluster. 

La salida se parecería a esto 

```
master.kiwenlau.com  172.17.0.65:7946  alive  
slave1.kiwenlau.com  172.17.0.66:7946  alive  
slave2.kiwenlau.com  172.17.0.67:7946  alive
```

Vista del Cluster arriba 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab4_2.png"  width="60%" height="60%">
</p>


Una vez que están todos arribas podemos entrar a cualquiera de los nodos con la siguiente instrucción 
	
```
ssh slave2.kiwenlau.com
```

La salida debe ser parecida a esto 

```
Warning: Permanently added 'slave2.kiwenlau.com,172.17.0.67' (ECDSA) to the list of known hosts.
Welcome to Ubuntu 15.04 (GNU/Linux 3.13.0-53-generic x86_64)
 * Documentation:  https://help.ubuntu.com/
The programs included with the Ubuntu system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.
Ubuntu comes with ABSOLUTELY NO WARRANTY, to the extent permitted by
applicable law.
root@slave2:~#
```


### Inicio de Hadoop 

Iniciamos Hadoop de la siguiente manera  ( si entramos a alguno de los nodos simplemente nos salimos )

``` 
./start-hadoop.sh

```
Con eso estamos listos con Hadoop Iniciado! 

## Laboratorio 

Con este  cluster listo ahora vamos a realizar la actividad de crear un contador de palabras!

#### Actividades previas 

Lo primero que hacemos es ver la saludo de nuestro sistema distribuido de archivos con el siguiente comando 

```
hdfs dfsadmin -report
```

Salida

```
Configured Capacity: 58521268224 (54.50 GB)
Present Capacity: 48446341120 (45.12 GB)
DFS Remaining: 48445751296 (45.12 GB)
DFS Used: 589824 (576 KB)
DFS Used%: 0.00%
Under replicated blocks: 0
Blocks with corrupt replicas: 0
Missing blocks: 0

-------------------------------------------------
Datanodes available: 3 (3 total, 0 dead)
....
```
Ahora si todo esta ok, podemos listar directorios distribuidos! 

```
hdfs dfs -ls / 

```
en nuestro caso la salida sera 

```
Found 2 items
drwx------   - root supergroup          0 2016-04-02 21:23 /tmp
drwxr-xr-x   - root supergroup          0 2016-04-02 21:23 /user
```

Ahora según las instrucciones del lab creamos nuestro propio directorio para poder poner nuestras cosas! 

```
root@master:~# hdfs dfs -mkdir /uhadoop        
root@master:~# hdfs dfs -mkdir /uhadoop/vtomasv
```

Creamos los dos directorios porque en nuestras maquinas no existía ninguno de los dos

en la ubicación actual creamos una zona segura solo para ordenarnos

```
root@master:~# mkdir vtomasv
root@master:~# cd vtomasv/
root@master:~/vtomasv# ls
root@master:~/vtomasv# 
```

Ahora copiamos los datos necesarios para poder correr el contador de palabras desde nuestra maquina a una de las maquinas del cluster para poder subirla a sistema de archivos distribuido

```
docker exec -i bf419706a698 sh -c 'cat > /root/vtomasv/es-wiki-abstracts.txt.gz' < ./es-wiki-abstracts.txt.gz
```

Ahora copiamos este archivo a nuestro FS distribuido 

```
hdfs dfs -copyFromLocal /root/vtomasv/es-wiki-abstracts.txt.gz /uhadoop/vtomasv/
```
Ahora podemos ver si quedo guardado de forma correcta

```
root@master:~/vtomasv# hdfs dfs -ls /uhadoop/vtomasv 
Found 1 items
-rw-r--r--   3 root supergroup  135350527 2016-04-04 02:39 /uhadoop/vtomasv/es-wiki-abstracts.txt.gz
root@master:~/vtomasv# 
```

### Ahora completamos la clase ***WordCount***



#### Mapper

Creamos un Mapper que nos permite dividir la linea de un archivo en palabras, luego ponerlas en un map para poder contarlas

```Java 
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
```

#### Reduce 
Ahora creamos el Reduce para poder sumar todas las palabras encontradas

```
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
```

#### Main

Por ultimo creamos un método Main que nos permita ejecutar esta clase en el cluser hadoop 

```
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
	     
	    FileInputFormat.setInputPaths(job, new Path(inputLocation));
	    FileOutputFormat.setOutputPath(job, new Path(outputLocation));
	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(IntWritable.class);
	    
	    job.setMapperClass(WordCountMapper.class);
	    job.setCombinerClass(WordCountReducer.class);
	    job.setReducerClass(WordCountReducer.class);
	     
	    job.setJarByClass(WordCount.class);
		job.waitForCompletion(true);
	}	
```

Ahora creamos el ejecutable y corremos, simplemente ejecutamos el build.xml de ant que se encuentra en el proyecto. 

```
docker exec -i bf419706a698 sh -c 'cat > /root/vtomasv/mdp-lab4,jar' < pmd/labMaterial/mdp-lab4/dist/mdp-lab4.jar 
```
Ahora corremos el Jar!!

```
root@master:~/vtomasv# hadoop jar /root/vtomasv/mdp-lab4,jar WordCount /uhadoop/vtomasv/es-wiki-abstracts.txt.gz /uhadoop/vtomasv/wc/
16/04/04 02:51:17 INFO client.RMProxy: Connecting to ResourceManager at master.kiwenlau.com/172.17.0.1:8040
16/04/04 02:51:17 WARN mapreduce.JobSubmitter: Hadoop command-line option parsing not performed. Implement the Tool interface and execute your application with ToolRunner to remedy this.
16/04/04 02:51:18 INFO input.FileInputFormat: Total input paths to process : 1
16/04/04 02:51:18 INFO mapreduce.JobSubmitter: number of splits:1
16/04/04 02:51:18 INFO mapreduce.JobSubmitter: Submitting tokens for job: job_1459632208432_0002
16/04/04 02:51:18 INFO impl.YarnClientImpl: Submitted application application_1459632208432_0002
16/04/04 02:51:18 INFO mapreduce.Job: The url to track the job: http://master.kiwenlau.com:8088/proxy/application_1459632208432_0002/
16/04/04 02:51:18 INFO mapreduce.Job: Running job: job_1459632208432_0002
16/04/04 02:51:30 INFO mapreduce.Job: Job job_1459632208432_0002 running in uber mode : false
16/04/04 02:51:30 INFO mapreduce.Job:  map 0% reduce 0%
16/04/04 02:51:43 INFO mapreduce.Job:  map 5% reduce 0%
16/04/04 02:51:46 INFO mapreduce.Job:  map 7% reduce 0%
16/04/04 02:51:49 INFO mapreduce.Job:  map 9% reduce 0%
16/04/04 02:51:52 INFO mapreduce.Job:  map 12% reduce 0%
16/04/04 02:51:55 INFO mapreduce.Job:  map 13% reduce 0%
16/04/04 02:51:58 INFO mapreduce.Job:  map 16% reduce 0%
16/04/04 02:52:01 INFO mapreduce.Job:  map 17% reduce 0%
16/04/04 02:52:04 INFO mapreduce.Job:  map 20% reduce 0%
16/04/04 02:52:07 INFO mapreduce.Job:  map 21% reduce 0%
16/04/04 02:52:10 INFO mapreduce.Job:  map 24% reduce 0%
16/04/04 02:52:16 INFO mapreduce.Job:  map 28% reduce 0%
16/04/04 02:52:22 INFO mapreduce.Job:  map 32% reduce 0%
16/04/04 02:52:28 INFO mapreduce.Job:  map 36% reduce 0%
16/04/04 02:52:34 INFO mapreduce.Job:  map 40% reduce 0%
16/04/04 02:52:40 INFO mapreduce.Job:  map 44% reduce 0%
16/04/04 02:52:46 INFO mapreduce.Job:  map 48% reduce 0%
16/04/04 02:52:49 INFO mapreduce.Job:  map 49% reduce 0%
16/04/04 02:52:52 INFO mapreduce.Job:  map 51% reduce 0%
16/04/04 02:52:55 INFO mapreduce.Job:  map 53% reduce 0%
16/04/04 02:52:58 INFO mapreduce.Job:  map 55% reduce 0%
16/04/04 02:53:01 INFO mapreduce.Job:  map 58% reduce 0%
16/04/04 02:53:04 INFO mapreduce.Job:  map 59% reduce 0%
16/04/04 02:53:07 INFO mapreduce.Job:  map 63% reduce 0%
16/04/04 02:53:13 INFO mapreduce.Job:  map 67% reduce 0%
16/04/04 02:53:16 INFO mapreduce.Job:  map 72% reduce 0%
16/04/04 02:53:19 INFO mapreduce.Job:  map 100% reduce 0%
16/04/04 02:53:29 INFO mapreduce.Job:  map 100% reduce 100%
16/04/04 02:53:30 INFO mapreduce.Job: Job job_1459632208432_0002 completed successfully
16/04/04 02:53:30 INFO mapreduce.Job: Counters: 49
	File System Counters
		FILE: Number of bytes read=80793709
		FILE: Number of bytes written=97516744
		FILE: Number of read operations=0
		FILE: Number of large read operations=0
		FILE: Number of write operations=0
		HDFS: Number of bytes read=135350664
		HDFS: Number of bytes written=12638244
		HDFS: Number of read operations=6
		HDFS: Number of large read operations=0
		HDFS: Number of write operations=2
	Job Counters 
		Launched map tasks=1
		Launched reduce tasks=1
		Data-local map tasks=1
		Total time spent by all maps in occupied slots (ms)=107655
		Total time spent by all reduces in occupied slots (ms)=7393
		Total time spent by all map tasks (ms)=107655
		Total time spent by all reduce tasks (ms)=7393
		Total vcore-seconds taken by all map tasks=107655
		Total vcore-seconds taken by all reduce tasks=7393
		Total megabyte-seconds taken by all map tasks=110238720
		Total megabyte-seconds taken by all reduce tasks=7570432
	Map-Reduce Framework
		Map input records=593811
		Map output records=55089200
		Map output bytes=555226108
		Map output materialized bytes=16552418
		Input split bytes=137
		Combine input records=57920579
		Combine output records=3849947
		Reduce input groups=1018568
		Reduce shuffle bytes=16552418
		Reduce input records=1018568
		Reduce output records=1018568
		Spilled Records=6171386
		Shuffled Maps =1
		Failed Shuffles=0
		Merged Map outputs=1
		GC time elapsed (ms)=1856
		CPU time spent (ms)=98520
		Physical memory (bytes) snapshot=424710144
		Virtual memory (bytes) snapshot=1678204928
		Total committed heap usage (bytes)=168562688
	Shuffle Errors
		BAD_ID=0
		CONNECTION=0
		IO_ERROR=0
		WRONG_LENGTH=0
		WRONG_MAP=0
		WRONG_REDUCE=0
	File Input Format Counters 
		Bytes Read=135350527
	File Output Format Counters 
		Bytes Written=12638244
time elapsed 135935 ms
root@master:~/vtomasv#   
```
Aplicacion Corriendo 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab4_3.png"  width="60%" height="60%">
</p>



Listando el directorio de salida "wc" podemos observar lo siguiente 

```
root@master:~/vtomasv# hadoop fs -ls /uhadoop/vtomasv/wc/
Found 2 items
-rw-r--r--   3 root supergroup          0 2016-04-04 02:53 /uhadoop/vtomasv/wc/_SUCCESS
-rw-r--r--   3 root supergroup   12638244 2016-04-04 02:53 /uhadoop/vtomasv/wc/part-r-00000
```

Veamos cuantas veces aparece la palabra *"de"*

```
root@master:~/vtomasv# hdfs dfs -cat /uhadoop/vtomasv/wc/part-r-00000 | grep -P "^de\t" | more
de	4916431
```
Listo!! hemos creado nuestro primer ejerció de contar palabras en hadoop! Felicitaciones! 

