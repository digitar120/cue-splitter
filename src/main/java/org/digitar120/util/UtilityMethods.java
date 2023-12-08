package org.digitar120.util;

import java.io.File;
import java.util.Arrays;

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
}
