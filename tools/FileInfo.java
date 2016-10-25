package tools;

import java.io.File;
import java.io.Serializable;

public class FileInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3668727626909536849L;
    private String fileName;
    private boolean removal;
    private File stordedFile;
    private String realPath;

    // private boolean commited;

    public FileInfo(String addFileName, File addedFile) {
        fileName = addFileName;
        removal = false;
        stordedFile = addedFile;
        realPath = addedFile.getPath();
        // commited = false;
    }

    public String fileName() {
        return fileName;
    }

    public boolean removal() {
        return removal;
    }

    public File storedFile() {
        return stordedFile;
    }

    public void setFileName(String newFileName) {
        fileName = newFileName;
    }

    public void changeFile(File fileIn) {
        stordedFile = fileIn;
    }

    public void changeRevoval(boolean remove) {
        removal = remove;
    }

    public void resetRealPath() {
        stordedFile.getPath();
    }

    public String realPath() {
        return realPath;
    }
}
