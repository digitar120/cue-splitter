package org.digitar120.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Bitrate {
    private Integer bitrate;
    private BitrateUnit bitrateUnit;

    public Bitrate(Integer bitrate, String unit){
        this.bitrate = bitrate;
        switch (unit){
            case "b/s":
                this.bitrateUnit = BitrateUnit.BITS;
                break;

            case "kb/s":
                this.bitrateUnit = BitrateUnit.KILOBITS;
                break;

            case "mb/s":
                this.bitrateUnit = BitrateUnit.MEGABITS;
                break;
        }
    }
}

enum BitrateUnit{
    BITS,
    KILOBITS,
    MEGABITS
}
