# Laboratorio 3

Hola! acá esta toda la info de este laboratorio, fue muy entretenido en clase es complicado replicar esto en este rep, pero de todas formas intentare contar en detalle lo visto en clase. 

## Resumem

Poder realizar actividades eficientes de manera local es un gran desafío, hay recursos que administrar y esos recursos frecuentemente son escasos, es por eso que una de las formas de obtener recursos es usar no solo los recursos locales sino también los de otra maquina disponible.... en la red mas cercana que tengamos. 
En este caso el ejercicio trata de utilizar computadores de el laboratorio del DCC para recrear una red de tipo (Peer‐to‐Peer: Structured) Computadora a Computadora con un servidor central que las pondrá de acuerdo para poder trabajar en conjunto. 

Para el directorio central utilizaremos la misma *clase* del taller anterior ***StartRegistryAndServer***, recordemos que nos registramos a ese directorio con una *clase* usuario (ya que somos almacenados en una tabla Hash) y cada nodo que se registre debe indicar el nombre de usuario, el nombre real y nombre del host con su respectivo puerto, todo esto para que cuando quiera realizar la operación de p2p pueda localizarnos uno a uno. 

Luego una vez que iniciamos el directorio (Intente montar una maquina en openshift pero lamentablemente no me dejaron habilitar los puertos de RMI para exposición sobre internet :S	 ) 

```cmd 
– java -jar rmi.jar StartRegistryAndServer -r -s 1
```

Cuando iniciamos el directorio el enviamos los parámetros ***-r*** que indican que iniciaremos el directorio RMI, y con el parámetro indicamos que tipo de servidor iniciaremos, existen tres tipos de directorios para este laboratorio (puedes jugar con los que quieras)  **1 - UserDirectoryServer**  , **2 - NaiveStringCountServer** y **3 - FTStringCountServer** para este caso iniciaremos el 1. 

Luego simplemente creamos una cantidad de nodos necesarias para poder realizar el conteo de los ngramas en forma distribuida! 

## Iniciando el directorio 

Para iniciar el directorio en eclipse lo hacemos como una herramienta externa como muestra la siguiente figura 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab3_01.png"  width="60%" height="60%">
</p>

Ahora tendremos el servidor corriendo de manera local 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab3_02.png"  width="60%" height="60%">
</p>

## Completar el metodo distributedNgramCount

Esto es un poco largo, primero dejare el método completo ( si, si, se que no respeta el principio de simplicidad de 5 lineas por método donde 3 son comentarios pero bueno es lo que ay!) 

