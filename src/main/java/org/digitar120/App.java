package org.digitar120;


import org.digitar120.model.CueFile;

import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.digitar120.model.Track;

import static org.digitar120.util.UtilityMethods.*;


public class App {
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .forEach(consumer);
        }
    }

    public static void main(String[] args) throws IOException {


        File directory = new File("C:\\Users\\Gabriel\\Desktop\\AK420\\A Matter Of Wax\\");


        // ADQUISICIÓN DE LOS DATOS DEL ARCHIVO
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Gabriel\\Desktop\\AK420\\A Matter Of Wax\\cue.cue"));
        CueFile cueFileDefinition = new CueFile();

        // Artista, álbum
        cueFileDefinition.setPerformer(readProperty(reader, cueFileDefinition, "PERFORMER ".length()));
        cueFileDefinition.setTitle(readProperty(reader, cueFileDefinition, "TITLE ".length()));
        /* cueFileDefinition.setFileName(
                StringUtils.strip(
                        reader.readLine()
                                .substring("FILE ".length())
                                .replace(" MP3", "")
                        , "\""
                )
        );*/
        String thirdLine = reader.readLine();
        String fileName = thirdLine.substring(
                thirdLine.indexOf("\"") + 1,
                StringUtils.ordinalIndexOf(thirdLine, "\"", 2)
        );
        cueFileDefinition.setFileName(fileName);

        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        cueFileDefinition.setFileFormat(fileExtension);


        String line;
        Integer currentIndex = -1;
        while (
                (line = reader.readLine()) != null
        ) {
            //System.out.println(line.trim().split(" ")[0]);
            line = line.trim(); // Para evitar ejecutar trim() en varios métodos después

            switch (
                    line.split(" ")[0] // Seleccionar la primera columna de cada línea
            ) {
                case "TRACK":
                    currentIndex++;
                    cueFileDefinition.getTrackList().add(new Track());
                    cueFileDefinition.getTrackList().get(currentIndex).setTrackNumber(currentIndex + 1);
                    break;
                case "PERFORMER":
                    cueFileDefinition.getTrackList().get(currentIndex).setPerformer(
                            line.substring(
                                    "PERFORMER ".length() +1
                                    , StringUtils.lastIndexOf(line, "\"")
                                    )
                    );
                    break;
                case "TITLE":
                    cueFileDefinition.getTrackList().get(currentIndex).setTitle(
                            line.substring(
                                    "TITLE ".length() +1
                                    , StringUtils.lastIndexOf(line, "\"")
                            )
                    );
                    break;
                case "INDEX":
                    String offset = line.substring("INDEX 01 ".length());
                    String offsetMinutes = offset.substring(0, 2);
                    String offsetSeconds = offset.substring(3,5);
                    String offsetMilliseconds = offset.substring(6, 8);

                    LocalTime timeOffset = LocalTime.MIN
                            .plus(
                                    Long.parseLong(offsetMinutes)
                                    , ChronoUnit.MINUTES)
                            .plus(
                                    Long.parseLong(offsetSeconds)
                                    , ChronoUnit.SECONDS
                            )
                            .plus(
                                    Long.parseLong(offsetMilliseconds)
                                    , ChronoUnit.MILLIS
                            );

                    cueFileDefinition.getTrackList().get(currentIndex).setTimeOffset(timeOffset);

                    break;
            }
        }

        // EJECUCIÓN
         //  ffmpeg -i "AK420 - A Matter Of Wax [Full BeatTape] [R1U2tN6Xlqk].opus" -ss 00:00:00 -to 00:03:09 -c copy "1. AK420 - Soul Made.opus"
        // ffmpeg -i <archivo> -ss <inicio> -to <fin> -c copy <archivo a crear>


        for (int i = 0; i< cueFileDefinition.getTrackList().size() -1; i++){
            ProcessBuilder builder = defineFFmpegCommand(
                    directory
                    ,cueFileDefinition.getFileName()
                    ,cueFileDefinition.getFileFormat()
                    ,cueFileDefinition.getTrackList().get(i).getTimeOffset()
                    ,cueFileDefinition.getTrackList().get(i + 1).getTimeOffset() // Los archivos CUE solo indican el comienzo de cada canción. El fin de la canción 'i' es el principio de la canción 'i+1'
                    ,cueFileDefinition.getTrackList().get(i).getTrackNumber()
                    ,cueFileDefinition.getTrackList().get(i).getPerformer()
                    ,cueFileDefinition.getTrackList().get(i).getTitle()
            );

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Process process = builder.start();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?> future = executor.submit(streamGobbler);

            try {
                int exitCode = process.waitFor();

                System.out.println(future.get());
            } catch (Exception e) {}
        }

        Integer lastTrackNumber = cueFileDefinition.getTrackList().size() -1;
        ProcessBuilder builder = defineLastFFmpegCommand(
                directory
                ,cueFileDefinition.getFileName()
                ,cueFileDefinition.getFileFormat()
                ,cueFileDefinition.getTrackList().get(lastTrackNumber).getTimeOffset()
                , cueFileDefinition.getTrackList().get(lastTrackNumber).getTrackNumber()
                ,cueFileDefinition.getTrackList().get(lastTrackNumber).getPerformer()
                ,cueFileDefinition.getTrackList().get(lastTrackNumber).getTitle()
        );
        printBuilderCommand(builder);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Future<?> future = executor.submit(streamGobbler);

        try {
            int exitCode = process.waitFor();
            System.out.println(exitCode);

            if (future.get()!=null){
                System.out.println(future.get());
            }
        } catch (Exception e) {}

        executor.shutdown();
        reader.close();

        Runtime.getRuntime().exit(0); // El programa no termina sin ésta línea.
    }

    private static ProcessBuilder defineFFmpegCommand(File directory, String filename, String fileFormat, LocalTime startingTime, LocalTime endingTime, Integer trackNumber, String trackPerformer, String trackTitle) {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(directory);
        if (isWindows) {
            builder.command(
                    "powershell.exe",
                    "ffmpeg.exe",
                    "-v",
                    "quiet",
                    "-y",
                    "-i",
                    "'.\\" + filename + "'",
                    "-ss",
                    startingTime.toString(),
                    "-to",
                    endingTime.toString(),
                    "-c",
                    "copy",
                    "'" + trackNumber + ". " + trackPerformer + " - " + trackTitle + "." + fileFormat + "'"
            );
        } else {
            builder.command(
                    "/bin/sh",
                    "-c",
                    "ffmpeg",
                    "-v",
                    "quiet",
                    "-y",
                    "-i",
                    "'.\\" + filename + "'",
                    "-ss",
                    startingTime.toString(),
                    "-to",
                    endingTime.toString(),
                    "-c",
                    "copy",
                    "'" + trackNumber + ". " + trackPerformer + " - " + trackTitle + "." + fileFormat + "'" // Nombre del archivo a crear
            );
        }
        return builder;
    }

    private static ProcessBuilder defineLastFFmpegCommand(File directory, String filename, String fileFormat, LocalTime startingTime, Integer trackNumber, String trackPerformer, String trackTitle) {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(directory);
        if (isWindows) {
            builder.command(
                    "powershell.exe",
                    "ffmpeg.exe",
                    "-y",
                    "-v",
                    "quiet",
                    "-i",
                    "'.\\" + filename + "'",
                    "-ss",
                    startingTime.toString(),
                    "-c",
                    "copy",
                    "'" + trackNumber + ". " + trackPerformer + " - " + trackTitle + "." + fileFormat + "'"
            );
        } else {
            builder.command(
                    "/bin/sh",
                    "-c",
                    "ffmpeg",
                    "-v",
                    "quiet",
                    "-y",
                    "-i",
                    "'.\\" + filename + "'",
                    "-ss",
                    startingTime.toString(),
                    "-c",
                    "copy",
                    "'" + trackNumber + ". " + trackPerformer + " - " + trackTitle + "." + fileFormat + "'" // Nombre del archivo a crear
            );
        }
        return builder;
    }


    private static String readProperty(BufferedReader reader, CueFile cueFileDefinition, Integer propertyStart) throws IOException {
        return StringUtils.strip(
                reader.readLine()
                        .substring(propertyStart),
                "\""
        );
    }
}
