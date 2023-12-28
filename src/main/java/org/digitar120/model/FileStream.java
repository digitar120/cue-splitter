package org.digitar120.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.digitar120.util.UtilityMethods.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class FileStream {
    private String type;
    private String duration;
    private String encoder;
    private String date;
    private String pURL;
    private List<String> synopsis = new ArrayList<>();
    private String comment;
    private List<FileChapter> chapters = new ArrayList<>();

    private void parseChapters(List<String> input){
        // Asume que que input es el contenido de la sección "Chapters"

        String line;
        for(int i = 0; i < input.size(); i+=3){
            // i+=3 porque cada capítulo tiene 3 líneas.

            line = input.get(i).trim();

            chapters.add(new FileChapter(
                    Integer.parseInt(getNthWord(line, 2).substring(StringUtils.indexOf(":", 1))),
                    Long.parseLong(StringUtils.chop(getNthWord(line, 3))),
                    Long.parseLong(StringUtils.chop(getNthWord(line, 5))),
                    input.get(i+2).substring("title           : ".length())
            ));

        }
    }
}
