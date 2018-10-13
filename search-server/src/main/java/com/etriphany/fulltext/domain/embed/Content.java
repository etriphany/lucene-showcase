package com.etriphany.fulltext.domain.embed;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines any content that can be indexed/searched.
 *
 * @author cadu.goncalves
 *
 */
@EqualsAndHashCode
@ToString
public class Content implements Serializable {

    // Logical id
    @Getter @Setter
    private String id;

    // Content path
    @Getter @Setter
    private String path;

    // Content language
    @Getter @Setter
    private String language;

    // Content file path
    private transient Path filePath;

    public Path getFilePath() {
        if (filePath == null) {
            filePath = Paths.get(path);
        }
        return filePath;
    }

    public Boolean isValid() {
        return id != null && path != null && !id.isEmpty() && !path.isEmpty();
    }

}
