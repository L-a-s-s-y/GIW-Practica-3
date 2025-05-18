# Manual de usuario
## Uso con Maven
Dirigirse al directorio del proyecto. Una vez ahí:
~~~
mvn install
mvn compile
~~~
Para ejecutar el indexador:
~~~
mvn exec:java -Dexec.mainClass="giw.prac3.Indexador" -Dexec.args="[ruta a los documentos] [ruta al archivo de stopwords] [ruta al directorio que será el índice]"
~~~

Para ejecutar el Buscador:
~~~
mvn exec:java -Dexec.mainClass="giw.prac3.BuscadorGUI" -Dexec.args="[ruta al directorio que será el índice] [ruta al archivo de stopwords]"
~~~

## Empaquetado (si fuese necesario) y uso habitual
Si fuese necesario empaquetar, dirigirse al directorio del proyecto y:
~~~
mvn clean package
~~~
### Indexador
Desde el directorio del proyecto:
~~~
java -cp target/giw-prac3-1.0-SNAPSHOT.jar giw.prac3.Indexador [ruta a los documentos] [ruta al archivo de stopwords] [ruta al directorio que será el índice]
~~~
## Motor de búsqueda
Desde el directorio del proyecto:
~~~
java -cp target/giw-prac3-1.0-SNAPSHOT.jar giw.prac3.BuscadorGUI [ruta al directorio que será el índice] [ruta al archivo de stopwords]
~~~
