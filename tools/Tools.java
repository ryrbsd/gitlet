package tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

import allcommand.Add;
import allcommand.Commit;

public class Tools implements Serializable {

    public static Boolean fileEquals(File f1, File f2) throws IOException {
        return Arrays.equals(Files.readAllBytes(f1.toPath()),
                Files.readAllBytes(f2.toPath()));
    }

    public static void saveObject(Object objectToSave, String filePath) {
        if (objectToSave == null) {
            return;
        }
        try {
            File outputFile = new File(filePath);
            FileOutputStream fileOut = new FileOutputStream(outputFile);
            ObjectOutputStream hashMapOut = new ObjectOutputStream(fileOut);
            hashMapOut.writeObject(objectToSave);
            fileOut.close();
        } catch (IOException e) {
            String msg = "IOException while saving myCat.";
            System.out.println(msg);
        }
    }

    public static Object loadObject(String filePath) {
        Object outputObject = null;
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream hashMapIn = new ObjectInputStream(fileIn);
            outputObject = hashMapIn.readObject();
        } catch (IOException e) {
            String msg = "IOException while loading addFile.";
            System.out.println(msg);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            String msg = "ClassNotFoundException while loading addFile.";
            System.out.println(msg);
            e.printStackTrace();
        }
        return outputObject;
    }

    public static void getAllLog(
            DirectedGraph<Integer, Integer, Hashtable<String, FileInfo>> dataGraph) {
        ArrayList<Vertex<Integer, Hashtable<String, FileInfo>>> tempList = dataGraph
                .getArray();
        Object[] tempArray = tempList.toArray();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Object eachObject : tempArray) {
            Vertex<Integer, Hashtable<String, FileInfo>> temp;
            temp = (Vertex<Integer, Hashtable<String, FileInfo>>) eachObject;
            System.out.println("====");
            System.out.println("Commit " + temp.getId());

            System.out.println(dateFormat.format(temp.getCommitTime()));
            System.out.println(temp.getCommitWords());
        }
    }

    public static void find(
            DirectedGraph<Integer, Integer, Hashtable<String, FileInfo>> dataGraph,
            String commitString) {
        ArrayList<Vertex<Integer, Hashtable<String, FileInfo>>> tempList = dataGraph
                .getArray();
        Object[] tempArray = tempList.toArray();
        Integer counts = 0;
        for (Object eachObject : tempArray) {
            Vertex<Integer, Hashtable<String, FileInfo>> temp;
            temp = (Vertex<Integer, Hashtable<String, FileInfo>>) eachObject;
            if (temp.getCommitWords().equals(commitString)) {
                System.out.println(temp.getId());
                counts += 1;
            }
        }
        if (counts == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static boolean saftyCall() {
        System.out.println("Warning: The command you entered may alter"
                + " the files in your directory. "
                + "Uncommited changes may be lost. "
                + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.equals("yes")) {
            scanner.close();
            return true;
        } else {
            System.out.println("Did not type 'yes', so aborting.");
            scanner.close();
            return false;
        }
    }

    public static void outputFile(String fileName, FileInfo output) {
        if (!output.removal()) {
            File destFile = new File(fileName);
            try {
                Files.copy(output.storedFile().toPath(), destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readTyped() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        scanner.close();
        return input;
    }

    public static void aplyAdd(String[] args, Add fileInAdd, Commit allCommit,
            String addFilePath, String commitFilePath) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
        } else {
            String fileNameToAdd = args[1];
            File fileToAdd = new File(fileNameToAdd);
            if (!fileToAdd.exists()) {
                System.out.println("There is no file on this path.");
            } else {
                try {
                    boolean same = true;
                    // fileInAdd = (Add) Tools.loadObject(addFilePath);
                    // allCommit = (Commit) Tools.loadObject(commitFilePath);
                    same = allCommit.sameAsLastCommit(fileToAdd);
                    if (same) {
                        System.out
                                .println("File has not been modified since the last commit.");
                    } else {
                        fileInAdd.addFile(fileNameToAdd, fileToAdd);
                        Tools.saveObject(fileInAdd, addFilePath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void aplyCommit(String[] args, String addFilePath,
            String commitFilePath) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
        } else {
            Add fileInAdd = (Add) Tools.loadObject(addFilePath);
            Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
            allCommit.commit(fileInAdd, args[1]);
            Tools.saveObject(allCommit, commitFilePath);
            fileInAdd.destoryAddFiles();
            Tools.saveObject(fileInAdd, addFilePath);
        }
    }

    public static void aplyRm(String[] args, String addFilePath,
            String commitFilePath) {
        try {
            if (args.length < 2) {
                System.out.println("Did not enter enough arguments.");
            } else {
                Add fileInAdd = (Add) Tools.loadObject(addFilePath);
                Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
                if (!fileInAdd.removeFile(args[1])) {
                    allCommit.remove(args[1], fileInAdd);
                }
                Tools.saveObject(allCommit, commitFilePath);
                Tools.saveObject(fileInAdd, addFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void aplyCheckout(String[] args, String commitFilePath) {
        if (Tools.saftyCall()) {
            boolean isBranch;
            boolean isFile;
            Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
            if (args.length < 2) {
                System.out.println("Did not enter enough arguments.");
            } else if (args.length == 3) {
                allCommit.reStoreFileFromId(args[2], Integer.parseInt(args[1]));
            } else {
                isBranch = allCommit.reStoreBranchFile(args[1]);
                isFile = allCommit.reStoreFile(args[1]);
                if (!isBranch && !isFile) {
                    System.out
                            .println("File does not exist in the most recent commit,"
                                    + " or no such branch exists.");
                }
            }
            Tools.saveObject(allCommit, commitFilePath);
        }
    }

    public static void aplyReset(String[] args, String commitFilePath) {
        if (Tools.saftyCall()) {
            if (args.length < 2) {
                System.out.println("Did not enter enough arguments.");
            } else {
                Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
                allCommit.reSet(Integer.parseInt(args[1]));
                Tools.saveObject(allCommit, commitFilePath);
            }
        }
    }

    public static void aplyMerge(String[] args, String commitFilePath) {
        if (Tools.saftyCall()) {
            try {
                if (args.length < 2) {
                    System.out.println("Did not enter enough arguments.");
                } else {
                    Commit allCommit = (Commit) Tools
                            .loadObject(commitFilePath);
                    allCommit.merge(args[1]);
                    Tools.saveObject(allCommit, commitFilePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void aplyRebase(String[] args, String commitFilePath) {
        if (Tools.saftyCall()) {
            if (args.length < 2) {
                System.out.println("Did not enter enough arguments.");
            } else {
                Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
                allCommit.rebase(args[1]);
                Tools.saveObject(allCommit, commitFilePath);
            }
        }
    }

    public static void aplyIRebase(String[] args, String commitFilePath) {
        System.out.println("Warning: The command you entered may alter"
                + " the files in your directory. "
                + "Uncommited changes may be lost. "
                + "Are you sure you want to continue? (yes/no)");
        boolean yesOrNo = false;
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.equals("yes")) {
            yesOrNo = true;
        } else {
            System.out.println("Did not type 'yes', so aborting.");
            yesOrNo = false;
        }
        if (yesOrNo) {
            if (args.length < 2) {
                System.out.println("Did not enter enough arguments.");
            } else {
                Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
                allCommit.irebase(args[1], scanner);
                Tools.saveObject(allCommit, commitFilePath);
            }
        }
    }

    public static void aplyRMBranch(String[] args, Commit allCommit,
            String commitFilePath) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
        } else {
            allCommit.removeBranch(args[1]);
            Tools.saveObject(allCommit, commitFilePath);
        }
    }

    public static void aplyBranch(String[] args, String commitFilePath) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
        } else {
            String branchName = args[1];
            Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
            allCommit.addbranch(branchName);
            Tools.saveObject(allCommit, commitFilePath);
        }
    }

    public static void aplyFind(String[] args, String commitFilePath) {
        if (args.length < 2) {
            System.out.println("Did not enter enough arguments.");
        } else {
            Commit allCommit = (Commit) Tools.loadObject(commitFilePath);
            allCommit.find(args[1]);
        }
    }
}
