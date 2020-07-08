/*
 * MainStats.java
 *
 * Copyright (c) 2019 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class MainStats {

  Collection<CFG> large_dyn_cfgs;
  CFG static_cfg;
  Collection<CFG> dyn_cfgs;
  double[] all_linf;
  double[] all_wjac;
  double[] all_l1;
  double epsilon = 0.1; // Math.log(9);
  Map<Node, Integer> real_hist = new HashMap<>(); // Real node coverage
  double globalLS;
  NewLenTar lt;

  public static void main(String[] args) {
    new MainStats().run(args);
  }

  String static_path;
  String dir;
  int trials;

  void parseParams(String[] a) {
    static_path = a[0];
    dir = a[1];
    trials = 100;
    if (a.length > 2) {
      trials = Integer.parseInt(a[2]);
    }
  }

  void init() {
    // compute a global value that can be used by all local
    // randomizers. the value is in globalLS.

    // option 1: worst case: use |N| from the static graph
    globalLS = static_cfg.getNodes().size() - 1;
    // -1 because the start node cannot ever be removed
  }

  void settings() {
    // get all "large" dynamic CFGs
    //    System.out.print("***\nReading large dynamic CFGs from '" + dir + "' ... ");
    //    large_dyn_cfgs = readDynamicCFGsFromXML(dir);

    // find the most popular entry node amond the large dynamic CFGs
    //    String entry_id = decideEntryNode(large_dyn_cfgs);

    // the union of the large dynamic CFGs gives us the static graph
    System.out.print("Creating static CFG ... ");
    // hailong
    //    static_cfg = createAggregateCFG(large_dyn_cfgs, entry_id);
    //    static_cfg = createCompleteCFG(large_dyn_cfgs, dir, entry_id);
    try {
      static_cfg = Util.readCFGFromXML(new FileInputStream(static_path), 1);
      lt = static_cfg.computeDominators();
      //            static_cfg.toDot(System.out);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    // hailong
    //        static_cfg = createAggregateCFG(large_dyn_cfgs, entry_id);
    //    static_cfg = createCompleteCFG(large_dyn_cfgs, dir, entry_id);
    //
    // static_cfg.computeDominators();
    // DiGraph dtree = static_cfg.getDomTree();
    // int[] pernode = new int[static_cfg.getNodes().size() - 1];
    // Map<Node, Integer> pernodeMap = new HashMap<>();
    // int k = 0;
    // for (Node n : static_cfg.getNodes())
    //   if (!n.equals(static_cfg.getRoot()))
    //     pernodeMap.put(n, domSubtreeSize(dtree.getNode(n.getId())));
    // pernodeMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(System.out::println);
    // Arrays.sort(pernode);
    // for (Node n : sortedNodes) {
    //   System.out.println(n.getId() + ":::" + static_cfg.getNodeWeight(n));
    // }

    // get all trimmed dynamic CFGs
    System.out.print("Reading trimmed dynamic CFGs from '" + dir + "' ... ");
    dyn_cfgs = readDynamicCFGsFromXML(dir);

    // make all trimmed dynamic CFGs have entry_id as the entry node;
    // ensure that, in each dynamic CFG, all nodes are reachable
    // from the entry node, and all edges have non-zero weights.
    System.out.print("Checking and fixing trimmed dynamic CFGs ... ");
    checkAndFixDynamic(dyn_cfgs, static_cfg);

    // for each trimmed dynamic CFG: (1) node weights; (2) dominator tree
    computeNodeWeightsAndDominators(dyn_cfgs);

    // hailong
    //    createAggregateCFG(dyn_cfgs, entry_id);

    // the real histogram
    computeRealHist();
  }

  public void run(String[] a) {
    parseParams(a);
    settings();
    init();

    Map<String, List<Double>> n2ls = new HashMap<>();
    n2ls.put("artificial entry", new ArrayList<>());
    for (Node n : static_cfg.getNodes()) n2ls.put(n.getId(), new ArrayList<>());
    for (CFG g : dyn_cfgs)
      for (Map.Entry<String, Integer> e : g.getDomLevels().entrySet())
        n2ls.get(e.getKey()).add((double) e.getValue());
    Map<String, Double> n2avgl = new HashMap<>();
    for (String n : n2ls.keySet()) {
      double[] ls = ArrayUtils.toPrimitive(n2ls.get(n).toArray(new Double[0]));
      Statistics st = new Statistics(ls);
      n2avgl.put(n, st.getMean());
    }

    List<Map.Entry<Node, Integer>> list = new LinkedList<>(real_hist.entrySet());
    list.sort(Map.Entry.comparingByValue());
    for (Map.Entry<Node, Integer> e : list) {
      System.out.println(e.getKey() + "\t" + e.getKey().getName());
      for (double level : n2ls.get(e.getKey().getId())) {
        System.out.println("\tfreq= " + e.getValue() + ", level= " + ((int) level));
      }
//      System.out.println("\tfreq= " + e.getValue() + ", level= " + n2avgl.get(e.getKey().getId()));
    }

    System.exit(0);
  }

  void computeRealHist() {
    for (Node n : static_cfg.getNodes()) real_hist.put(n, 0);
    for (CFG d : dyn_cfgs) {
      if (d.getNodes().size() == 1) {
        System.out.println("Warning: graph with 1 node");
        continue;
      }
      // contribution to the real histogram
      for (Node n : d.getNodes()) real_hist.put(n, 1 + real_hist.get(n));
    }
  }

  Collection<CFG> readDynamicCFGsFromXML(String dir) {
    return readDynamicCFGsFromXML(dir, 1);
  }

  // get all dynamic CFGs from XML
  Collection<CFG> readDynamicCFGsFromXML(String dir, int replication) {
    // System.out.print("***\nReading dynamic CFGs from '" + dir + "' with " + replication + "
    // replication factor ... ");
    Collection<CFG> res = new HashSet<>();
    File[] graphs = new File(dir).listFiles((d, name) -> name.endsWith(".xml"));
    assert graphs != null;
    try {
      for (File xmlf : graphs) {
        for (int i = 0; i < replication; i++) {
          CFG cfg = Util.readCFGFromXML(new FileInputStream(xmlf), 1);
          cfg2file.put(cfg, xmlf.getName());
          res.add(cfg);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    System.out.println(res.size() + " CFGs created successfully.");
    return res;
  }

  // make all dynamic CFGs have entry_id as the entry node; ensure
  // that in each dynamic CFG all nodes are reachable from the entry
  // and all edges have non-zero weights
  void checkAndFixDynamic(Collection<CFG> dyn_cfgs, CFG static_cfg) {
    String entry_id = static_cfg.getRoot().getId();
    int fixed = 0;
    for (CFG d : dyn_cfgs) {
      // fix the entry node if necessary
      if (!d.getRoot().getId().equals(entry_id)) {
        fixed++;
        // reset the root; add artificial edge newroot->oldroot
        Node oldroot = d.getRoot();
        Node newroot = d.getNode(entry_id);
        if (newroot == null) { // this node never showed up in the dynamic graph
          newroot = d.addNode(entry_id);
        }
        d.setRoot(newroot);
        d.addEdge(newroot, oldroot); // artificial edge
      }
      // check that everything is reachable from root and
      // every edge has non-zero weight
      performSanityChecks(d);
    }
    System.out.println("fixed " + fixed + "; sanity checks passed for " + dyn_cfgs.size() + ".");
  }

  // 1) every node should be reachable from the entry node
  // 2) every edge should have non-zero weight
  void performSanityChecks(CFG cfg) {
    for (Edge e : cfg.getEdges())
      if (cfg.getFreq(e) < 1) throw new RuntimeException("Zero-weight edge " + e);
    Set<Node> seen = new HashSet<>();
    List<Node> worklist = new LinkedList<>();
    worklist.add(cfg.getRoot());
    seen.add(cfg.getRoot());
    while (!worklist.isEmpty()) {
      Node n = worklist.remove(0);
      for (Node succ : n.getSuccessors())
        if (!seen.contains(succ)) {
          seen.add(succ);
          worklist.add(succ);
        }
    }
    if (seen.size() != cfg.getNodes().size()) {
      //      cfg.toDot(System.out);
      throw new RuntimeException(
          "Reachable nodes in "
              + cfg2file.get(cfg)
              + ": seen "
              + seen.size()
              + ", total "
              + cfg.getNodes().size());
    }
  }

  // for each dynamic CFG: (1) node weights; (2) dominator tree
  void computeNodeWeightsAndDominators(Collection<CFG> dyn_cfgs) {
    boolean print = false;
    for (CFG d : dyn_cfgs) {
      d.computeNodeWeights();
      d.computeDominators();
      // debugging prints
      if (print) {
        print = false;
        System.out.println("Dynamic CFG");
        d.toDot(System.out, true, true);
        System.out.println("Dominator tree");
        d.getDomTree().toDot(System.out);
      }
    }
  }

  Map<CFG, String> cfg2file = new HashMap<>();
}
