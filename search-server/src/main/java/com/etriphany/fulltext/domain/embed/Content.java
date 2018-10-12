package com.etriphany.fulltext.domain.embed;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines any content that can be indexed/searched.
 *
 * @author cadu.goncalves
 *
 */
public class Content implements Serializable {

    // Logical id
    private String id;

    // Content path
    private String path;

    // Content language
    private String language;

    // Content file path
    private transient Path filePath;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Path getFilePath() {
        if (filePath == null) {
            filePath = Paths.get(path);
        }
        return filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean isValid() {
        return id != null && path != null && !id.isEmpty() && !path.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Content)) {
            return false;
        }

        Content that = (Content) o;
        if (!id.equals(that.id)) {
            return false;
        }
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Content:{ id = %s, path = %s }", id == null ? "null" : id, path == null ? "null" : path);
    }
}
