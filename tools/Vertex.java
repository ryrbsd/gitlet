package tools;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Date;

public class Vertex<E, T> implements Serializable {
    private Integer id;
    private T data;
    private Integer parent;
    private Set<Integer> children;
    private String branch; // ?
    private String commitString;
    private Date commitTime;

    public Vertex() {
        id = 0;
        data = null;
        parent = 0;
        children = new TreeSet<Integer>();
        commitString = null;
        branch = null;
    }

    public Vertex(T myData, Integer myParent, String myBranch,
            String commitWords, Date time) {
        id = 0;
        data = myData;
        parent = myParent;
        branch = myBranch;
        commitString = commitWords;
        children = new TreeSet<Integer>();
        commitTime = time;
    }

    public Integer getParent() {
        return parent;
    }

    public Set<Integer> getChildren() {
        return children;
    }

    public Integer getId() {
        return id;
    }

    public String getMyBranch() {
        return branch;
    }

    public String getCommitWords() {
        return commitString;
    }

    public T getData() {
        return data;
    }

    public void setId(Integer newId) {
        id = newId;
    }

    public void addChild(Integer child) {
        if (children == null) {
            children = new TreeSet<>();
            children.add(child);
        }
    }

    public Date getCommitTime() {
        return commitTime;
    }

    public void setCommitString(String newCommitString) {
        commitString = newCommitString;
    }
}
