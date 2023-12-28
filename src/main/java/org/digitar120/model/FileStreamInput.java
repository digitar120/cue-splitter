package org.digitar120.model;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class FileStreamInput {
    private String type;
    private LocalTime duration;
    private Long startTime; // FFprobe registra el tiempo de inicio solamente en segundos
    private List<FileChapter> chapters;
    private List<FileStream> fileStreams;
}
