﻿# cue-splitter
Un proyecto personal, una aplicación de línea de comandos para trabajar con archivos CUE.

# Objetivos y descripción
El objetivo principal del programa es separar un archivo de música en sus canciones, mediante la información contenida en el archivo CUE. 

También me interesa parsear la información de FFprobe, un programa de utilidad de FFmpeg que sirve para informar sobre los metadatos incluídos en un archivo de música.

Sin un objetivo concreto, mi interés es también poder almacenar ésa información en objetos Java.

---

Un archivo CUE sirve, entre otras cosas, para darle una descripción del contenido de un único archivo de música a un reproductor de música. Hay instancias en las que el archivo de música puede tener todas las canciones en un único archivo, por ejemplo, si se digitaliza un disco de vinilo, o si se descarga un disco de YouTube. Una forma de apuntalar los nombres y artistas de cada canción es con un archivo CUE. Éste archivo define artista general, nombre de álbum, nombre de cada canción y el momento en el que cada canción arranca.

Sin embargo, hay reproductores de música que simplemente no reconocen un archivo CUE, ya que no es ampliamente utilizado. También puede pasar que sea más conveniente tener cada archivo por separado.

Después de investigar sobre convertidores de audio, llegué a la conclusión de que la manera más confiable de convertir un archivo de sonido es usando un programa de línea de comandos llamado FFmpeg, pero el problema más grande es alimentar cada línea de información para crear cada archivo correspondiente a cada canción. Un script fué la opción más lógica. 

También fué muy visible que la herramienta más directa sería un script en Bash, pero decidí hacerlo en Java para practicar. De todos modos, Java tiene sus propios beneficios, como la posibilidad de compilar para y ejecutar en cualquier plataforma.
