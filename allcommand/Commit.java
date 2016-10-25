package allcommand;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.Date;

import tools.DirectedGraph;
import tools.FileInfo;
import tools.Tools;

public class Commit implements Serializable {
    private DirectedGraph<Integer, Integer, Hashtable<String, FileInfo>> myGraph;
    private String currentBranch;
    private Integer currentHead;
    private String commitPathHead = ".gitlet/commit ";

    public Commit() {
        currentBranch = "master";
        myGraph = new DirectedGraph<>(currentBranch);
        currentHead = 0;
        currentHead = myGraph.addData(new Hashtable<String, FileInfo>(), 0,
                currentBranch, "initial commit");
    }

    public void commit(Add thingsToCommit, String commitString) {
        if (thingsToCommit == null || thingsToCommit.allFilesInAdd().isEmpty()) {
            System.out.println("You have nothing to commit.");
            return;
        }
        Hashtable<String, FileInfo> addFiles = thingsToCommit.allFilesInAdd();
        Hashtable<String, FileInfo> tempHashtable = new Hashtable<>();
        if (currentHead == 0) {
            Set<String> addKeySet = addFiles.keySet();
            tempHashtable = (Hashtable<String, FileInfo>) addFiles.clone(); // removal
            for (String key : addKeySet) {
                if (addFiles.get(key).removal()) {
                    tempHashtable.remove(key);
                }
            }
            currentHead = myGraph.addData(tempHashtable, currentHead,
                    currentBranch, commitString);
        } else {
            Boolean haveChanges = false;
            Hashtable<String, FileInfo> oldFiles = (Hashtable<String, FileInfo>) myGraph
                    .getDataAtVertex(currentHead);
            tempHashtable = (Hashtable<String, FileInfo>) oldFiles.clone();
            Set<String> keySetOfAdd = addFiles.keySet();
            for (String key : keySetOfAdd) {
                FileInfo addFileInfo = addFiles.get(key);
                if (oldFiles.containsKey(key)) {
                    FileInfo oldFileInfo = oldFiles.get(key); // compare files
                    File addFile = addFileInfo.storedFile();
                    File oldFile = oldFileInfo.storedFile();
                    Boolean isSame = true;
                    try {
                        isSame = Tools.fileEquals(addFile, oldFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!isSame) { // check removal
                        if (oldFileInfo.removal() && addFileInfo.removal()) {
                            tempHashtable.remove(key);
                            haveChanges = true;
                        } else if (oldFileInfo.removal()
                                && !addFileInfo.removal()) {
                            tempHashtable.put(key, addFileInfo);
                            haveChanges = true;
                        } else if (!oldFileInfo.removal()
                                && addFileInfo.removal()) {
                            tempHashtable.remove(key);
                            haveChanges = true;
                        } else {
                            tempHashtable.put(key, addFileInfo);
                            haveChanges = true;
                        }
                    } else {
                        if (oldFileInfo.removal() && addFileInfo.removal()) {
                            tempHashtable.remove(key);
                            haveChanges = true;
                        } else if (oldFileInfo.removal()
                                && !addFileInfo.removal()) {
                            tempHashtable.put(key, addFileInfo);
                            haveChanges = true;
                        } else if (!oldFileInfo.removal()
                                && addFileInfo.removal()) {
                            tempHashtable.remove(key);
                            haveChanges = true;
                        }
                    }
                } else {
                    if (!addFileInfo.removal()) {
                        tempHashtable.put(key, addFileInfo);
                        haveChanges = true;
                    }
                }
            }
            if (haveChanges) {
                currentHead = myGraph.addData(tempHashtable, currentHead,
                        currentBranch, commitString);
            }
        }
        saveFileToDir(tempHashtable, addFiles);
    }

    private void saveFileToDir(Hashtable<String, FileInfo> tempHashtable,
            Hashtable<String, FileInfo> addFiles) {
        String fileDirPath = commitPathHead + currentHead.toString() + "/";
        File commitDir = new File(fileDirPath);
        if (!commitDir.exists()) {
            commitDir.mkdir();
        }
        for (String eachFileName : addFiles.keySet()) {
            if (tempHashtable.containsKey(eachFileName)) {
                FileInfo eachFileInfo = tempHashtable.get(eachFileName);
                File oriFile = eachFileInfo.storedFile();
                File destFile = new File(fileDirPath + oriFile.getName());
                eachFileInfo.changeFile(destFile);
                try {
                    Files.copy(oriFile.toPath(), destFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    eachFileInfo.resetRealPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean remove(String fileName, Add stordRmFile) throws IOException {
        if (myGraph.getDataAtVertex(currentHead).containsKey(fileName)) {
            FileInfo temp = myGraph.getDataAtVertex(currentHead).get(fileName);
            if (temp != null) {
                myGraph.getDataAtVertex(currentHead).get(fileName)
                        .changeRevoval(true);
                stordRmFile.addFile(temp);
            }
            return true;
        }
        return false;
    }

    public DirectedGraph<Integer, Integer, Hashtable<String, FileInfo>> getAllData() {
        return myGraph;
    }

    public void log() {
        myGraph.logFrom(currentHead);
    }

    public void status() {
        Set<String> branchs = myGraph.allBranches().keySet();
        System.out.println("=== Branches ===");
        System.out.println("*" + currentBranch);
        for (String branch : branchs) {
            if (!branch.equals(currentBranch)) {
                System.out.println(branch);
            }
        }
        System.out.print("\n");
    }

    public Hashtable<String, FileInfo> lastCommit() {
        return myGraph.getDataAtVertex(currentHead);
    }

    public boolean sameAsLastCommit(File fileToAdd) {
        Hashtable<String, FileInfo> lastCommitTable = lastCommit();
        if (lastCommitTable.containsKey(fileToAdd.getPath())) {
            File lastFile = lastCommitTable.get(fileToAdd.getPath())
                    .storedFile();
            Boolean removal = lastCommitTable.get(fileToAdd.getPath())
                    .removal();
            if (removal) {
                return false;
            }
            try {
                if (Tools.fileEquals(lastFile, fileToAdd)) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void find(String commitString) {
        myGraph.find(commitString);
    }

    public boolean reStoreFile(String fileName) {
        Hashtable<String, FileInfo> currCommit = myGraph
                .getDataAtVertex(currentHead);
        if (!currCommit.containsKey(fileName)) {
            return false;
        } else {
            File destFile = new File(fileName);
            FileInfo temp = currCommit.get(fileName);
            try {
                Files.copy(temp.storedFile().toPath(), destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public boolean reStoreBranchFile(String branch) {
        if (branch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return true;
        }
        if (!switchBranch(branch)) {
            return false;
        } else {
            Hashtable<String, FileInfo> currCommit = myGraph
                    .getDataAtVertex(currentHead);
            for (String fileName : currCommit.keySet()) {
                File destFile = new File(fileName);
                FileInfo temp = currCommit.get(fileName);
                try {
                    Files.copy(temp.storedFile().toPath(), destFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    public boolean reStoreFileFromId(String fileName, Integer id) {
        Hashtable<String, FileInfo> currCommit = myGraph.getDataAtVertex(id);
        if (currCommit == null) {
            System.out.println("No commit with that id exists.");
            return false;
        } else {
            if (!currCommit.containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                return false;
            } else {
                File destFile = new File(fileName);
                FileInfo temp = currCommit.get(fileName);
                try {
                    Files.copy(temp.storedFile().toPath(), destFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
    }

    public boolean switchBranch(String branch) {
        TreeMap<String, Integer> branchMap = myGraph.allBranches();
        if (!branchMap.containsKey(branch)) {
            return false;
        } else {
            currentBranch = branch;
            currentHead = branchMap.get(branch);
            return true;
        }
    }

    public void addbranch(String abranch) {
        if (!switchBranch(abranch)) {
            myGraph.addBranch(abranch, currentHead);
        }
    }

    public void removeBranch(String branch) {
        if (branch.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
        }
        if (!myGraph.hasBranch(branch)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            myGraph.allBranches().remove(branch);
        }
    }

    public void reSet(Integer id) {
        Hashtable<String, FileInfo> data = myGraph.getDataAtVertex(id);
        if (data != null) {
            for (String fileName : data.keySet()) {
                FileInfo temp = data.get(fileName);
                Tools.outputFile(fileName, temp);
            }
            currentHead = id;
            myGraph.allBranches().put(currentBranch, id);
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    public void merge(String givenBranch) throws IOException {
        Integer ancester = findAncester(currentBranch, givenBranch);
        if (ancester == -1) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        } else if (ancester == -2) {
            System.out.println("A branch with that name does not exit.");
            return;
        } else {
            Hashtable<String, FileInfo> ancesterData = (Hashtable<String, FileInfo>) myGraph
                    .getDataAtVertex(ancester).clone();
            Hashtable<String, FileInfo> currentData = (Hashtable<String, FileInfo>) myGraph
                    .getDataAtVertex(currentHead).clone();
            Integer givenBranchHead = myGraph.getBranchHead(givenBranch);
            Hashtable<String, FileInfo> givenData = (Hashtable<String, FileInfo>) myGraph
                    .getDataAtVertex(givenBranchHead).clone();
            Set<String> currentKeys = currentData.keySet();
            Set<String> givenKeys = givenData.keySet();
            Set<String> ancesterKeys = ancesterData.keySet();
            for (String currentKey : currentKeys) {
                FileInfo currFile = currentData.get(currentKey);
                if (!ancesterKeys.contains(currentKey)) { // ancestor not
                                                            // contains key
                    if (givenKeys.contains(currentKey)) { // given contains key
                        FileInfo givenFile = givenData.get(currentKey);
                        String conflicted = currentKey + ".conflicted";
                        Tools.outputFile(conflicted, givenFile);
                        givenKeys.remove(currentKey);
                    }
                } else { // ancestor contains key
                    FileInfo ancestorFile = ancesterData.get(currentKey);
                    if (givenKeys.contains(currentKey)) { // same file in
                                                            // given
                        FileInfo givenFile = givenData.get(currentKey);
                        boolean oldAndGiven = ancestorFile.storedFile()
                                .getPath()
                                .equals(givenFile.storedFile().getPath());
                        boolean currAndOld = ancestorFile.storedFile()
                                .getPath()
                                .equals(currFile.storedFile().getPath());
                        boolean currAndGiven = currFile.storedFile().getPath()
                                .equals(givenFile.storedFile().getPath());
                        if (!oldAndGiven && !currAndOld) {
                            String conflicted = currentKey + ".conflicted";
                            Tools.outputFile(conflicted, givenFile);
                        } else if (!oldAndGiven && currAndOld) {
                            Tools.outputFile(currentKey, givenFile);
                        }
                        givenKeys.remove(currentKey);
                    }
                }
            }
            for (String givenString : givenKeys) {
                FileInfo givenFile = givenData.get(givenString);
                if (ancesterData.containsKey(givenString)) {
                    FileInfo ancestorFile = ancesterData.get(givenString);
                    boolean oldAndGiven = ancestorFile.storedFile().getPath()
                            .equals(givenFile.storedFile().getPath());
                    if (!oldAndGiven) {
                        Tools.outputFile(givenString, givenFile);
                    }
                } else {
                    Tools.outputFile(givenString, givenFile);
                }
            }
        }
    }

    private Integer findAncester(String currBranch, String givenBranch) {
        if (currBranch.equals(givenBranch)) {
            return -1;
        }
        if (!myGraph.hasBranch(givenBranch)) {
            return -2;
        }
        Integer currHead = myGraph.getBranchHead(currBranch);
        Integer givenHead = myGraph.allBranches().get(givenBranch);
        Integer sptInteger = findSplitPoint(currHead, givenHead);
        return sptInteger;

    }

    private Integer findSplitPoint(Integer curr, Integer given) {
        if (curr.equals(given)) {
            return curr;
        } else if (curr == 0) {
            return 0;
        } else if (given == 0) {
            return 0;
        } else {
            if (curr < given) {
                Integer givenPar = myGraph.getVertex(given).getParent();
                return findSplitPoint(curr, givenPar);
            } else {
                Integer currPar = myGraph.getVertex(curr).getParent();
                return findSplitPoint(currPar, given);
            }
        }
    }

    public void rebase(String targetBranch) {
        if (!myGraph.hasBranch(targetBranch)) {
            System.out.println("A branch with that name does not exist.");
        } else if (targetBranch.equals(currentBranch)) {
            System.out.println("Cannot rebase a branch onto itself.");
        } else {
            Integer targetBranchhead = myGraph.getBranchHead(targetBranch);
            if (isAncestor(currentHead, targetBranchhead)) {
                System.out.println("Already up-to-date.");
                return;
            } else if (isAncestor(targetBranchhead, currentHead)) {
                currentHead = targetBranchhead;
                return;
            } else {
                Integer ancestorId = findSplitPoint(currentHead,
                        targetBranchhead);
                Set<String> noModifySet = filesNoChcnge(currentHead, ancestorId);
                currentHead = targetBranchhead;
                Set<Integer> pathSet = findRebasePath(currentBranch,
                        targetBranch);
                for (Integer walker : pathSet) {
                    String commitwards = myGraph.getVertex(walker)
                            .getCommitWords();
                    Hashtable<String, FileInfo> currFiles = myGraph
                            .getDataAtVertex(walker);
                    commitForRebase(currFiles, commitwards, noModifySet);
                }
            }
            // upload to working dir
            Hashtable<String, FileInfo> resultTable = myGraph
                    .getDataAtVertex(currentHead);
            Set<String> resultKeysSet = resultTable.keySet();
            for (String theKey : resultKeysSet) {
                FileInfo outputFileInfo = resultTable.get(theKey);
                Tools.outputFile(theKey, outputFileInfo);
            }
        }
    }

    private Set<String> filesNoChcnge(Integer currHead, Integer ancestor) {
        Hashtable<String, FileInfo> ancestorFiles = myGraph
                .getDataAtVertex(ancestor);
        Hashtable<String, FileInfo> currFiles = myGraph
                .getDataAtVertex(currHead);
        Set<String> noModifyFiles = new TreeSet<>();
        for (String key : currFiles.keySet()) {
            if (ancestorFiles.containsKey(key)) {
                FileInfo ancestorFileInfo = ancestorFiles.get(key);
                FileInfo currFileInfo = currFiles.get(key);
                if (ancestorFileInfo.storedFile().getPath()
                        .equals(currFileInfo.storedFile().getPath())) {
                    noModifyFiles.add(key);
                }
            }
        }
        return noModifyFiles;
    }

    private boolean isAncestor(Integer curr, Integer target) {
        Integer walkerInteger = curr;
        while (walkerInteger != 0) {
            if (walkerInteger.equals(target)) {
                return true;
            } else {
                walkerInteger = myGraph.getVertex(walkerInteger).getParent();
            }
        }
        return false;
    }

    private Set<Integer> findRebasePath(String curr, String target) {
        Set<Integer> pathSet = new TreeSet<>();
        Integer ancestor = findAncester(curr, target);
        Integer walker = myGraph.getBranchHead(curr);
        while (walker != ancestor) {
            pathSet.add(walker);
            walker = myGraph.getVertex(walker).getParent();
        }
        return pathSet;
    }

    private void commitForRebase(Hashtable<String, FileInfo> thingsToCommit,
            String commitString, Set<String> noModifySet) {
        if (thingsToCommit == null) {
            System.out.println("error!");
            return;
        }
        Hashtable<String, FileInfo> addFiles = thingsToCommit;
        Hashtable<String, FileInfo> tempHashtable = new Hashtable<>();
        if (currentHead == 0) {
            Set<String> addKeySet = addFiles.keySet();
            tempHashtable = (Hashtable<String, FileInfo>) addFiles.clone();
            // check removal
            for (String key : addKeySet) {
                if (addFiles.get(key).removal()) {
                    tempHashtable.remove(key);
                }
            }
            currentHead = myGraph.addData(tempHashtable, currentHead,
                    currentBranch, commitString);
        } else {
            // check duplicate and check removal
            Boolean haveChanges = false;
            Hashtable<String, FileInfo> oldFiles = (Hashtable<String, FileInfo>) myGraph
                    .getDataAtVertex(currentHead);
            tempHashtable = (Hashtable<String, FileInfo>) oldFiles.clone();
            Set<String> keySetOfAdd = addFiles.keySet();
            for (String key : keySetOfAdd) {
                FileInfo addFileInfo = addFiles.get(key);
                if (oldFiles.containsKey(key)) {
                    // compare files
                    if (!noModifySet.contains(key)) {
                        tempHashtable.put(key, addFileInfo);
                    } else {
                        tempHashtable.put(key, oldFiles.get(key));
                    }

                } else {
                    tempHashtable.put(key, addFileInfo);
                    haveChanges = true;

                }
            }
            currentHead = myGraph.addData(tempHashtable, currentHead,
                    currentBranch, commitString);
        }
    }

    public void irebase(String targetBranch, Scanner scanner) {
        if (!myGraph.hasBranch(targetBranch)) {
            System.out.println("A branch with that name does not exist.");
        } else if (targetBranch.equals(currentBranch)) {
            System.out.println("Cannot rebase a branch onto itself.");
        } else {
            Integer targetBranchhead = myGraph.getBranchHead(targetBranch);
            if (isAncestor(currentHead, targetBranchhead)) {
                System.out.println("Already up-to-date.");
                return;
            } else if (isAncestor(targetBranchhead, currentHead)) {
                currentHead = targetBranchhead;
                return;
            } else {
                Integer ancestorId = findSplitPoint(currentHead,
                        targetBranchhead);
                DateFormat dateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                Set<String> noModifySet = filesNoChcnge(currentHead, ancestorId);
                currentHead = targetBranchhead;
                Set<Integer> pathSet = findRebasePath(currentBranch,
                        targetBranch);
                Integer[] setArray = pathSet
                        .toArray(new Integer[pathSet.size()]);
                for (int i = 0; i < setArray.length; i += 1) {
                    Integer walker = setArray[i];
                    Hashtable<String, FileInfo> currFiles = myGraph
                            .getDataAtVertex(walker);
                    System.out.println("Currently replaying:");
                    System.out.println("Commit " + walker);
                    Date commitDate = myGraph.getVertex(walker).getCommitTime();
                    System.out.println(dateFormat.format(commitDate));
                    String commitwards = myGraph.getVertex(walker)
                            .getCommitWords();
                    System.out.println(commitwards);
                    boolean goodCase = false;
                    while (!goodCase) {
                        System.out.println("Would you like to (c)ontinue, "
                                + "(s)kip this commit,"
                                + " or change this commit's (m)essage?");
                        String input = scanner.nextLine();
                        switch (input) {
                            case "m":
                                System.out.println("Enter a new commit message.");
                                String inputcommit = scanner.nextLine();
                                commitForRebase(currFiles, inputcommit, noModifySet);
                                goodCase = true;
                                break;
                            case "s":
                                if ((i == 0) || (i == (setArray.length - 1))) {
                                    goodCase = false;
                                } else {
                                    goodCase = true;
                                }
                                break;
                            case "c":
                                commitForRebase(currFiles, commitwards, noModifySet);
                                goodCase = true;
                                break;
                            default:
                                goodCase = false;
                                break;
                        }
                    }
                }
                scanner.close();
            }
            Hashtable<String, FileInfo> resultTable = myGraph
                    .getDataAtVertex(currentHead);
            Set<String> resultKeysSet = resultTable.keySet();
            for (String theKey : resultKeysSet) {
                FileInfo outputFileInfo = resultTable.get(theKey);
                Tools.outputFile(theKey, outputFileInfo);
            }
        }
    }

}
