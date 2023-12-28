package org.digitar120.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class FileChapter {
    private int index;
    private LocalTime startTime;
    private LocalTime endTime;
    private String chapterTitle;

    // FFprobe da algunos tiempos solamente en segundos. Por ejemplo: 3m 9s -> 189.000000
    public FileChapter(int index, Long starTime, Long endTime, String chapterTitle){
        this.index = index;
        this.startTime = LocalTime.MIN.plusSeconds(starTime);
        this.endTime = LocalTime.MIN.plusSeconds(endTime);
        this.chapterTitle = chapterTitle;
    }

}
