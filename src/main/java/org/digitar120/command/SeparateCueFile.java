package org.digitar120.command;

import org.apache.commons.lang3.StringUtils;
import org.digitar120.model.CueFile;
import org.digitar120.model.Track;
import org.digitar120.streamGobbler.StreamGobbler;
import picocli.CommandLine;

import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.digitar120.util.UtilityMethods.*;

@CommandLine.Command(
        name = "Separate CUE file",
        description = "Separates a music file into its separate tracks, according to the given CUE file"
)
public class SeparateCueFile implements Runnable{
    @CommandLine.Parameters(index = "0", description = "CUE file path")
    private String cueFilePath;

    public static void main (String[] args){
        new CommandLine(new SeparateCueFile()).execute(args);
    }

    @Override
    public void run() {

        String cueFileAbsolutePath = getAbsoluteFilePathIfExists(cueFilePath);

        String workingDirectory = new File(cueFileAbsolutePath).getParent();

        BufferedReader reader = createReader(cueFileAbsolutePath);

        String[] mainParameters = getMainParameters(reader);
        CueFile cueFile = new CueFile(
                mainParameters
                , getExtension(mainParameters[2]) // La tercera posición de mainParameters es el nombre del archivo de música
        );

        populateTrackList(reader, cueFile);


        dryRun(cueFileAbsolutePath, workingDirectory, cueFile);

        /*

        executeFFmpeg(cueFileAbsolutePath, workingDirectory, cueFile);

        closeReader(reader);

        Runtime.getRuntime().exit(0);

         */
        // TODO: resolver cómo se identifica la última pista, en relación al tiempo (getNextOffsetIfExists)
        // TODO: ver cómo devolver un código de ejecución diferente a 0 si hay fallas

    }

    private static void dryRun(String cueFileAbsolutePath, String workingDirectory, CueFile cueFile) {
        for(Track track: cueFile.getTrackList()){
            ProcessBuilder builder = defineFFmpegCommand(
                    workingDirectory,
                    cueFileAbsolutePath,
                    cueFile,
                    track,
                    getNextOffsetIfExists(cueFile, track));
            printBuilderCommand(builder);
        }
    }

