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

## Manos a la obra 

Ahora una vez con estos conceptos podemos empezar a codificar. 
Vamos a crear un chat, en realidad un chat muy básico, la idea es que vamos a tener un servidor central donde los usuarios se registran, luego una vez registrados pueden hablar entre ellos de forma directa. 
Primero debe estar arriba el servidor central, este servidor central es el que permite que los usuario puedan hablarse entre si, por lo tanto un usuario para poder hablar con otro lo primero que hace es buscar el objeto en el servidor de usuarios que le permite registrarse (remotamente), luego con ese objeto se registra en el servidor central, una vez registrado puede buscar a otros usuarios. Particularmente si este usuario que se acaba de registrar quiere hablarle a otro lo que debe hacer es pedirle al servidor central que le muestre donde se encuentra el otro usuario, ya que cada usuario le va hablar al otro el forma directa, es por eso que el servidor central nos entrega los datos de donde se encuentra el registro del usuario con el que queremos hablar y nos conectamos con el para invocar los métodos remotos de ese usuario y así simular el chat. 

### Arquitectura inicial 
 
 Simulamos que existe un directorio de usuarios y dos usuarios que quieren hablar entre si. 
  
<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/interaccion.001.png"  >
</p>

### Creando usuarios  

Cada uno de los clientes repite este paso, se conecta al repositorio del directorio de usuarios y se agrega al directorio. 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/interaccion.002.png"  >
</p>

### Ahora uno de los clientes decide hablarle al otro

En este paso ya los dos usuarios creados, tienen la capacidad de listar que usuarios están conectados, el usuario (cliente 1) decide hablarle a otro usuario (cliente 2) por lo que lo busca en el directorio y se conecta al repositorio del otro usuario (con el que desea hablar)  

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/interaccion.003.png"  >
</p>

### A conversar! 

Una vez obtiene el objeto remoto ***InstantMessagingStub*** puede invocar el método *message(User, msg)* y con eso comienza la diversion! 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/interaccion.004.png"  >
</p>