# Laboratorio 6 

Hola ! Esta vez vamos hacer lo que realizamos en el ejercicio [numero 5](https://github.com/vtomasv/pmd/tree/master/labMaterial/mdp-lab5) pero de manera mucho mas sencilla. En Haddop vimos que es posible crear dos [trabajos](https://hadoop.apache.org/docs/r2.6.1/api/org/apache/hadoop/mapreduce/Job.html) para lograr agrupar actores que trabaron juntos contando la cantidad total de veces y ordenarlos alfabéticamente. 
Para esto vamos a usar [PIG](https://es.wikipedia.org/wiki/Pig_(herramienta_de_programaci%C3%B3n)) brevemente **Pig** es una plataforma de alto nivel para crear programas MapReduce utilizados en Hadoop. Manos a la obra!   

## Creando un ambiente para probar PIG

La idea es ahorrar el máximo de tiempo en temas de administración y gestión de ambientes de desarrollo, es por eso que les dejo como inquietud un rol que es fundamental hoy en las organizaciones [DevOps](https://es.wikipedia.org/wiki/DevOps). Este tipo de roles nos permiten disponer con ciertas herramientas ambientes listos para poder desarrollar sin tener que hacer grandes esfuerzos de instalación ni nada que se le parezca. Hoy al igual que en el [ejercicio 4](https://github.com/vtomasv/pmd/tree/master/labMaterial/mdp-lab4) vamos a usar Docker y por medio de el vamos a bajarnos una maquina que ya tiene instalado PIG lo que nos facilitara la tarea de ejecutar y probar sin ningún tipo de problemas esto en nuestras maquinas sin instalar ni una sola aplicacion! 

### Bajando la receta Docker de Pig

Ustedes pueden usar cualquier recetar de Docker que contenga **Pig** yo decidi utilizar [esta](https://github.com/ipedrazas/docker-hadoop-2.5.2-pig-0.14.0), es obvio que pueden descargar la que ustedes les plazca. 

Primero debemos subir Kitematic, luego usamos la consola de Docker para el resto. 

Una vez que estamos dentro de la consola de Docker corremos el siguiente comando 

```bash
docker pull ipedrazas/hadoop-pig
```

Deberiamos tener una salida de esta forma 

```
latest: Pulling from ipedrazas/hadoop-pig
89b52f216c6c: Pulling fs layer 
df4eef236fcf: Pulling fs layer 
d6551114882c: Pulling fs layer 
67bcc7222632: Pulling fs layer 
b13317403897: Pulling fs layer 
e190d631ecdd: Pull complete 
...
ed015d5e80a5: Pull complete 
67c76f9929f1: Pull complete 
318c33449e4b: Already exists 
Digest: sha256:871ee256a629dc2607213d9677ff05872409b72fa7e838ce7d49dac268104811
Status: Downloaded newer image for ipedrazas/hadoop-pig:latest
```
 
 Una vez que la maquina ya se bajo solo tenemos que iniciar la maquina con el siguiente comando para quedar logeados en su consola

```bash
docker run -i -t ipedrazas/hadoop-pig /etc/bootstrap.sh -bash
```

Luego si queremos ver si esta corriendo **Pig** ejecutamos el siguiente comando 

```bash
pig -x local
```

### Afinando el ambiente
 
Una vez que tenemos todo corriendo pasamos a la etapa de copiar los datos de prueba para poder correr nuestros procesos creados con **Pig** 

Para poder copiar los archivos de prueba desde nuestra maquina a una de las máquinas docker ejecutamos el siguiente comando 


```
docker exec -i CONTAINER ID sh -c 'cat > /dondeDejamosElFile' < ./FileACopiar
```
Para obtener el CONTAINER ID solo corremos ``docker ps`` 

```
docker ps
CONTAINER ID        IMAGE                  COMMAND                CREATED   ...4add01e694bb        ipedrazas/hadoop-pig   "/etc/bootstrap.sh -   55 minutes ...
```
Ahora copiamos el archivo **imdb-stars-100k.tsv** 

```
bash-3.2$ docker exec -i 4add01e694bb sh -c 'cat > /root/vtomasv/imdb-stars-100k.tsv' < ./imdb-stars-100k.tsv 
```
Ahora esta en la maquina y debemos copiarlo a nuestro sistema de archivos distribuidos para poder correr nuestro proceso Hadoop con **pig** 

Solo por Orden creamos los siguientes directorios (ustedes pueden personalizar estos directorios sin problemas :) ) 

```
./usr/local/hadoop-2.5.2/bin/hdfs dfs -mkdir uhadoop
./usr/local/hadoop-2.5.2/bin/hdfs dfs -mkdir uhadoop/vtomasv
```

Copiamos los archivos! 

```
./usr/local/hadoop-2.5.2/bin/hdfs dfs -copyFromLocal /root/vtomasv/imdb-stars-100k.tsv  uhadoop/vtomasv 
```

Ahora copiamos nuestro archivo **PIG** dentro del contenedor, esto lo hacemos al igual que antes desde la consola de Docker

```
bash-3.2$ docker exec -i 4add01e694bb sh -c 'cat > /root/vtomasv/actor-count.pig' < ./actor-count.pig 
```

Ahora estamos listos para poder ejecutar sin problemas nuestro proceso **Pig** 

> Nota: No olvides modificar el archivo **pig** con la dirección correcta de la entrada y de la salida ;) 

Ups! 

Por alguna razón en este contenedor no viene encendido el **historyserver** pero puedes encenderlo con el siguiente comando 

```
 /usr/local/hadoop/sbin/mr-jobhistory-daemon.sh start historyserver
```

Ahora si podemos iniciar nuestro proceso **Pig** con 

`` 
pig actor-count.pig
```

Tendrás una salida de la siguiente forma: 

```
.....

HadoopVersion	PigVersion	UserId	StartedAt	FinishedAt	Features
2.5.2	0.14.0	root	2016-04-18 15:47:53	2016-04-18 15:50:26	HASH_JOIN,GROUP_BY,ORDER_BY,FILTER

Success!

Job Stats (time in seconds):
JobId	Maps	Reduces	MaxMapTime	MinMapTime	AvgMapTime	MedianMapTime	MaxReduceTime	MinReduceTime	AvgReduceTime	MedianReducetime	Alias	Feature	Outputs
job_1460999380570_0005	1	0	6	6	6	6	0	0	0	0	full_movies,movies,raw	MAP_ONLY	
job_1460999380570_0006	2	1	12	12	12	12	7	7	7	7	actor_pairs,filter_actor_pairs,filteres_costart,full_movies_copy	HASH_JOIN	
job_1460999380570_0007	1	1	6	6	6	6	6	6	6	6	costart_count,costart_groups	GROUP_BY,COMBINER	
job_1460999380570_0008	1	1	5	5	5	5	5	5	5	5	ordered_costart_count	SAMPLER	
job_1460999380570_0009	1	1	6	6	6	6	7	7	7	7	ordered_costart_count	ORDER_BY	/user/root/uhadoop/vtomasv/imdb-costars-100k,

Input(s):
Successfully read 100000 records (8294271 bytes) from: "hdfs://172.17.0.4:9000/user/root/uhadoop/vtomasv/imdb-stars-100k.tsv"

Output(s):
Successfully stored 8952 records (317729 bytes) in: "/user/root/uhadoop/vtomasv/imdb-costars-100k"

Counters:
Total records written : 8952
Total bytes written : 317729
Spillable Memory Manager spill count : 0
Total bags proactively spilled: 0
Total records proactively spilled: 0

Job DAG:
job_1460999380570_0005	->	job_1460999380570_0006,
job_1460999380570_0006	->	job_1460999380570_0007,
job_1460999380570_0007	->	job_1460999380570_0008,
job_1460999380570_0008	->	job_1460999380570_0009,
job_1460999380570_0009


2016-04-18 15:50:26,869 [main] INFO  org.apache.hadoop.yarn.client.RMProxy - Connecting to ResourceManager at /0.0.0.0:8032
2016-04-18 15:50:26,873 [main] INFO  
...
2016-04-18 15:50:27,423 [main] INFO  org.apache.hadoop.mapred.ClientServiceDelegate - Application state is completed. FinalApplicationStatus=SUCCEEDED. Redirecting to job history server
2016-04-18 15:50:27,459 [main] INFO  org.apache.hadoop.yarn.client.RMProxy - Connecting to ResourceManager at /0.0.0.0:8032
2016-04-18 15:50:27,463 [main] INFO  org.apache.hadoop.mapred.ClientServiceDelegate - Application state is completed. FinalApplicationStatus=SUCCEEDED. Redirecting to job history server
2016-04-18 15:50:27,489 [main] INFO  org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.MapReduceLauncher - Success!
2016-04-18 15:50:27,517 [main] INFO  org.apache.pig.Main - Pig script completed in 2 minutes, 38 seconds and 72 milliseconds (158072 ms)
... 

```
Ahora podemos ver el contenido

```
bash-4.1# ./hdfs dfs -cat uhadoop/vtomasv/imdb-costars-100k_2/part-r-00000  | more
28	Akpinar, Metin##Alasya, Zeki
24	Acosta, Armando##Alcaraz, Eduardo
21	Acosta, Armando##Agostí, Carlos
19	Acosta, Armando##Alcocer, Víctor
17	Acuff, Eddie##Adams, Ernie (I)
16	Acosta, Armando##Aguilar, Antonio (I)
16	Adams, Ernie (I)##Adamson, Victor
15	Aguilar, Antonio (I)##Alcocer, Víctor
15	Adams, Ernie (I)##Adams, Ted (I)
15	Acosta, Armando##Adalid, Ricardo
15	Alcaraz, Eduardo##Alcocer, Víctor
15	Abraham, David (II)##Agha
14	Akan, Tarik##Akçatepe, Halit
14	Adams, Ted (I)##Adamson, Victor
11	Acosta, Armando##Aguilar, Luis (I)
10	Aguilar, Luis (I)##Alcocer, Víctor
10	Aguilar hijo, Antonio##Aguilar, Antonio (I)
9	Aguilera, Manuel (I)##Aguirre, Fernando (I)
9	Al Atrache, Farid##Al Nabulsy, Abdel Salam (I)
9	Aiello, Danny##Aiello, Rick
9	Abel, Alfred##Albers, Hans
9	Adalid, Ricardo##Alcocer, Víctor
9	Agha##Agha, Jalal
8	Akçatepe, Halit##Alasya, Zeki
8	Abraham, David (II)##Ajit (I)
8	Aguilar, Antonio (I)##Aguilar, Salvador (I)
8	Abeillé, Jean##Agoston, Gaby
8	Agostí, Carlos##Alcaraz, Eduardo
8	Agostí, Carlos##Alcocer, Víctor
8	Akpinar, Metin##Akçatepe, Halit
7	Agha##Ajit (I)
7	Adalid, Ricardo##Alcaraz, Eduardo
7	Ahn, Philip##Ahn, Philson
7	Abel, Alfred##Alberti, Fritz
7	Aguilar, Antonio (I)##Aguilar, José (I)
7	Aguilar, Antonio (I)##Aguilar, Luis (I)
7	Adolphson, Edvin##Ahrle, Elof
7	Aguirre 'Trotsky', José Luis##Alcocer, Víctor
7	Adolphson, Edvin##Ahlin, Harry
6	Acuff, Eddie##Alberni, Luis
6	Acosta, Armando##Aguilar, Adolfo (I)
6	Aguilar, Luis (I)##Ahuet, Julio
6	Aguilar, Antonio (I)##Aguilar, Pepe (I)
```