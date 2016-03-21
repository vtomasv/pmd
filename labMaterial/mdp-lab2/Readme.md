# Laboratorio 2


## Resumen 

En este laboratorio la idea es aprender a usar [RMI](https://es.wikipedia.org/wiki/Java_Remote_Method_Invocation) (Java Remote Method Invocation). 

El ejercicio puede ser un poco confuso por los nombres de las clases por pero intentare explicar todo en detalle para que los nombre no produzcan algún tipo de confusion. 

## Pasos Iniciales 

Lo primero que vamos a hacer es poder entender el concepto de ejecución remota, pensemos en el siguiente escenario. Tengo un computador y un celular y quiero que cuando pulse un botón en el celular el computador inicie una actividad, ósea se ejecute algo en el computador y devuelva la respuesta. Todo esto pensando que claramente lo ejecuto en el computador porque el celular no tiene capacidad (memoria, disco, red, etc.) para poder realizarlo por si solo.

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/diagrama_1.png"  width="60%" height="60%">
</p>

Esto suena muy simple, pero en realidad hay que tener ciertas consideraciones. Para que el celular pueda ejecutar algo en forma remota es necesario que el computador exponga un servicio en forma remota. Supongamos que el servicio que quiero ejecutar lo provee un 3ro que no soy yo y debo ejecutar una función en un computador de forma remota, lo que esperamos es que el el dueño del computador nos entregue una ***contrato*** de comunicación con el. El ***contrato*** de comunicación no es mas que una **interface** java que establece la forma en la cual el celular y el computador pueden hablar, veamos un ejemplo. 


```java 
package org.mdp.dir;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * This is the interface that will be registered in the server.
 * In RMI, a remote interface is called a stub (on the client-side)
 * or a skeleton (on the server-side).
 * 
 * An implementation is created and registered on the server.
 * 
 * Remote machines can then call the methods of the interface.
 * 
 * Note: every method *must* throw RemoteException!
 * 
 * Note: every object passed or returned *must* be Serializable!
 * 
 * @author Aidan
 *
 */
public interface UserDirectoryStub extends Remote, Serializable{
	public boolean createUser(User u) throws RemoteException;
	
	public Map<String,User> getDirectory() throws RemoteException;
	
	public User removeUserWithName(String un) throws RemoteException;
}
```

En este ejemplo, podemos ver que desde el celular podría invocar los métodos ***createUser*** , ***getDirectory*** y ***removeUserWithName*** . Cada uno de estos métodos se verían como locales en mi codigo del celular pero en realidad se ejecutaran en el computador. 

El computador también puede tener la necesidad de no solo exponer un ***contrato*** sino que también tenga la necesidad de exponer otros contratos para otros usuarios, es por esto que el computador dispondrá de un catalogo de servicios, donde el podrá registrar los ***contratos*** que este dispuesto a exponer para que 3ros los usen. 

A este catalogo lo llamaremos *Registry*, este tiene la capacidad de almacenar los ***contratos*** y funciona como un artefacto independiente, es decir mi celular u otros pueden conectarse para pedirle un servicio y ejecutar cosas de forma remota sin dificultades. 

Ahora veamos como queda el escenario con este nuevo componente. 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/diagrama_2.png"  width="60%" height="60%">
</p>


