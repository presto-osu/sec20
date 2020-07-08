import java.io.PrintStream;
import java.util.*;

public class NewLenTar {

  // ---
  public NewLenTar(CFG c) {
    cfg = c;
  }

  // ---
  public Node artEntry() {
    return art_entry;
  }
  // ---
  public Node getDominator(Node n) {
    return get_dom(n);
  }
  // ---
  public boolean isDomAncestor(Node n1, Node n2) {
    // precondition: n1 != n2
    if (n1 == n2) throw new RuntimeException("NewLenTar.isDomAncestor" + " called with n1 == n2");
    // TODO: there has to be a better way to do this. Do some
    // caching of the results? Build an actual dominator tree?

    // is it true that n1 dominates n2? i.e. n1 is an
    // ancestor of n2 in the dominator tree.
    Node n = n2;
    while (n != art_entry) {
      n = getDominator(n);
      if (n == n1) return true;
    }
    return false;
  }

  public void toDot(PrintStream ps) {
    ps.println("digraph g {");

    ps.println("}");
  }
  // ----------------------------------------
  // create a dominator tree for this CFG
  public void findDominators() {

    // artificial entry node
    art_entry = new Node("artificial entry");

    lt_semi = new HashMap<>();
    lt_parent = new HashMap<>();
    lt_vertex = new Node[cfg.getNodes().size() + 5];
    lt_bucket = new HashMap<>();
    lt_dom = new HashMap<>();
    lt_ancestor = new HashMap<>();
    lt_label = new HashMap<>();
    lt_entries = new HashSet<>();

    // ---------------------------------------------------------
    // first, the only "entry" node (the successor of the artificial entry)
    lt_entries.add(cfg.getRoot());

    // --------------------------------------------
    // Initialize some data structs for the LT algo
    for (Iterator<Node> it = cfg.getNodes().iterator(); it.hasNext(); ) {
      Node n = it.next();
      // initialize lt_semi and lt_bucket
      set_semi(n, 0);
      lt_bucket.put(n, new HashSet<>());
    }
    // and for the artificial entry
    set_semi(art_entry, 0);
    lt_bucket.put(art_entry, new HashSet<>());

    // ------------ step 1 ------------------
    // need to do DFS from the artificial entry
    lt_n = 0;
    dfs_visited = new HashSet<Node>(); // for a sanity check: did we visit all CFG nodes?
    lt_dfs(art_entry);
    if (1 + cfg.getNodes().size() != dfs_visited.size()) {
      System.out.println(
          "ERROR EXIT: CFG nodes: "
              + (1 + cfg.getNodes().size())
              + ", DFS visited: "
              + dfs_visited.size());
      System.exit(1);
    }

    // ---------------------------------
    // steps 2 and 3 of LT
    for (int i = lt_n; i > 1; i--) {
      Node w = lt_vertex[i];

      // ------- step 2 --------
      Iterator<Node> it;
      // the set of w's predecessors, taking into accout the artificial entry
      Set<Node> h = getPred(w);
      for (it = h.iterator(); it.hasNext(); ) {
        Node v = it.next();
        if (v == w) continue;
        Node u = lt_eval(v);
        if (get_semi(u) < get_semi(w)) set_semi(w, get_semi(u));
      }

      // add to bucket
      (lt_bucket.get(lt_vertex[get_semi(w)])).add(w);

      // call lt_link
      lt_link(lt_parent.get(w), w);

      // -------- step 3 --------
      Node pa = lt_parent.get(w);
      HashSet<Node> bu = lt_bucket.get(pa);
      for (Iterator<Node> it2 = bu.iterator(); it2.hasNext(); ) {
        Node v = it2.next();
        Node u = lt_eval(v);
        if (get_semi(u) < get_semi(v)) set_dom(v, u);
        else set_dom(v, pa);
      }
      bu.clear();
    }

    // ------------ step 4 ------------------
    for (int i = 2; i <= lt_n; i++) {
      Node w = lt_vertex[i];
      Node x = get_dom(w);
      Node y = lt_vertex[get_semi(w)];
      if (!x.equals(y)) {
        Node z = get_dom(x);
        set_dom(w, z);
      }
    }
  }

  // ----------------------------------------
  protected Set<Node> getPred(Node w) {
    if (lt_entries.contains(w)) {
      // we have to "pretend" that the artificial entry is a predecessor of entry nodes
      HashSet<Node> h = new HashSet<>();
      h.add(art_entry);
      return h;
    }
    return w.getPredecessors();
  }

  // -----------------------------------------
  private void lt_link(Node v, Node w) {
    lt_ancestor.put(w, v);
  }
  // ---------------------------------
  private Node lt_eval(Node v) {
    if (lt_ancestor.containsKey(v)) {
      lt_compress(v);
      return get_label(v);
    } else {
      return v;
    }
  }

  // ----------------------------------
  private void lt_compress(Node v) {
    Node a = lt_ancestor.get(v);

    if (lt_ancestor.containsKey(a)) {

      lt_compress(a);

      if (get_semi(get_label(a)) < get_semi(get_label(v))) set_label(v, get_label(a));

      lt_ancestor.put(v, lt_ancestor.get(a));
    }
  }

  // -------------------------
  protected void lt_dfs(Node v) {
    // System.out.println("dfs: " + v);
    dfs_visited.add(v);
    lt_n++;
    set_semi(v, lt_n);
    lt_vertex[lt_n] = v;
    set_label(v, v);
    Iterator it;
    // artificial entry has as successors all entry nodes
    if (v == art_entry) it = lt_entries.iterator();
    else it = v.getSuccessors().iterator();

    while (it.hasNext()) {
      Node w = (Node) it.next();
      if (w == v) continue;
      if (get_semi(w) == 0) {
        lt_parent.put(w, v);
        lt_dfs(w);
      }
    }
  }

  // ---------------
  protected int get_semi(Node p) {
    return (lt_semi.get(p)).intValue();
  }

  protected void set_semi(Node p, int val) {
    lt_semi.put(p, val);
  }

  protected Node get_dom(Node p) {
    return (lt_dom.get(p));
  }

  protected void set_dom(Node p, Node q) {
    lt_dom.put(p, q);
  }

  protected Node get_label(Node p) {
    return (lt_label.get(p));
  }

  protected void set_label(Node p, Node q) {
    lt_label.put(p, q);
  }

  // for the algorithm
  protected int lt_n;
  protected HashMap<Node, Integer> lt_semi;
  protected HashMap<Node, Node> lt_parent;
  protected Node[] lt_vertex;
  protected HashMap<Node, HashSet<Node>> lt_bucket;
  protected HashMap<Node, Node> lt_dom;
  protected HashMap<Node, Node> lt_ancestor;
  protected HashMap<Node, Node> lt_label;
  protected Node art_entry;
  protected HashSet<Node> lt_entries;
  protected HashSet<Node> dfs_visited;

  // the underlying CFG
  protected CFG cfg;
}
