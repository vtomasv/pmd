# Laboratorio 01
===========

## Objetivo 
Con el siguiente ejercicio vas a poder entender que significa en esfuerzo y codigo poder procesar archivos muy grandes, ya sea en memoria o usando el disco. 
Claramente cada una de las estrategias tiene sus pro y contras sin embargo lo importante es que adquieras los conocimientos para poder entender la complejidad del procesamiento masivo de datos. 

Para este ejercicio el profesor Aidan Hogan dejo a disposición un extracto de wiki en español ***es-wiki-abstracts.txt.gz***  con el cual trabajaremos. 

Adicionalmente como ven he subido los fuentes creados por el profesor para poder procesar este archivo en los siguientes ejercicios. 

*Para mas detalles de la actividad original dejada por el sr. Hogan dejo el pdf dentro de este lab*

***Nota:*** *no dejes de poner estos parámetros de VM en la ejecución de los test de unidad `-Djub.consumers=CONSOLE,H2 -Djub.db.file=.benchmarks` para poder ver los graficos en html y almacenar las ejeuciones para comparar la performance* 

Actividades 
------

Lo primero es poder entender el esfuerzo de procesamiento de archivos en memoria, el ejercicio es simple, primero gracias a la clase *RunWordCountInMemory* contaremos las apariciones de las palabras en el abstract del archivo de wikipedia, como verán en sus maquinas puede ser que se demore un poco o simplemente se rompa. Deje a disposición en los test de unidad un test *RunWordCountInMemoryTest* el cual nos permite no solo correr la prueba sino que también ademas nos creara una pagina web con los resultados si lo ejecutamos mas de una vez podremos ver el esfuerzo de cada ejecución y así ver que significa solo obtener las primeras 100 o mas palabras repetidas en el archivo. 

```java
public class RunWordCountInMemoryTest {

...

public void runWordCountInMemory() throws Exception  {
		...
		// Juega con el parametro -k :-)
		String[] args = {"-i", filePath , "-igz", "-k", "100"}; 
		RunWordCountInMemory.main(args);
		...
		
		}

}
		
``` 	

Como se ve es interesante como un archivo tan complejo puede ser procesado, sin embargo veamos como se comporta realizando una actividad un poco mas compleja que contar, en la calse *RunNGramCountInMemory* podemos ver como se realizara el conteo de los  [n-grama](https://es.wikipedia.org/wiki/N-grama), también esta a disposición el test de unidad *RunNGramCountInMemoryTest*, como en el ejercicio anterior puedes jugar con la cantidad de palabras que quieres que formen el [n-grama](https://es.wikipedia.org/wiki/N-grama) de la siguiente manera. 

```java
...

   // En todos los test puedes cambiar la cantidad de veces
   // que quieres que se ejecute o probar concurrencia! :-)
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 1)
	@Test
	public void runNGramCountInMemory_02() throws Exception {
		
		String filePath = new URI(RunNGramCountInMemoryTest.pathFileInput).normalize().getPath();		
		// El parametro n indica la agrupacion para los n-gramas 
		String[] args = {"-i", filePath , "-igz", "-k", "100",  "-n", "2"}; 
		
		RunNGramCountInMemory.main(args);
	}
	
	...
```

Ahora de este ejercicio como ya vimos el procesamiento en memoria puede resultar un poco complejo, sobretodo por las exigencias de equipo que se necesita para tener resultados sin que se realice uso intensivo de disco, procesador y memoria. Una solución es usar el disco para ir guardando los resultados eso lo entramos en la siguiente clase *ExtractNGrams* el en test *ExtractNGramsTest* están configurados al menos tres pruebas 2,3 y 4  [n-grama](https://es.wikipedia.org/wiki/N-grama) como podrán observar no es necesaria tanta RAM y ademas no hay crash sino que funcionan sin problemas :-). 


```java

...

	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void extractNGrams_3() throws Exception {
		
		String filePath = new URI(ExtractNGramsTest.pathFileInput).normalize().getPath();		
		String[] args = {"-i", filePath , 
						"-igz", "-n", "3", "-o", ExtractNGramsTest.nameFileOut_03, "-ogz"}; 
		
		ExtractNGrams.main(args);
		
	}   
...

```

Bueno ahora es tiempo de poder entender en detalle uno de los procesos mas útiles para ordenar grandes volúmenes de datos, si bien este algoritmo o patron de solución puede ser aplicado en una maquina también es posible usarlo en forma distribuida, esto seguramente lo veremos mas en detalle en proximos laboratorios :-). 

Todo el material que el profesor dejo a disposición es muy claro con respecto al ordenamiento externo, sin embargo no quiero dejar de mencionar otros vínculos que me ayudaron mucho a entender este proceso se los dejo a disposición: 

1. https://en.wikipedia.org/wiki/External_sorting
2. https://www.youtube.com/watch?v=ATK74YSzwxg


Para este ejercicio modificaremos la clase *ExternalMergeSort*, la cual nos deja disposición el método **mergeSortedBatches** para ser completado. En el ejercicio esta el codigo que el profesor nos ayudo a crear sin embargo estas libre para poder probar lo que tu quieras. Como puedes ver se utiliza la clase ***TreeSet*** que no es thread safe, adicionalmente puedes jugar usando la clase ***ConcurrentSkypListSet*** . 


![](../../assets/img/java-collections-api-O.png)

```Java

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

		// 	Iniciamos nuestro arbol 		
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

```

Despues que estudies en detalle este codigo puedes ejecutar el test *ExternalMergeSortTest* donde es posible procesar y ordenar los n-gramas con el algoritmo de external sorting. En este ejercicio puedes elegir sin problemas el tamaño de los archivos, es decir la cantidad de lineas que queremos que tenga cada archivo ordenado que forman parte de la solución, el parámetro es el ***-b*** jugando con este parámetro puedes ver que efectos tiene sobre la performance de este algoritmo (uso de memoria/cpu o uso de disco) 

```java

	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0, concurrency = 4)
	@Test
	public void externalMergeSort_4() throws Exception {
		
		String pathFileInput = Thread.currentThread().getContextClassLoader().getResource(ExternalMergeSortTest.nameFileinput_04).getPath();

		String fileInput = new URI(pathFileInput).normalize().getPath();		
		
		String[] args = {"-i", fileInput , "-igz", 
				"-o", ExternalMergeSortTest.nameFileOutput_04, "-ogz",
				"-tmp",	".",
				"-b", "10000000"}; 	// juega con este parametro
		
		ExternalMergeSort.main(args);
		
	}
	
```

Ahora vamos a contar la cantidad de veces que aparece un n-grama esto es posible gracias a la clase *CountDuplicates*, deje disponible un test de unidad que ayuda a ejecutar esto *CountDuplicatesTest*, pero sin embargo contar no es suficiente si queremos saber cual es el n-grama que mas se repite por eso tenemos que utilizar nuevamente  *ExternalMergeSort* en orden reverso y luego para obtener los últimos 10 podemos usar la clase *Head* hay un metodo en  *CountDuplicatesTest* que nos imprime en consola esto. 

Por ahora esto es todo espero se diviertan!!

Saludos. 

***vtomasv***

