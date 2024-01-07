package org.digitar120;

import org.apache.commons.lang3.StringUtils;
import org.digitar120.model.*;
import org.digitar120.streamGobbler.StreamGobbler;
import picocli.CommandLine;

import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.digitar120.util.UtilityMethods.*;

@CommandLine.Command(
        name = "CUEFileTool",
        description = "CUE file multitool"
)
public class CueSplitter implements Runnable{
    static boolean isWindows = System.getProperty("os.name")
            .toLowerCase()
            .startsWith("windows");

    @CommandLine.Parameters(index = "0", description = "CUE file path")
    private String cueFilePath;

    public static void main (String[] args){
        new CommandLine(new CueSplitter()).execute(args);
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

        String fileAbsolutePath =
                workingDirectory +
                (isWindows ? "\\" : "/")
                + cueFile.getFileName();


        executeFFprobe(fileAbsolutePath).forEach(System.out::println);

        List<String> ffprobeOutput = executeFFprobe(fileAbsolutePath);



        List<FileStreamInput> fileMetadata = new ArrayList<>();
        int inputCount = -1;
        int fileStreamCount = -1;
        for(int i = 0; i < ffprobeOutput.size(); i++){
            String line = ffprobeOutput.get(i);
            switch (getFirstWord(line)){
                case "Input":
                    inputCount++;
                    fileMetadata.add(new FileStreamInput(
                            inputCount,
                            StringUtils.chop(getNthWord(line, 2))
                    ));
                    break;

                case "Duration:":
                    String durationColumn = StringUtils.chop(getNthWord(line, 1));

                    fileMetadata.get(inputCount).setDuration(LocalTime.parse(durationColumn));
                    fileMetadata.get(inputCount).setStartTime(LocalTime.MIN); //TODO parsear
                    fileMetadata.get(inputCount).setBitrate(new Bitrate(
                            Integer.parseInt(getNthWord(line, 5)),
                            getNthWord(line, 6)
                    ));
                    break;
                case "Chapter":
                        break;

                case "Stream":
                    fileStreamCount++;
                    String streamIndexColumn = getNthWord(line, 1);

                    int streamIndex;
                    if (streamIndexColumn.contains("(")){
                        streamIndex = Integer.parseInt(
                                streamIndexColumn.substring(
                                        StringUtils.indexOf(streamIndexColumn, ":") +1,
                                        StringUtils.indexOf(streamIndexColumn, "(")
                                )
                        );
                    } else {
                        streamIndex = Integer.parseInt(StringUtils.chop(streamIndexColumn.substring(StringUtils.indexOf(streamIndexColumn, ":") +1)));
                    }

                    fileMetadata.get(inputCount).getFileStreams().add(new FileStream(
                            streamIndex,

                            StringUtils.chop(getNthWord(line, 2)),
                            new StreamContainer(StringUtils.chop(getNthWord(line, 3))),
                            "",
                            "",
                            "",
                            "",
                            new ArrayList<>(),
                            "",
                            new ArrayList<>()
                    ));

                    break;

            }
        }

        System.out.println(fileMetadata);


        // TODO Parsear capítulos
        /*
        List<FileStreamInput> streamInputs = new ArrayList<>();
        inputLineIndexes.forEach(n -> streamInputs.add(new FileStreamInput(
                n,
                StringUtils.chop(getNthWord(inputLines.get(n), 3)),
                StringUtils.chop(getNthWord(inputLines.get(n+1), 2)),
                Long.parseLong(StringUtils.chop(getNthWord(inputLines.get(n+1), 4))),
                new Bitrate(
                        Integer.getInteger(getNthWord(inputLines.get(n+1), 6)),
                        getNthWord(inputLines.get(n+1), 7)
                ),
                null,
                null

        )));

         */

        // TODO: Resolver problema de ejecución en Linux

    }

    private static List<Integer> getInputIndexes(List<String> input){
        // Guardar índices de las líneas en las que comienza una nueva definición de Input
        List<Integer> inputLineIndexes = new ArrayList<>();
        for(int i = 0; i < input.size(); i++){
            if (getFirstWord(input.get(i)).equals("Input")){
                inputLineIndexes.add(i);
            }
        }
        return inputLineIndexes;
    }

    private static List<String> getInputLines(String fileAbsolutePath) {
        List<String> ffprobeOutput = executeFFprobe(fileAbsolutePath);

        int beginningLine = getInputBeginningLine(ffprobeOutput);
        return ffprobeOutput.stream().skip(beginningLine).collect(Collectors.toList());
    }

    private static int getInputBeginningLine(List<String> input){
        int i = 0;
        while (!getFirstWord(input.get(i).trim()).equals("Input")){
            i++;
        }

        return i;
    }

    private static void dryRun(String cueFileAbsolutePath, String workingDirectory, CueFile cueFile) {
        for(Track track: cueFile.getTrackList()){
            ProcessBuilder builder = defineFFmpegCommand(workingDirectory, cueFileAbsolutePath, cueFile, track);

            printBuilderCommand(builder);
        }
    }

    private static List<String> executeFFprobe(String absoluteFilePath){
        ProcessBuilder builder = defineFFprobeCommand(new File(absoluteFilePath).getParent(), absoluteFilePath);

        printBuilderCommand(builder);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Process process = builder.start();

            List<String> accumulatedOutput = new ArrayList<>();
            StreamGobbler streamGobbler = new StreamGobbler(process.getErrorStream(), accumulatedOutput::add);
            Future<?> future = executorService.submit(streamGobbler);

            int exitCode = process.waitFor();

            executorService.shutdown();

            return accumulatedOutput;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeFFmpeg(String cueFileAbsolutePath, String workingDirectory, CueFile cueFile) {
        for (Track track: cueFile.getTrackList()){
            ProcessBuilder builder = defineFFmpegCommand(workingDirectory, cueFileAbsolutePath, cueFile, track);

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            try {
                Process process = builder.start();
                StreamGobbler streamGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);
                // FFmpeg no escribe a stdout, escribe a stderr
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

    private static ProcessBuilder defineFFprobeCommand(String workingDirectory, String filePath){
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(workingDirectory));

        System.out.println(transformPathSpaces(filePath));


        builder.command(
                isWindows ? "powershell.exe" : "sh",
                isWindows ? "-Command" : "-c",
                isWindows ?
                        ("ffprobe.exe -v verbose " + "'" + filePath + "'") :
                        ("ffprobe -v verbose \"" + filePath + "\"")
        );
        return builder;
    }

    private static ProcessBuilder defineFFmpegCommand(String workingDirectory, String cueFilePath, CueFile cueFile, Track track){


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
                , isLastTrack ? "" : "-to"

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
                , isLastTrack ? "" : cueFile.getTrackList()
                        .get(track.getTrackNumber()) // No es +1 porque trackNumber comienza en 1
                        .getTimeOffset().toString()

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


}
