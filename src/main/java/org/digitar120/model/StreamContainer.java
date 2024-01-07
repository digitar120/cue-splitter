package org.digitar120.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class StreamContainer {
    private String format;
    // Crear heredados para audio, video, etc.

    public StreamContainer(String format){
        this.format = format;
    }
}
