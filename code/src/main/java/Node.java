/*
 * Node.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE
 * in the root directory.
 */

import java.util.HashSet;
import java.util.Set;

public class Node implements Comparable<Node> {
  private String mId;
  private String mName;
  private Set<Node> mSucc = new HashSet<>();
  private Set<Node> mPred = new HashSet<>();

  public Node() {}

  public Node(String id) {
    this.mId = this.mName = id;
  }

  public Node(String id, String name) {
    this.mId = id;
    this.mName = name;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return hashCode() == o.hashCode();
  }

  @Override
  public String toString() {
    return "(Node \"" + mId + "\")";
  }

  public String getId() {
    return mId;
  }

  public String getName() {
    return mName;
  }

  public void setId(String id) {
    this.mId = id;
  }

  public void setName(String name) {
    this.mName = name;
  }

  public void addSuccessor(Node succ) {
    mSucc.add(succ);
  }

  public void addPredecessor(Node pred) {
    mPred.add(pred);
  }

  public Set<Node> getSuccessors() {
    return mSucc;
  }

  public Set<Node> getPredecessors() {
    return mPred;
  }

  @Override
  public int compareTo(Node o) {
    return mId.compareTo(o.mId);
  }
}
