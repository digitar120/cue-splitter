package org.digitar120.util;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public final class UtilityMethods {
    public UtilityMethods(){}

    public static void printBuilderCommand(ProcessBuilder builder){
        System.out.println(
                String.join(" ", builder.command().toArray(new String[0]))
        );
    }

    public static boolean verifyFileExistence(File directory, String filename){
        // Verificación si el directorio ya contiene un archivo. Útil para reemplazar la confirmación de ffmpeg, que es
        // omitida por la opción "-y"

        // TODO: refactorizar como Optional
        if (
                Arrays.asList(directory.list())
                        .contains(filename)
        ){
            return true;
        } else {
            return false;
        }
    }

    public static String returnFirstWord(String string){
        return string.split(" ")[0];
    }

    public static String readNextLine(BufferedReader reader){
        try {
            return reader.readLine();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static String getFirstWord(String string){
        // Devuelve la primera secuencia de caracteres de una String, delimitada con un espacio al final.
        return string.trim().split(" ")[0];
    }

    public static String getNthWord(String string, Integer index){
        try {
            return string.trim().split(" ")[index];
        } catch (IndexOutOfBoundsException e){
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader createReader(String absolutePath) {
        try {
            return new BufferedReader(new FileReader(absolutePath));
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void closeReader(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAbsoluteFilePathIfExists(String cueFilePath) {
        File cueFile = new File(cueFilePath);

        if (cueFile.isFile() && cueFile.toString().endsWith(".cue")){
            return cueFile.getAbsolutePath();
        } else {
            System.err.println("Error: not a CUE file");
            throw new RuntimeException();
        }
    }

    public static String getStringWithinDelimiters(String string, String delimiter){
        // Ejemplo:
        //PERFORMER "Rammstein"
        // Devuelve:
        //Rammstein

        // Asume que solo hay dos delimitadores.

        return string.trim().substring(
                StringUtils.indexOf(string, delimiter) +1
                , StringUtils.lastIndexOf(string, delimiter)
        );
    }

    public static String getExtension(String path){
        // Devuelve los caracteres de una String que le siguen al último punto

        return path.substring(
                StringUtils.lastIndexOf(path, ".") +1
        );
    }

    public static String transformPathSpaces(String path){
        // En sh POSIX, un path con espacios puede expresarse con comillas o con espacios escapados.
        // "/carpeta 1/archivo 1"
        // /carpeta\ 1/archivo\ 1
        return StringUtils.replace(path, " ", "\\ ");
    }

    public static String getChoppedNthWord (String string, int column){
        return StringUtils.chop(getNthWord(string, column));
    }

    public static String getStringAfterSequence(String string, String sequence){
        return string.substring(
                StringUtils.indexOf(string, sequence)
        );
    }
}
