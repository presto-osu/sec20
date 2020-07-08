/*
 * CFG.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE
 * in the root directory.
 */

import java.io.*;
import java.util.*;

public class CFG extends DiGraph {

  private Map<Edge, Long> mEdge2Freq = new HashMap<>();
  // ADDED BY NASKO
  private DiGraph dominator_tree;
  private Map<Node, Long> nodeWeights = new HashMap<>();

  // ADDED BY NASKO
  public long getNodeWeight(Node n) {
    return nodeWeights.get(n);
  }

  public Map<Node, Long> getNodeWeights() {
    return nodeWeights;
  }

  // ADDED BY NASKO
  public void resetNodeWeight(Node n, long w) {
    nodeWeights.put(n, w);
  }

  // ADDED BY NASKO
  public DiGraph getDomTree() {
    return dominator_tree;
  }

  public List<String> getDominatees(String n) {
    List<String> dominatees = new LinkedList<>();
    Node node = dominator_tree.getNode(n);
    dominatees.add(node.getId());
    List<Node> workList = new LinkedList<>();
    workList.add(node);
    while (!workList.isEmpty()) {
      Node cur = workList.remove(0);
      dominatees.add(0, cur.getId());
      workList.addAll(cur.getSuccessors());
    }
    return dominatees;
  }

  public Map<String, Integer> getDomLevels() {
    Map<String, Integer> n2l = new HashMap<>();
    List<Node> workList = new LinkedList<>();
    workList.add(dominator_tree.getRoot());
    int level = 0;
    int counter = 1;
    while (!workList.isEmpty()) {
      Node cur = workList.remove(0);
      n2l.put(cur.getId(), level);
      workList.addAll(cur.getSuccessors());
      if (--counter == 0) {
        counter = workList.size();
        level += 1;
      }
    }
    return n2l;
  }

  // ADDED BY NASKO
  public void computeNodeWeights() {
    // assumption: the graph is not going to change in the future.
    // node weight: sum of freq for incoming and outgoing edges.
    for (Node n : getNodes()) nodeWeights.put(n, 0L);
    for (Edge e : getEdges()) {
      long fr_e = getFreq(e);
      Node t = e.getTarget();
      //      Node s = e.getSource();
      //      nodeWeights.put(s, fr_e + nodeWeights.get(s));
      //      if (!s.equals(t)) // avoid double-counting of self edges
      nodeWeights.put(t, fr_e + nodeWeights.get(t));
    }
  }

  public NewLenTar computeDominators() {
    NewLenTar lt = new NewLenTar(this);
    lt.findDominators();

    //    CFG domTree = new CFG();
    dominator_tree = new DiGraph();
    for (Node n : this.getNodes()) dominator_tree.addNode(n.getId());

    dominator_tree.setRoot(dominator_tree.addNode("artificial entry"));
    for (Node n : this.getNodes()) {
      Node d_n = dominator_tree.getNode(n.getId());
      Node d_d = dominator_tree.getNode(lt.getDominator(n).getId());
      dominator_tree.addEdge(d_d, d_n);
    }
    return lt;
  }

  @Override
  public Collection<Edge> getEdges() {
    return mEdge2Freq.keySet();
  }

  @Override
  public boolean hasEdge(Node src, Node tgt) {
    return mEdge2Freq.containsKey(new Edge(src, tgt));
  }

  @Override
  public Edge addEdge(Node src, Node tgt) {
    return addEdge(src, tgt, 1);
  }

  public Edge addEdge(Node src, Node tgt, long freq) {
    if (src == null || tgt == null || freq < 1) return null;
    src.addSuccessor(tgt);
    tgt.addPredecessor(src);
    Edge edge = new Edge(src, tgt);
    mEdge2Freq.put(edge, freq + mEdge2Freq.getOrDefault(edge, 0L));
    return edge;
  }

  public long getFreq(Edge edge) {
    return mEdge2Freq.get(edge);
  }

  public long resetFreq(Edge edge, long freq) {
    return mEdge2Freq.put(edge, freq);
  }

  public CFG copy() {
    CFG cfg = new CFG();
    cfg.mNodes.putAll(this.mNodes);
    cfg.mRoot = this.mRoot;
    return cfg;
  }

  @Override
  public void toDot(PrintStream ps) {
    toDot(ps, true, false);
  }

  public void toDot(PrintStream ps, boolean withFreq, boolean withNodeWeight) {
    ps.println("digraph g {");
    int c = 0;
    Map<String, String> n2n = new HashMap<>();
    for (String n : mNodes.keySet()) {
      if (n.equals(mRoot.getId())) {
        ps.println(
            "\tn"
                + ++c
                + " [label=\""
                + n
                + ((withNodeWeight) ? (":" + nodeWeights.get(getNode(n))) : "")
                + "\",shape=box]");
      } else {
        ps.println(
            "\tn"
                + ++c
                + " [label=\""
                + n
                + ((withNodeWeight) ? (":" + nodeWeights.get(getNode(n))) : "")
                + "\"]");
      }
      n2n.put(n, "n" + c);
    }
    for (Edge e : mEdge2Freq.keySet()) {
      if (withFreq) {
        ps.println(
            "\t"
                + n2n.get(e.getSource().getId())
                + " -> "
                + n2n.get(e.getTarget().getId())
                + " [label=\""
                + mEdge2Freq.get(e)
                + "\"]");
      } else {
        ps.println("\t" + n2n.get(e.getSource().getId()) + " -> " + n2n.get(e.getTarget().getId()));
      }
    }
    ps.println("}");
  }
}
