package org.digitar120.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class CueFile {
    private String performer;
    private String title;
    private String fileName;
    private String fileFormat;
    private List<Track> trackList = new ArrayList<>();

    public CueFile(String performer, String title, String fileName, String fileFormat) {
        this.performer = performer;
        this.title = title;
        this.fileName = fileName;
        this.fileFormat = fileFormat;
    }

    public CueFile(String[] mainParameters, String fileFormat) {
        this.performer = mainParameters[0];
        this.title = mainParameters[1];
        this.fileName = mainParameters[2];
        this.fileFormat = fileFormat;
    }
}