```java
public static void distributedNgramCount(String inputFile, boolean gz, int k, int n, int dir_port, String dir_hostname) throws IOException, NotBoundException, AlreadyBoundException {
		System.out.println(INTRO);
		
		// open input from file
		InputStream is = new FileInputStream(inputFile);
		if(gz){
			is = new GZIPInputStream(is);
		}
		BufferedReader fileInput = new BufferedReader(new InputStreamReader(is,"utf-8"));
		
		// open iterator
		NGramParserIterator ngramIter = new NGramParserIterator(fileInput,n);
		
		// open prompt input
		BufferedReader prompt = new BufferedReader(new InputStreamReader(System.in));
		
		// read inputs on user details
		String uname = null;
		while(true){
			System.out.println("\n\nFirst enter a username ...");
			String line = prompt.readLine().trim();
			if(RESTRICTED.contains(line)){
				System.err.println("\n\nRestricted username ... try another");
			} else{
				uname = line;
				break;
			}
		}

		String name = null;
		while(true){
			System.out.println("\n\nNext enter your real name ...");
			String line = prompt.readLine().trim();
			if(RESTRICTED.contains(line)){
				System.err.println("\n\nRestricted username ... try another");
			} else{
				name = line;
				break;
			}
		}

		String hostIp = null;
		while(true){
			System.out.println("\n\nNext your hostname or local IP ...");
			String line = prompt.readLine().trim();
			hostIp = line;
			break;
		}

		int uport = -1;
		while(true){
			System.out.println("\n\nFinally your port (any number between 1001 and 1999 ...)");
			String line = prompt.readLine().trim();
			try{
				uport = Integer.parseInt(line);
				if(uport>1000 && uport<2000){
					break;
				} else{
					System.err.println("\n\nInvalid port (should be between 1001 and 1999)");
				}
			} catch(Exception e){
				System.err.println("\n\nNot a number (should be between 1001 and 1999)");
			}
		}

		User me = new User(uname, name, hostIp, uport);
		
		// set the hostname property
		System.setProperty("java.rmi.server.hostname", hostIp);

		// Then start your own RMI registry ...
		// so people sending you n-grams you can find your server
		Registry reg = startRegistry(me.getPort());

		// and start your count server to start receiving n-grams ...
		
		// create a skeleton/stub for the n-gram counting server
		// exportObject just creates an interface that can be called
		// remotely
		NaiveStringCountServer server = new NaiveStringCountServer();
		Remote stub = UnicastRemoteObject.exportObject(server,0);

		// just use the class name for the skeleton (any name would be fine
		// but client needs to know the name to find the stub)
		String skelname = NaiveStringCountServer.class.getSimpleName();

		// bind the skeleton to the registry under the given name
		reg.bind(skelname, stub);

		
		// now connect to directory to find
		// machines to count words for you
		UserDirectoryStub uds = connectToDirectory(dir_hostname, dir_port);
		
		// send it your details
		uds.createUser(me);
		
		// okay now we need to wait until everyone is at this same point
		Map<String,User> userDir = null;
		String line = null;
		do{
			System.out.println("\n\n1) We need to coordinate here and wait until everyone has started a server and registered it in the directory.");
			System.out.println("When everyone is at this point in the lab, type 'next'. Please don't do it before that point.");
			System.out.println("Otherwise you can type enter to refresh the user list");
			
			userDir = uds.getDirectory();
			printDirectory(userDir);
			
			line = prompt.readLine().trim();
		} while(!line.equals("next"));
		
		// refresh users just in case
		userDir = uds.getDirectory();
		printDirectory(userDir);
		
		ArrayList<NaiveStringCountStub> slaves = new ArrayList<NaiveStringCountStub>();
		ArrayList<User> users = new ArrayList<User>();
		
		// let's now open a connection to each
		// machine and get its NaiveStringCountStub
		System.out.println("\n\nConnecting to servers of users ...");
		for(User u: userDir.values()){
			// find the registry of that user
			Registry registry = LocateRegistry.getRegistry(u.getHostname(), u.getPort());
	
			// then get the stub we want from the registry
			System.out.println("\nBinding to "+u.getUsername());
			
			NaiveStringCountStub slave = (NaiveStringCountStub) registry.lookup(NaiveStringCountServer.class.getSimpleName());
			
			// let's test we can call the test method
			System.out.println("Slave "+u.getUsername()+" working: "+slave.test(me));
			
			slaves.add(slave);
			users.add(u);
		}
		
		System.out.println("\n\nConnected to "+slaves.size()+" slaves!");
		
		// this object will store batches of n-grams
		// to be sent to each server
		// 
		// e.g., batches.get(hash) indicates words to be sent to slaves.get(hash)
		// once batches.get(hash).size() reaches BATCH_SIZE, call
		// slaves.get(hash).countAndIndexStrings(batches.get(hash));
		ArrayList<ArrayList<String>> batches = new ArrayList<ArrayList<String>>(slaves.size());
		for(int i=0; i<slaves.size(); i++){
			batches.add(new ArrayList<String>(BATCH_SIZE));
		}
		
		int count = 0;
		int batchesSent = 0;
		while(ngramIter.hasNext()){
			String ngram = ngramIter.next();
			
			count++;
			if(count%TICKS==0){
				System.out.println("We have read "+count+" ngrams from file");
			}
			
			// TODO decide to which machine to send the ngram
			//  look at instructions for this ...
			
			// TODO add the ngram to the batch for that machine
			
			// TODO if we're at max batch size (BATCH_SIZE), send the ngrams
			// AND clear the batch
			// AND increment batches sent
			// AND print a message to say batch has been sent
		}
		
		System.out.println("Read "+count+" ngrams and sent them in "+batchesSent+" batches.");
		
		// TODO push the remaining batches to the slaves
		
		// TODO tell all the slaves you're finished sending data
		//  slaves.get(i).finalise(me)


		
		
		// now we need a coordination point to wait until everyone
		// is done ... this time we can check automatically
		
		boolean allFinalised = false;
		do{
			System.out.println("\n\nWe need to coordinate here and wait until everyone has sent their data.");
			System.out.println("We will wait until everyone has finalised automatically. Hit enter to refresh.\n");
			
			Set<User> finalised = server.finalised();
			
			allFinalised = true;
			for(User u:userDir.values()){
				if(finalised.contains(u)){
					System.out.println("Finalised "+u);
				} else{
					System.out.println("Still waiting on "+u);
					allFinalised = false;
				}
			}
			
			line = prompt.readLine().trim();
		} while(!allFinalised);
		
		
		System.out.println("\n\nAll users have finalised. Let's get the top-"+k+" n-grams from each slave");
		// now each slave should have the final counts for its
		// batch, so we can get the top-k from each slave
		// and find the best one
		
		// we'll do this the easy way, using a count object
		ConcurrentCountMap<String> globalTopKs = new ConcurrentCountMap<String>(); 
		for(NaiveStringCountStub slave: slaves){
			globalTopKs.addAll(slave.getTopKCounts(k));
		}
		
		// we should now have m x k n-grams in the globalTopKs object
		// for m the number of machines.
		// finally we can just print the top-k from that map:
		synchronized(server){
			System.out.println("\n\nThe top-"+k+" "+n+"-grams in the data are:");
			globalTopKs.printOrderedStats(k);
		}
		
		// we are done
		// hopefully it worked out okay
		do{
			System.out.println("\n\n2) We need to wait in case other users have yet to ask us for our counts.");
			System.out.println("We could do this automatically but let's wait manually. Type 'end' when everyone is done.");
			
			line = prompt.readLine().trim();
		} while(!line.equals("end"));
		
		System.out.println("\n\nIn total, we counted "+server.getNonUniqueStringsCounted()+" non-unique n-grams and "+server.getCounts().size()+" unique n-grams");
		
		// remove ourselves from the directory
		uds.removeUserWithName(me.getUsername());
		
		fileInput.close();
		prompt.close();
	}
```
### Enviar de manera correcta los ngramas a los nodos correspondientes 

