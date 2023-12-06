package org.digitar120.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public final class UtilityMethods {
    private UtilityMethods(){}

    public static void printBuilderCommand(ProcessBuilder builder){
        System.out.println(
                String.join(" ", builder.command().toArray(new String[0]))
        );
    }

    public static boolean verifyFileExistence(File directory, String filename){
        // Verificación si el directorio ya contiene un archivo. Útil para reemplazar la confirmación de ffmpeg, que es
        // omitida por la opción "-y"
        if (
                Arrays.asList(directory.list())
                        .contains(filename)
        ){
            return true;
        } else {
            return false;
        }
    }
}