package edu.ufp.inf.sd.project.server;

import java.io.File;
import java.io.Serializable;

public class Task implements Serializable {

    private String path;

    private File file;

    public Task(String path, File file) {
        this.path = path;
        this.file=file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
