package org.digitar120.model;

import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class FileStreamInput {
    private int inputIndex;
    private String containerFormat;
    private LocalTime duration;
    private LocalTime startTime; // FFprobe registra el tiempo de inicio solamente en segundos
    private Bitrate bitrate;
    private List<FileChapter> chapters = new ArrayList<>();
    private List<FileStream> fileStreams = new ArrayList<>();

    public FileStreamInput(int inputIndex, String containerFormat) {
        this.inputIndex = inputIndex;
        this.containerFormat = containerFormat;
    }
}
