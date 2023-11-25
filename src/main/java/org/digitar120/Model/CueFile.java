package org.digitar120.Model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CueFile {
    private String performer;
    private String title;
    private String fileName;
    private String fileFormat;
    private List<Track> trackList;
}
