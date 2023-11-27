package org.digitar120;


import org.digitar120.Model.CueFile;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.digitar120.Model.Track;


public class App
{

    public static void main( String[] args ) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Gabriel\\Desktop\\AK420\\A Matter Of Wax\\cue.cue"));
        CueFile cueFileDefinition = new CueFile();

        // Artista, álbum
        cueFileDefinition.setPerformer(readProperty(reader, cueFileDefinition, "PERFORMER ".length()));
        cueFileDefinition.setTitle(readProperty(reader,cueFileDefinition,"TITLE ".length()));
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
                thirdLine.indexOf("\"")+1,
                StringUtils.ordinalIndexOf(thirdLine, "\"", 2)
                );
        cueFileDefinition.setFileName(fileName);

        String fileExtension = fileName.substring(fileName.lastIndexOf(".")+1);
        cueFileDefinition.setFileFormat(fileExtension);


        String line;
        Integer currentIndex = -1;
        // TODO adquiere correctamente solo la última canción.
        while (
                (line = reader.readLine()) != null
        ){
            //System.out.println(line.trim().split(" ")[0]);
            switch (
                    line.trim().split(" ")[0]
            ){
                case "TRACK":
                    currentIndex++;
                    cueFileDefinition.getTrackList().add(new Track());
                    cueFileDefinition.getTrackList().get(currentIndex).setTrackNumber(currentIndex+1);
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
                    cueFileDefinition.getTrackList().get(currentIndex).setOffset(
                            line.trim().substring("INDEX 01 ".length())
                    );
            }
        }

        //System.out.print(cueFileDefinition.toString());



        /*
        String line;
        while ((line = reader.readLine()) != null){
            // patrón repetitivo para adquirir la información de las pistas
            }
        }
        */



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
