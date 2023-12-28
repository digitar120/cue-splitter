package org.digitar120.model;

import lombok.Data;

@Data
public class Bitrate {
    private Double bitrate;
    private BitrateUnit bitrateUnit;

}

enum BitrateUnit{
    BITS,
    KILOBITS,
    MEGABITS
}
