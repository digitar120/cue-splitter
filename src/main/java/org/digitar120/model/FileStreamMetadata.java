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
public class FileStreamMetadata {
    private String type;
    private String duration;
    private String encoder;
    private String date;
    private String pURL;
    private List<String> synopsis = new ArrayList<>();
    private String comment;
    private List<FileChapter> chapters;
}
