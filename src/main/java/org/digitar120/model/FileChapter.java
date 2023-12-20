package org.digitar120.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class FileChapter {
    private int index;
    private String start;
    private String end;
    private String title;
}
