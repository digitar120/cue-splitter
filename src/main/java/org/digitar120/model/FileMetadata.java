package org.digitar120.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class FileMetadata {
    private List<FileStreamInput> fileStreamInputList;
}
