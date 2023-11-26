package org.digitar120.Model;

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
}
