# Opcionales

Claramente es complicado realizar un buen servidor de alta disponibilidad para realizar esta actividad del laboratorio 3, sin embargo pensé en usar una de las mejores plataformas para realizar esta actividad en forma distribuida :) y esa plataforma es de Google.
La plataforma es Cloud Dataflow y busque un ejemplo de [contar palabras](https://github.com/GoogleCloudPlatform/DataflowJavaSDK-examples) de google que viene por defecto y arriba de ese ejemplo contar lo ngramas para esta actividad. 

# Cloud Dataflow SDK para Java 

[Google Cloud Dataflow](https://cloud.google.com/dataflow/) provee una de las mejores plataformas para ejecucion batch o streamming en paralelo, podes bajarte el sdk [Cloud
Dataflow SDK for Java](https://cloud.google.com/dataflow/model/programming-model) y las distintas demostraciones de Google Cloud Dataflow. 

## Contador de Palabras 

Aca hay un buen ejemplo del contador de palabras 
[contador de palabras](https://github.com/GoogleCloudPlatform/DataflowJavaSDK-examples/blob/master/src/main/java/com/google/cloud/dataflow/examples) hay mas detalles de como ejecutar esto en la plataforma en  [dataflow](https://cloud.google.com/dataflow/examples/wordcount-example).

1. [`MinimalWordCount`](https://github.com/GoogleCloudPlatform/DataflowJavaSDK-examples/blob/master/src/main/java/com/google/cloud/dataflow/examples/MinimalWordCount.java) es un simple pipeline para contar palabras, lo que si hace es introducir conceptos como [Pipelines](https://cloud.google.com/dataflow/model/pipelines),
[PCollections](https://cloud.google.com/dataflow/model/pcollection),
[ParDo](https://cloud.google.com/dataflow/model/par-do),
y  [leer y escribir](https://cloud.google.com/dataflow/model/reading-and-writing-data) desde fuentes storage (que son los discos en la nube).

1. [`WordCount`](https://github.com/GoogleCloudPlatform/DataflowJavaSDK-examples/blob/master/src/main/java/com/google/cloud/dataflow/examples/WordCount.java) introduce las buenas practicas [PipelineOptions](https://cloud.google.com/dataflow/pipelines/constructing-your-pipeline#Creating) y  propietarias [PTransforms](https://cloud.google.com/dataflow/model/composite-transforms).


## Corriendo el ejemplo de MPD

lo primero fue crear una cuenta en la plataforma cloud de google, despues de eso crear todos los accesos y funciones básicas para tener acceso a DataFlow como ven al inicio no hay flujos creados.  

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab03_cloud_01.png"  width="60%" height="60%">
</p> 



Despues solo segui el ejemplo de plugin de [eclipse](https://cloud.google.com/dataflow/getting-started-eclipse?hl=es) (fue muy sencillo). Una vez listo cree el proyecto en eclipse. 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab03_cloud_02.png"  width="60%" height="60%">
</p> 

A continuación la clase que cuenta los n-gramas 


```Java
package org.mdp.ngrams.cloud;

import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.io.TextIO;
import com.google.cloud.dataflow.sdk.options.DataflowPipelineOptions;
import com.google.cloud.dataflow.sdk.options.Default;
import com.google.cloud.dataflow.sdk.options.DefaultValueFactory;
import com.google.cloud.dataflow.sdk.options.Description;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.transforms.Aggregator;
import com.google.cloud.dataflow.sdk.transforms.Count;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.PTransform;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.transforms.Sum;
import com.google.cloud.dataflow.sdk.transforms.Top;
import com.google.cloud.dataflow.sdk.transforms.SerializableComparator;
import com.google.cloud.dataflow.sdk.util.gcsfs.GcsPath;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;

import java.io.IOException;
import java.util.*;


/**
 * Contar  N-Grams.
 */
public class NGramCount {

  static class ExtractNGramsFn extends DoFn<String, String> {
    private static final long serialVersionUID = 0;

    private Integer n;

    public ExtractNGramsFn(Integer n) {
      this.n = n;
    }

    private final Aggregator<Long, Long> ngramCount =
        createAggregator("ngramCount", new Sum.SumLongFn());

    @Override
    public void processElement(ProcessContext c) {

      String[] words = c.element().split("\\s+");

      List<String> ngrams = new ArrayList<String>();
      for (int i = 0; i <= words.length-this.n; i++) {
        StringBuilder ngram = new StringBuilder();
        for (int j = 0; j < this.n; j++) {
          if (j > 0) {
            ngram.append("\t");
          }
          ngram.append(words[i+j]);
        }
        ngrams.add(ngram.toString());
      }

      for (String ngram : ngrams) {
        if (!ngram.isEmpty()) {
          ngramCount.addValue(1L);
          c.output(ngram);
        }
      }
    }
  }


  public static class FormatAsTextFn extends DoFn<List<KV<String, Long>>, String> {
    private static final long serialVersionUID = 0;

    @Override
    public void processElement(ProcessContext c) {

      for (KV<String, Long> item : c.element()) {
        String ngram = item.getKey();
        long count = item.getValue();
        c.output(ngram + "\t" + count);
      }
    }
  }


  public static class CountNGrams
    extends PTransform<PCollection<String>, PCollection<List<KV<String, Long>>>> {

    private static final long serialVersionUID = 0;

    private Integer n;
    private Integer top;

    public CountNGrams(Integer n) {
      this.n = n;
      this.top = new Integer(100);
    }

    public CountNGrams(Integer n, Integer top) {
      this.n = n;
      this.top = top;
    }

    @Override
    public PCollection<List<KV<String, Long>>> apply(PCollection<String> lines) {


      PCollection<String> ngrams = lines.apply(
          ParDo.of(new ExtractNGramsFn(this.n)));

      PCollection<KV<String, Long>> ngramCounts =
          ngrams.apply(Count.<String>perElement());

      PCollection<List<KV<String, Long>>> topNgrams = 
          ngramCounts.apply(Top.of(this.top, new SerializableComparator<KV<String, Long>>() {
                    private static final long serialVersionUID = 0;

                    @Override
                    public int compare(KV<String, Long> o1, KV<String, Long> o2) {
                      return Long.compare(o1.getValue(), o2.getValue());
                    }
                  }).withoutDefaults());
      
      return topNgrams;
    }
  }


  public static interface NGramCountOptions extends PipelineOptions {
    @Description("N-Gramas.")
    @Default.Integer(3) // 
    Integer getN();
    void setN(Integer value);

    @Description("Cantidad de Ngramas a contar.")
    @Default.Integer(100)
    Integer getTop();
    void setTop(Integer value);

    @Description("Lugar donde saca las ngramas a contar.")
    @Default.String("gs://mdp-staging-location/es-wiki-abstracts-1k.txt")
    String getInputFile();
    void setInputFile(String value);

    @Description("Path del archivo donde se escribe la respuesta.")
    @Default.InstanceFactory(OutputFactory.class)
    String getOutput();
    void setOutput(String value);


    public static class OutputFactory implements DefaultValueFactory<String> {
      @Override
      public String create(PipelineOptions options) {
        DataflowPipelineOptions dataflowOptions = options.as(DataflowPipelineOptions.class);
        if (dataflowOptions.getStagingLocation() != null) {
          return GcsPath.fromUri(dataflowOptions.getStagingLocation())
              .resolve("counts-pmd.txt").toString();
        } else {
          throw new IllegalArgumentException("mmm se debe especificar un lugar donde dejar la salida");
        }
      }
    }

  }

  public static void main(String[] args) throws IOException {
    NGramCountOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
      .as(NGramCountOptions.class);
    Pipeline p = Pipeline.create(options);

    p.apply(TextIO.Read.named("ReadLines").from(options.getInputFile()))
     .apply(new CountNGrams(options.getN(), options.getTop()))
     .apply(ParDo.of(new FormatAsTextFn()))
     .apply(TextIO.Write.named("WriteCounts").to(options.getOutput()));

    p.run();
  }
}

```

Una vez listo esto solo hay que correr y esperar los resultados en el archivo de salida 

Ni bien inicia la ejecución se puede ver como se distribuyen y en paralelo se inician los flujos necesarios. 


<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab03_cloud_03.png"  width="60%" height="60%">
</p> 

Cuando termina podemos ver el estado de cada uno de los stages,. 


<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab03_cloud_04.png"  width="60%" height="60%">
</p> 

Ahora vemos el archivo de salida en nuestro storage

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab03_cloud_05.png"  width="60%" height="60%">
</p> 

Este es el contenido 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab03_cloud_06.png"  width="60%" height="60%">
</p> 

Y podemos ver el rendimiento de nuestras maquinas en el dashboard de Cloud 

<p style="text-align:center;">
<img  src="https://raw.githubusercontent.com/vtomasv/pmd/master/assets/lab03_cloud_07.png"  width="60%" height="60%">
</p> 


Saludos!