    private static void closeReader(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    

    private static void executeFFmpeg(String cueFileAbsolutePath, String workingDirectory, CueFile cueFile) {
        for (Track track: cueFile.getTrackList()){
            ProcessBuilder builder = defineFFmpegCommand(
                    workingDirectory,
                    cueFileAbsolutePath,
                    cueFile,
                    track,
                    getNextOffsetIfExists(cueFile, track));

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            try {
                Process process = builder.start();
                StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
                Future<?> future = executorService.submit(streamGobbler);

                int exitCode = process.waitFor();
                System.out.println(exitCode);

                if (future.get()!=null){ // future.get() devuelve "null" si no hay una devolución desde la ejecución
                    System.out.println(future.get());
                }

                executorService.shutdown();

            } catch (IOException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static Optional<LocalTime> getNextOffsetIfExists(CueFile cueFile, Integer trackIndex){
        return Optional.of(
                cueFile.getTrackList().get(
                        trackIndex +1
                ).getTimeOffset());
    }

    private static Optional<LocalTime> getNextOffsetIfExists(CueFile cueFile, Track track){
        // Obtener el tiempo de inicio de la canción siguiente, si +esta existe
        try{
            return Optional.of(
                    cueFile.getTrackList().get(
                            cueFile.getTrackList().indexOf(track) +1
                    ).getTimeOffset());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ProcessBuilder defineFFmpegCommand(String workingDirectory, String cueFilePath, CueFile cueFile, Track track, Optional<LocalTime> nextTrackOffset){
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");

        boolean isLastTrack = track.getTrackNumber().equals(cueFile.getTrackList().size());

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(workingDirectory));

        // Modelo de comando
        // ffmpeg -i "AK420 - A Matter Of Wax [Full BeatTape] [R1U2tN6Xlqk].opus" -ss 00:00:00 -to 00:03:09 -c copy "1. AK420 - Soul Made.opus"
        // ffmpeg -i <archivo> -ss <inicio> -to <fin> -c copy <archivo a crear>
        builder.command(
                isWindows ? "powershell.exe" : "sh"
                , isWindows ? "-Command" : "-c"
                , isWindows ? "ffmpeg.exe" : "ffmpeg"
                , "-v"
                , "verbose"
                , "-i" // Archivo de música de entrada, especificado en el archivo CUE
                , "'" + cueFilePath + "'"
                , "-ss"
                , track.getTimeOffset().toString()
                , nextTrackOffset.isPresent() ? "-to" : ""

                /*
                Si la pista ingresada no es la última (por ej, 11/12), el fin de ésta es el comienzo de la siguiente.
                Por ejemplo:

                  TRACK 10 AUDIO
                    TITLE "Mehr"
                    INDEX 01 38:01:20
                  TRACK 11 AUDIO
                    TITLE "Roter Sand"
                    INDEX 01 42:11:14

                Los límites de "Mehr" son su comienzo (38:01:20) y el comienzo de "Roter Sand" (42:11:14).

                Como "Roter Sand" es la última pista, la opción "-to" y su argumento no son necesarios.

                 */
                , nextTrackOffset.isPresent() ? nextTrackOffset.get().toString() : ""
                , "-c"
                , "copy"
                , "'" +
                        track.getTrackNumber() +
                        ". " +
                        track.getPerformer() +
                        " - " +
                        track.getTitle() +
                        "." +
                        cueFile.getFileFormat() +
                        "'"
        );

        return builder;
    }

    private static void populateTrackList(BufferedReader reader, CueFile cueFile) {
        try {
            cueFile.setTrackList(parseTrackList(reader, cueFile.getPerformer()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Track> parseTrackList(BufferedReader reader, String mainPerformer) throws IOException {
        List<Track> trackList = new ArrayList<>();

        String line;
        int currentIndex = -1;

        while ( (line = reader.readLine()) != null){
            line = line.trim();

            switch (getFirstWord(line)){
                case "TRACK":
                    currentIndex++;
                    trackList.add(new Track());
                    trackList.get(currentIndex).setTrackNumber(currentIndex + 1);
                    break;
                case "PERFORMER":
                    trackList.get(currentIndex).setPerformer(getStringWithinDelimiters(line, "\""));
                case "TITLE":
                    trackList.get(currentIndex).setTitle(getStringWithinDelimiters(line, "\""));
                    break;
                case "INDEX":
                    trackList.get(currentIndex).setTimeOffset(parseCUETiming(line));

                    // Algunos archivos CUE pueden omitir la sección PERFORMER de una pista, por ejemplo cuando todas
                    // pertenecen al mismo artista.
                    if (trackList.get(currentIndex).getPerformer() == null){
                        trackList.get(currentIndex).setPerformer(mainPerformer);
                    }
            }
        }
        return trackList;
    }

    private static LocalTime parseCUETiming(String indexLine){
        String offset = indexLine.substring("INDEX ## ".length());

        String offsetMinutes = offset.substring(0, 2);
        String offsetSeconds = offset.substring(3,5);
        String offsetMilliseconds = offset.substring(6, 8);

        return LocalTime.MIN
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
    }

    private static String[] getMainParameters(BufferedReader reader){
        String[] mainParameters = new String[3];
        for (int i = 0; i<3; i++){
            mainParameters[i] = getStringWithinDelimiters(readNextLine(reader), "\"");
        }
        return mainParameters;
    }

    private static String getAbsoluteFilePathIfExists(String cueFilePath) {
        File cueFile = new File(cueFilePath);

        if (cueFile.isFile() && cueFile.toString().endsWith(".cue")){
            return cueFile.getAbsolutePath();
        } else {
            System.err.println("Error: not a CUE file");
            throw new RuntimeException();
        }
    }

    private static BufferedReader createReader(String absolutePath) {
        try {
            return new BufferedReader(new FileReader(absolutePath));
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static String readNextLine(BufferedReader reader){
        try {
            return reader.readLine();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static String getFirstWord(String string){
        // Devuelve la primera secuencia de caracteres de una String, delimitada con un espacio al final.
        return string.trim().split(" ")[0];
    }

    private static String getStringWithinDelimiters(String string, String delimiter){
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

    private static String getExtension(String path){
        // Devuelve los caracteres de una String que le siguen al último punto

        return path.substring(
                StringUtils.lastIndexOf(path, ".") +1
        );
    }
}
