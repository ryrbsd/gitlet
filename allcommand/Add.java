package allcommand;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import tools.FileInfo;
import tools.Tools;

public class Add implements Serializable {
    Hashtable<String, FileInfo> allFilesHashMap;

    public Add() {
        allFilesHashMap = new Hashtable<>();

    }

    public Add(Hashtable<String, FileInfo> addHashtable) {
        allFilesHashMap = addHashtable;
    }

    public void addFile(String fileNameString, File fileToAdd)
            throws IOException {
        if (!allFilesHashMap.containsKey(fileNameString)) {
            FileInfo fileInfoToAdd = new FileInfo(fileNameString, fileToAdd);
            allFilesHashMap.put(fileNameString, fileInfoToAdd);
        } else {
            FileInfo oldFileInfo = allFilesHashMap.get(fileNameString);
            File oldFile = oldFileInfo.storedFile();
            try {
                boolean same = Tools.fileEquals(oldFile, fileToAdd);
                if (same) {
                    if (allFilesHashMap.get(fileNameString).removal()) {
                        allFilesHashMap.get(fileNameString)
                                .changeRevoval(false);
                    } else {
                        System.out
                                .println("File has not been modified since the last commit.");
                    }
                } else {
                    allFilesHashMap.put(fileNameString, new FileInfo(
                            fileNameString, fileToAdd));
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    public void addFile(FileInfo fileInfo) throws IOException {
        String fileNameString = fileInfo.fileName();
        if (!allFilesHashMap.containsKey(fileNameString)) {
            allFilesHashMap.put(fileNameString, fileInfo);
        } else {
            FileInfo oldFileInfo = allFilesHashMap.get(fileNameString);
            File oldFile = oldFileInfo.storedFile();
            try {
                boolean same = Tools.fileEquals(oldFile, fileInfo.storedFile());
                if (same) {
                    if (allFilesHashMap.get(fileNameString).removal()) {
                        allFilesHashMap.get(fileNameString)
                                .changeRevoval(false);
                    } else {
                        System.out
                                .println("File has not been modified since the last commit.");
                    }
                } else {
                    allFilesHashMap.put(fileNameString, fileInfo);
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    public Hashtable<String, FileInfo> allFilesInAdd() {
        return allFilesHashMap;
    }

    public void destoryAddFiles() {
        allFilesHashMap = new Hashtable<>();
    }

    public boolean removeFile(String fileName) {
        if (allFilesHashMap.containsKey(fileName)) {
            allFilesHashMap.remove(fileName);
            return true;
        }
        return false;
    }

    public void status() {
        Set<String> fileInfos = allFilesHashMap.keySet();
        TreeSet<String> fileRemoval = new TreeSet<>();
        System.out.println("=== Staged Files ===");
        for (String fileString : fileInfos) {
            FileInfo fileInfo = allFilesHashMap.get(fileString);
            if (!fileInfo.removal()) {
                System.out.println(fileInfo.fileName());
            } else {
                fileRemoval.add(fileInfo.fileName());
            }
        }
        System.out.print("\n");
        System.out.println("=== Files Marked for Removal ===");
        for (String removalFileName : fileRemoval) {
            System.out.println(removalFileName);
        }
    }
}
