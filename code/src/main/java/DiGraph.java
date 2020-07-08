import com.google.common.base.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

public class DiGraph {
  protected Map<String, Node> mNodes = new HashMap<>();
  protected Set<Edge> mEdges = new HashSet<>();
  protected Node mRoot;

  public Node getRoot() {
    return mRoot;
  }

  public void setRoot(Node root) {
    mRoot = root;
  }

  public Node addNode(String id) {
    mNodes.putIfAbsent(id, new Node(id));
    return mNodes.get(id);
  }

  public Node addNode(String id, String name) {
    mNodes.putIfAbsent(id, new Node(id, name));
    return mNodes.get(id);
  }

  public boolean hasNode(String id) {
    return mNodes.containsKey(id);
  }

  public void removeNode(String id) {
    Node n = mNodes.remove(id);
    mEdges.removeIf((Predicate<Edge>) e -> e.getSource().equals(n) || e.getTarget().equals(n));
  }

  // ADDED BY NASKO
  public Node getNode(String id) {
    return mNodes.get(id);
  }

  public Collection<Node> getNodes() {
    return mNodes.values();
  }

  public Edge addEdge(Node src, Node tgt) {
    Edge edge = new Edge(src, tgt);
    if (!mEdges.add(edge)) {
      throw new RuntimeException("Edge already present: " + edge);
    }
    src.addSuccessor(tgt);
    tgt.addPredecessor(src);
    return edge;
  }

  public boolean hasEdge(Node src, Node tgt) {
    return mEdges.contains(new Edge(src, tgt));
  }

  public Collection<Edge> getEdges() {
    return mEdges;
  }

  public void toDot(String s) {
    try {
      File f = new File(s);
      PrintStream p = new PrintStream(f);
      toDot(p);
      p.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void toDot(PrintStream ps) {
    ps.println("digraph g {\n\tedge [arrowhead=vee]");
    int c = 0;
    Map<String, String> n2n = new HashMap<>();
    for (String n : mNodes.keySet()) {
      if (n.equals(mRoot.getId())) {
        ps.println("\tn" + ++c + " [label=\"" + n + "\",peripheries=2]");
      } else {
        ps.println("\tn" + ++c + " [label=\"" + n + "\"]");
      }
      n2n.put(n, "n" + c);
    }
    for (Edge e : mEdges) {
      ps.println("\t" + n2n.get(e.getSource().getId()) + " -> " + n2n.get(e.getTarget().getId()));
    }
    ps.println("}");
  }
}
