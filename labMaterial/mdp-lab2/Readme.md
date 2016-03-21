# Laboratorio 2


## Resumen 

En este laboratorio la idea es aprender a usar [RMI](https://es.wikipedia.org/wiki/Java_Remote_Method_Invocation) (Java Remote Method Invocation). 

El ejercicio puede ser un poco confuso por los nombres de las clases por pero intentare explicar todo en detalle para que los nombre no produzcan algún tipo de confusion. 

## Pasos Iniciales 

Lo primero que vamos a hacer es poder entender el concepto de ejecución remota, pensemos en el siguiente escenario. Tengo un computador y un celular y quiero que cuando pulse un botón en el celular el computador inicie una actividad, ósea se ejecute algo en el computador y devuelva la respuesta. Todo esto pensando que claramente lo ejecuto en el computador porque el celular no tiene capacidad (memoria, disco, red, etc.) para poder realizarlo por si solo.

![Diagrama 1](https://raw.githubusercontent.com/vtomasv/pmd/9516fef849b2d98fee6727d3797005eda22207b4/assets/diagrama_1.svg)

<img src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/diagrama_1.svg">


	