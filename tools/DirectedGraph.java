package tools;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

public class DirectedGraph<V, E, T> implements Serializable { // V is id E is
                                                                // edges also
                                                                // both Integer
                                                                // T
    // for data
    ArrayList<Vertex<E, T>> allCommitsArray;
    Integer currentIndex;
    TreeMap<String, Integer> branchHeadersMap;

    public DirectedGraph(String defaultBranch) {
        allCommitsArray = new ArrayList<>();
        branchHeadersMap = new TreeMap<String, Integer>();
        branchHeadersMap.put(defaultBranch, 0);
    }

    public Integer addData(T tempFiles, Integer head, String branch,
            String commitString) {
        Vertex<E, T> tempVertex = new Vertex<>(tempFiles, head, branch,
                commitString, new Date());
        allCommitsArray.add(tempVertex);
        if (!branchHeadersMap.containsKey(branch)) {
            throw new RuntimeException("No this branch");
        }

        Integer newHead = allCommitsArray.indexOf(tempVertex);
        if (head > 0) {
            allCommitsArray.get(head).addChild(newHead);
        }
        allCommitsArray.get(newHead).setId(newHead);
        branchHeadersMap.put(branch, newHead);

        return newHead;
    }

    public TreeMap<String, Integer> allBranches() {
        return branchHeadersMap;
    }

    public Integer parentOf(V vertex) {
        return allCommitsArray.get((Integer) vertex).getParent();
    }

    public int numberOfChildrens(V vertex) {
        return allCommitsArray.get((Integer) vertex).getChildren().size();
    }

    public Set<Integer> childrensOf(V vertex) {
        return (Set<Integer>) allCommitsArray.get((Integer) vertex)
                .getChildren();
    }

    public Vertex<E, T> getVertex(Integer index) {
        if (index < allCommitsArray.size()) {
            return allCommitsArray.get(index);
        }
        return null;
    }

    public T getDataAtVertex(Integer index) {
        if (index < allCommitsArray.size()) {
            return allCommitsArray.get(index).getData();
        }
        return null;
    }

    public Boolean containsVertex(Integer index) {
        if (allCommitsArray.size() < index || allCommitsArray.isEmpty()
                || allCommitsArray.get(index) == null) {
            return false;
        }
        return true;
    }

    public ArrayList<Vertex<E, T>> getArray() {
        return allCommitsArray;
    }

    public void logFrom(Integer vertex) {
        Integer walker = vertex;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (walker != 0) {
            Vertex<E, T> temp = allCommitsArray.get(walker);
            System.out.print("\n====\nCommit " + temp.getId() + "\n"
                    + dateFormat.format(temp.getCommitTime()) + "\n"
                    + temp.getCommitWords() + "\n");
            walker = temp.getParent();
        }
        Vertex<E, T> temp = allCommitsArray.get(walker);
        System.out.print("\n====\nCommit " + temp.getId() + "\n"
                + dateFormat.format(temp.getCommitTime()) + "\n"
                + temp.getCommitWords() + "\n");
    }

    public void find(String commitString) {
        Integer findNumber = 0;
        for (Vertex<E, T> temp : allCommitsArray) {
            if (temp.getCommitWords().equals(commitString)) {
                System.out.println(temp.getId());
                findNumber += 1;
            }
        }
        if (findNumber == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void addBranch(String branchName, Integer id) {
        branchHeadersMap.put(branchName, id);
    }

    public Integer getBranchHead(String branch) {
        if (hasBranch(branch)) {
            return branchHeadersMap.get(branch);
        }
        return -1;
    }

    public boolean hasBranch(String branch) {
        if (branchHeadersMap.containsKey(branch)) {
            return true;
        }
        return false;
    }
}