Lo primero es poder entender que necesitamos dividir para vencer, por eso lo que hacemos es determinar cual seria la mejor estrategia para enviar los ngramas a los nodos. Lo que vamos hacer es enviar un tipo de ngrama a un nodo particular siguiendo la filosofía de si fueramos facebook y tenemos tres servidores en uno guardamos las fotos en otro los comentarios y en otro los usuarios :) 

Todo comienza desde ```String ngram = ngramIter.next();``` en ese momento obtenemos el ngrama siguiente, ahora tenemos que determinar según la cantidad de nodos que tengamos debemos enviarlo a un ngrama y todos los ngramas siguientes deben ser enviados al mismo nodo! 

A cada *ngram* se le puede calcular un hash dado que es un String de manera muy simple solo invocando el método [hashCode()](https://es.wikipedia.org/wiki/HashCode()_(Java)) ese numero puede ser usado para poder indicarnos a que nodo debemos enviar este ngram, pero es muy grande y por eso buscamos la manera de acomodarlo a que nos de un numero que si este dentro del rango de nodos que tenemos disponibles de la siguiente forma. 

`java
int hc = Math.abs(ngram.hashCode()) % slaves.size();
` 

Estos *ngram* los almacenamos hasta completar un total de `DistributedNgramCountApp.BATCH_SIZE` los vamos almacenando en `batches.get(hc).add(ngram);` una vez que están llenos los enviamos al nodo que los procesara (recordemos que en la lista de nodos también estamos nosotros). 


```Java
			if (batches.get(hc).size() == DistributedNgramCountApp.BATCH_SIZE )
			{
				slaves.get(hc).countAndIndexStrings(batches.get(hc));
				batches.get(hc).clear();
				batchesSent++;
				System.out.println("El batch" + hc + " fue enviado");
				
			}
```
Si vemos el codigo en detalle pudimos sacar todos los *ngram* del archivo, pero lo que no sabemos es cuales ***batches*** no puedieron ser enviados dado que no cumplieron con la condición de `DistributedNgramCountApp.BATCH_SIZE` por eso recorremos todos los ***batches*** y aquellos que no sean vacíos los enviamos! 

```java
		// TODO push the remaining batches to the slaves
		for (int i = 0; i < batches.size(); i++) {
			if (batches.get(i).size() >= 0)
			{			
				slaves.get(i).countAndIndexStrings(batches.get(i));
				batches.get(i).clear();
			}
		}
```

Una vez que terminamos de enviar todo los datos a cada uno de los nodos podemos indicar que hemos finalizado de enviar toda la informacion para que procedan a realizar las operaciones que sean necesarias. 

```Java
		// TODO tell all the slaves you're finished sending data
		//  slaves.get(i).finalise(me)
		for (int i = 0; i < slaves.size(); i++) {
			slaves.get(i).finalise(me);
		}
```

Con eso hemos terminado el laboratorio 3! 


## Pruebas 

Ahora vamos a jugar un rato con lo que hemos construido, primero vamos a dividir el gran archivo que tenemos de wiki que Aidan nos paso en el lab 1! 

`split -b 100mb es-wiki-abstracts.txt es-wiki-abstracts-`

Con esto ahora tenemos 4 archivos que podemos usar (solo porque lo estoy haciendo de forma local), dado esto tendré que crear 4 nodos que procesen uno cada archivo. 

Ahora configuramos cada uno de los nodos para que corra independientemente 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab3_04.png"  width="60%" height="60%">
</p>

Así quedaría nuestra consola con todos los nodos listos para correr

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab3_05.png"  width="60%" height="60%">
</p>

El servidor de nombres nos mostrara cada nodo resigtrado de la siguiente manera

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab3_06.png"  width="60%" height="60%">
</p>

Cuando todo este listo simplemente iniciamos todos los nodos con ***next** y esperamos que terminen cuando estos finalicen aparecerá lo siguiente:

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab3_07.png"  width="60%" height="60%">
</p>

Y luego nos mostrara los 10 ngramas!!!

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab3_08.png"  width="60%" height="60%">
</p>

Saludos!!


