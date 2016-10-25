import java.io.File;
import java.io.Serializable;
import tools.Tools;

import allcommand.Add;
import allcommand.Commit;

public class Gitlet implements Serializable {
    private File gitletFile;

    public static void main(String[] args) {
        String commitPath = ".gitlet/commits.ser";
        String addPath = ".gitlet/add.ser";
        String[] commends = args[0].split(" ");
        Add fileInAdd;
        Commit allCommit;
        switch (commends[0]) {
            case "init":
                new Gitlet();
                allCommit = new Commit();
                fileInAdd = new Add();
                Tools.saveObject(allCommit, commitPath);
                Tools.saveObject(fileInAdd, addPath);
                break;
            case "add":
                fileInAdd = (Add) Tools.loadObject(addPath);
                allCommit = (Commit) Tools.loadObject(commitPath);
                Tools.aplyAdd(args, fileInAdd, allCommit, addPath, commitPath);
                break;
            case "commit":
                Tools.aplyCommit(args, addPath, commitPath);
                break;
            case "log":
                allCommit = (Commit) Tools.loadObject(commitPath);
                allCommit.log();
                break;
            case "rm":
                Tools.aplyRm(args, addPath, commitPath);
                break;
            case "global-log":
                fileInAdd = (Add) Tools.loadObject(addPath);
                allCommit = (Commit) Tools.loadObject(commitPath);
                Tools.getAllLog(allCommit.getAllData());
                break;
            case "status":
                fileInAdd = (Add) Tools.loadObject(addPath);
                allCommit = (Commit) Tools.loadObject(commitPath);
                allCommit.status();
                fileInAdd.status();
                break;
            case "branch":
                Tools.aplyBranch(args, commitPath);
                break;
            case "checkout":
                allCommit = (Commit) Tools.loadObject(commitPath);
                Tools.aplyCheckout(args, commitPath);
                break;
            case "rm-branch":
                allCommit = (Commit) Tools.loadObject(commitPath);
                Tools.aplyRMBranch(args, allCommit, commitPath);
                break;
            case "reset":
                Tools.aplyReset(args, commitPath);
                break;
            case "find":
                Tools.aplyFind(args, commitPath);
                break;
            case "merge":
                Tools.aplyMerge(args, commitPath);
                break;
            case "rebase":
                Tools.aplyRebase(args, commitPath);
                break;
            case "i-rebase":
                Tools.aplyIRebase(args, commitPath);
                break;
            default:
                System.out.println("Unrecognized command.");
                break;
        }
    }

    public Gitlet() {
        gitletFile = new File(".gitlet/");
        if (!gitletFile.exists()) {
            gitletFile.mkdir();
        } else {
            System.out
                    .println("A gitlet version control system already exist.");
        }
    }

}
