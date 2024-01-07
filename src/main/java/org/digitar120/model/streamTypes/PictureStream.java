package org.digitar120.model.streamTypes;

import lombok.*;
import org.digitar120.model.StreamContainer;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PictureStream extends StreamContainer {
    private String format;
    private int referencedFrameAmount;
    private String colorFormat;
    private String pictureDimensions;
    private String targetFramerate;
    private String timeBaseNumber;
    private Boolean isAPicture;

    public PictureStream(String format) {
        super(format);
    }
}
