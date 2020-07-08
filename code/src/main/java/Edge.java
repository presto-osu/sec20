/*
 * Edge.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE
 * in the root directory.
 */

public class Edge {
  private Node mSrc, mTgt;

  public Edge(Node node, Node succ) {
    mSrc = node;
    mTgt = succ;
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
    return "(Edge " + mSrc + "->" + mTgt + ")";
  }

  public Node getSource() {
    return mSrc;
  }

  public Node getTarget() {
    return mTgt;
  }
}
