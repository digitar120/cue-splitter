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
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");


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
            switch (
                    line.trim().split(" ")[0]
            ) {
                case "TRACK":
                    currentIndex++;
                    cueFileDefinition.getTrackList().add(new Track());
                    cueFileDefinition.getTrackList().get(currentIndex).setTrackNumber(currentIndex + 1);
                    break;
                case "PERFORMER":
                    cueFileDefinition.getTrackList().get(currentIndex).setPerformer(
                            line.trim().substring("PERFORMER ".length())
                    );
                    break;
                case "TITLE":
                    cueFileDefinition.getTrackList().get(currentIndex).setTitle(
                            line.trim().substring("TITLE ".length())
                    );
                    break;
                case "INDEX":
                    String offset = line.trim().substring("INDEX 01 ".length());
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
        // TIEMPO EN HH:MM:SS
        // Direcciones proveídas a comandos deben encerrarse en comillas simples
        // Comando a utilizar: ffmpeg -i "AK420 - A Matter Of Wax [Full BeatTape] [R1U2tN6Xlqk].opus" -ss 00:00:00 -to 00:03:09 -c copy "1. AK420 - Soul Made.opus"
        // ffmpeg -i <archivo> -ss <inicio> -to <fin> -c copy <archivo a crear>


        ExecutorService executor = Executors.newSingleThreadExecutor();

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command(

                    "powershell.exe",
                    "ffmpeg.exe",
                    "-y",
                    "-v",
                    "-i",
                    "'.\\" + cueFileDefinition.getFileName() + "'",
                    "-ss",
                    "00:00:00",
                    "-to",
                    "00:03:09",
                    "-c",
                    "copy",
                    cueFileDefinition.getTrackList().get(0).getPerformer() + " - " + cueFileDefinition.getTrackList().get(0).getTitle()

            );
            builder.directory(new File("C:\\Users\\Gabriel\\Desktop\\AK420\\A Matter Of Wax\\"));
        }

        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Future<?> future = executor.submit(streamGobbler);

        try {
            int exitCode = process.waitFor();
            System.out.println(future.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        executor.shutdown();
        reader.close();
    }


    private static String readProperty(BufferedReader reader, CueFile cueFileDefinition, Integer propertyStart) throws IOException {
        return StringUtils.strip(
                reader.readLine()
                        .substring(propertyStart),
                "\""
        );
    }
}
