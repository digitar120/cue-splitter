package org.digitar120.model;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class FileStreamInput {
    private int inputIndex;
    private String containerFormat;
    private LocalTime duration;
    private Long startTime; // FFprobe registra el tiempo de inicio solamente en segundos
    private Bitrate bitrate;
    private List<FileChapter> chapters;
    private List<FileStream> fileStreams;

    // private void parseFileStreamInput
}
