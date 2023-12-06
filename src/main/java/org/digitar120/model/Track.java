package org.digitar120.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@Setter
@ToString
public class Track {
    private Integer trackNumber;
    private String performer;
    private String title;
    private LocalTime timeOffset;
}
