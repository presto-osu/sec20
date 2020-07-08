import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.*;
import com.opencsv.*;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {

  Collection<CFG> large_dyn_cfgs;
  CFG static_cfg;
  Collection<CFG> dyn_cfgs;
  double[] all_linf;
  double[] all_wjac;
  double[] all_l1;
  double[] all_linf_true;
  double[] all_wjac_true;
  double[] all_l1_true;
  double[] all_me;
  double[] all_toptau_precision;
  double[] all_toptau_recall;
  double[] all_toptau_f_score;
  double[] all_coverage;
  double[] all_precision;
  double[] all_recall;
  double[] all_fmeasure;
  double[] all_mre;
  double[] all_re;
  Map<Node, Double[]> all_error_pernode;
  double[][] all_hot_node_coverage;
  double[][] all_hot_recall;
  double[][] all_hot_l1;
  double[][] all_hot_linf;
  double[][] all_hot_wjac;
  double[][] all_hot_l1_true;
  double[][] all_hot_me;
  double[][] all_hot_linf_true;
  double[][] all_hot_wjac_true;
  double epsilon = 0.1; // Math.log(9);
  Map<Node, Integer> real_hist = Maps.newHashMap(); // Real node coverage
  double globalLS;
  NewLenTar lt;
  private String app;
  int replication = 1;
  private String adjDir = String.format("dataset.adj.%dk", replication);
  protected String tag;
  private String type;

  public static void main(String[] args) {
    new Main().run(args);
  }

  String static_path;
  String dir;
  int trials;

  void parseParams(String[] a) {
    static_path = a[0];
    dir = a[1];
    String[] parts = dir.split("/");
    app = parts[parts.length - 1];
    trials = 100;
    epsilon = 0.1;
    type = a[2];
    trials = Integer.parseInt(a[3]);
    epsilon = Double.parseDouble(a[4]);
  }

  void init() {
    tag = "baseline";
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
//      static_cfg.getDomTree().toDot(System.out);
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

    // run actual experiments
    System.out.print("Running " + trials + " trials with epsilon ");
    System.out.printf("%.3f", epsilon);
    System.out.println(" and sensitivity " + globalLS);
    all_linf = new double[trials];
    all_wjac = new double[trials];
    all_l1 = new double[trials];
    all_linf_true = new double[trials];
    all_wjac_true = new double[trials];
    all_l1_true = new double[trials];
    all_coverage = new double[trials];
    all_precision = new double[trials];
    all_recall = new double[trials];
    all_fmeasure = new double[trials];
    all_me = new double[trials];
    all_re = new double[trials];
    all_mre = new double[trials];
    all_error_pernode = Maps.newHashMap();
    for (Node n : static_cfg.getNodes()) {
      all_error_pernode.put(n, new Double[trials]);
    }

    all_toptau_precision = new double[trials];
    all_toptau_recall = new double[trials];
    all_toptau_f_score = new double[trials];
    all_hot_node_coverage = new double[5][trials];
    all_hot_recall = new double[5][trials];
    all_hot_l1 = new double[5][trials];
    all_hot_linf = new double[5][trials];
    all_hot_wjac = new double[5][trials];
    all_hot_l1_true = new double[5][trials];
    all_hot_me = new double[5][trials];
    all_hot_linf_true = new double[5][trials];
    all_hot_wjac_true = new double[5][trials];

    int[] tarr = new int[trials];
    for (int j = 0; j < trials; j++) tarr[j] = j;
    Arrays.stream(tarr)
        .parallel()
        .forEach(
            new IntConsumer() {
              // Arrays.stream(tarr).forEach(new IntConsumer() {
              @Override
              public void accept(int j) {
                System.out.print(".");
                runTrial(j);
              }
            });
    //    for (int j = 0; j < trials; j++) {
    //      System.out.print(".");
    //      runTrial(j);
    //    }
    // Arrays.sort(all_linf);
    // Arrays.sort(all_wjac);
    // System.out.printf("%f %f\n", median(all_linf), median(all_wjac));

    stats();
  }

  void stats() {
    System.out.println("\nApp:\t" + dir + "\t" + globalLS);
    long real_sum = 0;
    for (Node n : static_cfg.getNodes())
      real_sum += real_hist.get(n);
    System.out.println("real_sum:\t" + real_sum);
    int num = 0;
    for (Node n : static_cfg.getNodes()) {
      if (real_hist.get(n) > 0) num += 1;
    }
    System.out.println("positives:\t" + num + "/" + static_cfg.getNodes().size());
    
    {
      // Statistics s = new Statistics(all_coverage);
      // System.out.println("mean_coverage:\t" + s.getMean());
      // System.out.println("error_coverage:\t" + s.getConfidenceInterval95());
      // System.out.println("stddev_coverage:\t" + s.getStdDev());
    }
    {
      // Statistics s = new Statistics(all_fmeasure);
      // System.out.println("mean_fmeasure:\t" + s.getMean());
      // System.out.println("error_fmeasure:\t" + s.getConfidenceInterval95());
      // System.out.println("stddev_fmeasure:\t" + s.getStdDev());
    }
    {
      Statistics s = new Statistics(all_precision);
      System.out.println("mean_precision:\t" + s.getMean());
      System.out.println("error_precision:\t" + s.getConfidenceInterval95());
      // System.out.println("stddev_precision:\t" + s.getStdDev());
    }
    {
      Statistics s = new Statistics(all_recall);
      System.out.println("mean_recall:\t" + s.getMean());
      System.out.println("error_recall:\t" + s.getConfidenceInterval95());
      // System.out.println("stddev_recall:\t" + s.getStdDev());
    }
    {
      // Statistics s = new Statistics(all_mre);
      // System.out.println("mean_mre:\t" + s.getMean());
      // System.out.println("error_mre:\t" + s.getConfidenceInterval95());
      // System.out.println("stddev_mre:\t" + s.getStdDev());
    }
    {
      StringBuilder sb = new StringBuilder("re");
      for (Node n : static_cfg.getNodes()) {
        Statistics s = new Statistics(ArrayUtils.toPrimitive(all_error_pernode.get(n)));
        double mean = s.getMean(), stddev = s.getStdDev();
        sb.append("\n").append(mean);
      }
      File f = new File(String.format("re/%s_%s_%s.csv", app, tag, type));
      try (FileWriter fw = new FileWriter(f)) {
        fw.write(sb.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
      //      System.out.println(sb);
    }
    //    {
    //      Statistics slinf = new Statistics(all_linf);
    //      System.out.println("mean_linf:\t" + slinf.getMean());
    //      System.out.println("error_linf:\t" + slinf.getStdDev());
    //    }
    //    {
    //      Statistics slinf = new Statistics(all_linf_true);
    //      System.out.println("mean_linftrue:\t" + slinf.getMean());
    //      System.out.println("error_linftrue:\t" + slinf.getStdDev());
    //    }
    //
    {
      Statistics s = new Statistics(all_me);
      System.out.println("mean_me:\t" + s.getMean());
      System.out.println("error_me:\t" + s.getConfidenceInterval95());
      // System.out.println("stddev_me:\t" + sl1.getStdDev());
    }

    //    {
    //      Statistics swjac = new Statistics(all_wjac);
    //      System.out.println("mean_wjac:\t" + swjac.getMean());
    //      System.out.println("error_wjac:\t" + swjac.getStdDev());
    //    }
    //    {
    //      Statistics swjac = new Statistics(all_wjac_true);
    //      System.out.println("mean_wjactrue:\t" + swjac.getMean());
    //      System.out.println("error_wjactrue:\t" + swjac.getStdDev());
    //    }

    // statsHot(all_hot_node_coverage, "hotcov");
    statsHot(all_hot_recall, "hotrecall");
    statsHot(all_hot_me, "hotme");
    //    statsHot(all_hot_linf, "hotlinf");
    //    statsHot(all_hot_l1, "hotl1");
    //    statsHot(all_hot_wjac, "hotwjac");
    //    statsHot(all_hot_linf_true, "hotlinftrue");
    //    statsHot(all_hot_l1_true, "hotl1true");
    //    statsHot(all_hot_wjac_true, "hotwjactrue");

    //    Statistics sp = new Statistics(all_toptau_precision);
    //    Statistics sr = new Statistics(all_toptau_recall);
    //    Statistics sf = new Statistics((all_toptau_f_score));
    //    System.out.println("accuracy: " + sf.getMean() + " " + sp.getMean() + " " + sr.getMean());
  }

  void statsHot(double[][] data, String tag) {
    StringBuffer meanSB = new StringBuffer("mean_").append(tag).append(":");
    StringBuffer errorSB = new StringBuffer("error_").append(tag).append(":");
    // StringBuffer stddevSB = new StringBuffer("stddev_").append(tag).append(":");
    for (int idx = data.length - 1; idx >= 0; idx -= 1) {
      Statistics sHot = new Statistics(data[idx]);
      meanSB.append("\t").append(sHot.getMean());
      errorSB.append("\t").append(sHot.getConfidenceInterval95());
      // stddevSB.append("\t").append(sHot.getStdDev());
    }
    System.out.println(meanSB);
    System.out.println(errorSB);
    // System.out.println(stddevSB);
  }

  CSVParser parser = new CSVParserBuilder().withSeparator('\t').withQuoteChar(' ').build();

  Object[] runTrial(int trial_num) {
    Map<Node, Integer> adj_hist = new HashMap<>(); // Aggregate node coverage after post-processing
    for (Node n : static_cfg.getNodes()) {
      adj_hist.put(n, 0);
    }
    String adjPath = String.format("%s/%s_%s_%s_%d.csv", adjDir, app, tag, type, trial_num);
    if (tag.equals("relaxed") && (globalLS > 1.1 || globalLS < 0.9))
      adjPath = String.format("%s/%s_%s_%s_%d_%d.csv", adjDir, app, tag, type, (int) globalLS, trial_num);

    if (new File(adjPath).exists()) {
      try (CSVReader reader =
          new CSVReaderBuilder(new FileReader(adjPath)).withCSVParser(parser).build()) {
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
          String id = nextLine[0].trim();
          int freq = Integer.parseInt(nextLine[1]);
          adj_hist.put(static_cfg.getNode(id), freq);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      Map<Node, Integer> serv_hist =
          new HashMap<>(); // Aggregate node coverage reported to the server
      for (Node n : static_cfg.getNodes()) {
        serv_hist.put(n, 0);
      }
      double z = Math.exp(epsilon / globalLS);
      double p1 = z / (1 + z);
      double p2 = 1 / (1 + z);
      // System.out.println("p1: " + p1 + ", p2: " + p2);
      // compute the aggregate "raw" histogram at the server
      dyn_cfgs.forEach(
          new Consumer<CFG>() {
            @Override
            public void accept(CFG d) {
              // compute the output of the local randomizer
              for (Node mmm : static_cfg.getNodes()) {
                if (d.getNodes().contains(mmm)) {
                  // 1 bit
                  if (Math.random() < p1) { // keep the 1 as 1
                    serv_hist.put(mmm, 1 + serv_hist.get(mmm));
                  }
                } else {
                  // 0 bit
                  if (Math.random() < p2) { // flip the 0 to 1
                    serv_hist.put(mmm, 1 + serv_hist.get(mmm));
                  }
                }
              }
            }
          });
      //    for (CFG d : dyn_cfgs) {
      //      // compute the output of the local randomizer
      //      for (Node mmm : static_cfg.getNodes()) {
      //        if (d.getNodes().contains(mmm)) {
      //          // 1 bit
      //          if (Math.random() < p1) { // keep the 1 as 1
      //            serv_hist.put(mmm, 1 + serv_hist.get(mmm));
      //          }
      //        } else {
      //          // 0 bit
      //          if (Math.random() < p2) { // flip the 0 to 1
      //            serv_hist.put(mmm, 1 + serv_hist.get(mmm));
      //          }
      //        }
      //      }
      //    }
      // post-process at the server
      for (Node mmm : static_cfg.getNodes()) {
        double x = serv_hist.get(mmm);
        int estimate = (int) Math.round(((z + 1) * x - dyn_cfgs.size()) / (z - 1));
        if (estimate < 0) estimate = 0;
        if (estimate > dyn_cfgs.size()) estimate = dyn_cfgs.size();
        adj_hist.put(mmm, estimate);
      }
      dumpProfile(adj_hist, adjPath);
    }

    computeAccuracy(trial_num, adj_hist);
    return null;
  }

  void dumpProfile(Map<Node, Integer> p, String fileName) {
    try (ICSVWriter writer =
        new CSVWriterBuilder(new FileWriter(fileName)).withParser(parser).build()) {
      for (Map.Entry<Node, Integer> e : p.entrySet()) {
        String[] line = new String[] {e.getKey().getId(), e.getValue().toString()};
        writer.writeNext(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void computeAccuracy(int trial_num, Map<Node, Integer> adj_hist) {
    // compute coverage
    {
      Set<Node> adjcovered =
          adj_hist.entrySet().stream()
              .filter(e -> e.getValue() > 0)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
      Set<Node> realcovered =
          real_hist.entrySet().stream()
              .filter(e -> e.getValue() > 0)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
      int true_positives = Sets.intersection(adjcovered, realcovered).size();
      long false_positives = adjcovered.stream().filter(n -> !realcovered.contains(n)).count();
      long false_negatives = realcovered.stream().filter(n -> !adjcovered.contains(n)).count();
      double precision = ((double) true_positives) / (true_positives + false_positives);
      double recall = ((double) true_positives) / (true_positives + false_negatives);
      all_precision[trial_num] = precision;
      all_recall[trial_num] = recall;
      all_fmeasure[trial_num] = 2 * precision * recall / (precision + recall);
      all_coverage[trial_num] = ((double) adjcovered.size()) / realcovered.size();
    }

    long real_sum = 0;
    for (Node n : real_hist.keySet()) real_sum += real_hist.get(n);
    long adj_sum = 0;
    for (Node n : adj_hist.keySet()) adj_sum += adj_hist.get(n);
    // System.out.println("Real sum: " + real_sum + ", adj sum: " + adj_sum);

    // compute hot node related metrics
    for (int idx = 0; idx < 5; idx += 1) {
      double tau = idx * 0.25;
      Map<Node, Integer> real_hot = hot(tau, real_hist);
      Map<Node, Integer> adj_hot = hot(tau, adj_hist);
      Sets.SetView<Node> intersect = Sets.intersection(real_hot.keySet(), adj_hot.keySet());
      all_hot_node_coverage[idx][trial_num] = ((double) intersect.size()) / real_hot.size();


      int true_positives = Sets.intersection(adj_hot.keySet(), real_hot.keySet()).size();
      // long false_positives = adjcovered.stream().filter(n -> !realcovered.contains(n)).count();
      long false_negatives = real_hot.keySet().stream().filter(n -> !adj_hot.keySet().contains(n)).count();
      all_hot_recall[idx][trial_num] = ((double) true_positives) / (true_positives + false_negatives);

      double linf = 0., linf_true = 0.;
      double l1 = 0., l1_true = 0.;
      double wjac1 = 0., wjac1_true = 0.;
      double wjac2 = 0., wjac2_true = 0.;
      for (Node mmm : real_hot.keySet()) {
        {
          double x = rel(real_hist.get(mmm), real_sum);
          double y = rel(adj_hist.get(mmm), adj_sum);
          // System.out.print(mmm + ": ");
          // System.out.printf("%.3f, %.3f\n", x, y);
          double diff = Math.abs(x - y);
          if (linf < diff) linf = diff;
          l1 += diff;
          wjac1 += Math.min(x, y);
          wjac2 += Math.max(x, y);
        }
        {
          double x = real_hist.get(mmm);
          double y = adj_hist.get(mmm);
          double diff = Math.abs(x - y);
          if (linf_true < diff) linf_true = diff;
          l1_true += diff;
          wjac1_true += Math.min(x, y);
          wjac2_true += Math.max(x, y);
        }
      }
      all_hot_linf[idx][trial_num] = linf;
      all_hot_wjac[idx][trial_num] = 1 - wjac1 / wjac2;
      all_hot_l1[idx][trial_num] = l1;
      all_hot_linf_true[idx][trial_num] = linf_true;
      all_hot_wjac_true[idx][trial_num] = 1 - wjac1_true / wjac2_true;
      all_hot_l1_true[idx][trial_num] = l1_true;
      all_hot_me[idx][trial_num] = l1_true / static_cfg.getNodes().size();
    }

    // accuracy: L infinity and weighted Jaccard
    double linf = 0., linf_true = 0.;
    double l1 = 0., l1_true = 0.;
    double wjac1 = 0., wjac1_true = 0.;
    double wjac2 = 0., wjac2_true = 0.;
    for (Node mmm : static_cfg.getNodes()) {
      {
        double x = rel(real_hist.get(mmm), real_sum);
        double y = rel(adj_hist.get(mmm), adj_sum);
        // System.out.print(mmm + ": ");
        // System.out.printf("%.3f, %.3f\n", x, y);
        double diff = Math.abs(x - y);
        if (linf < diff) linf = diff;
        l1 += diff;
        wjac1 += Math.min(x, y);
        wjac2 += Math.max(x, y);
      }
      {
        int x = real_hist.get(mmm);
        if (x == 0) x = 1;
        int y = adj_hist.get(mmm);
        double diff = Math.abs(x - y);
        all_error_pernode.get(mmm)[trial_num] = diff;
        if (linf_true < diff) linf_true = diff;
        l1_true += diff;
        wjac1_true += Math.min(x, y);
        wjac2_true += Math.max(x, y);
      }
    }
    all_linf[trial_num] = linf;
    all_wjac[trial_num] = 1 - wjac1 / wjac2;
    all_l1[trial_num] = l1;
    all_linf_true[trial_num] = linf_true;
    all_wjac_true[trial_num] = 1 - wjac1_true / wjac2_true;
    all_l1_true[trial_num] = l1_true;
    all_me[trial_num] = l1_true / static_cfg.getNodes().size();
    all_re[trial_num] = l1_true / real_sum;
    all_mre[trial_num] = l1_true / real_sum / static_cfg.getNodes().size();

    // others
    //    Set<Node> trueTopTau =
    //        Sets.newHashSet(topKFrequent((int) Math.round(0.1 * real_hist.size()), real_hist));
    //    Set<Node> predTopTau =
    //        Sets.newHashSet(topKFrequent((int) Math.round(0.1 * adj_hist.size()), adj_hist));
    //    int tp = 0;
    //    int fp = 0;
    //    int fn = 0;
    //    for (Node v : predTopTau)
    //      if (trueTopTau.contains(v)) tp += 1;
    //      else fp += 1;
    //    for (Node v : trueTopTau) if (!predTopTau.contains(v)) fn += 1;
    //    //    System.out.println("tp=" + tp + " fp=" + fp + " fn=" + fn);
    //    double precision = (double) tp / (tp + fp);
    //    double recall = (double) tp / (tp + fn);
    //    all_toptau_precision[trial_num] = precision;
    //    all_toptau_recall[trial_num] = recall;
    //    all_toptau_f_score[trial_num] = 2 * precision * recall / (precision + recall);
  }

  Map<Node, Integer> hot(double threshold, Map<Node, Integer> hist) {
    double max = threshold * hist.values().parallelStream().max(Integer::compareTo).get();
    Map<Node, Integer> ret = Maps.newHashMap();
    hist.forEach(
        (key, value) -> {
          if (value >= max) ret.put(key, value);
        });
    return ret;
  }

  public List<Node> topKFrequent(int k, Map<Node, Integer> hist) {
    List<Node> res = Lists.newLinkedList();
    if (k < 1) return res;
    //    Random rand = new Random();
    PriorityQueue<Map.Entry<Node, Integer>> pq =
        new PriorityQueue<>(k, Comparator.comparingInt(Map.Entry::getValue));
    //                a.getValue().equals(b.getValue())
    //                    ? 2 * rand.nextInt(2) - 1 //b.getKey().compareTo(a.getKey())
    //                    : (int) (a.getValue() - b.getValue()));
    for (Map.Entry<Node, Integer> e : hist.entrySet()) {
      pq.add(e);
      if (pq.size() > k) pq.poll();
    }
    assert pq.peek() != null;
    long leastFreq = pq.peek().getValue();
    //        System.out.println("least freq: " + leastFreq);

    while (!pq.isEmpty()) {
      if (pq.peek().getValue() > leastFreq) leastFreq = pq.peek().getValue();
      Map.Entry<Node, Integer> e = pq.poll();
      //            if (e.getValue() != 0)
      res.add(0, e.getKey());
    }
    //        System.out.println("most freq: " + leastFreq);

    //    for (Map.Entry<String, Long> e : funcProfiles.entrySet())
    //      if (e.getValue() == leastFreq) res.add(e.getKey());
    return res;
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
    return readDynamicCFGsFromXML(dir, replication);
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
    //    File f = new File(dir + "/list.txt");
    //    String st;
    //    try {
    //      BufferedReader br = new BufferedReader(new FileReader(f));
    //      while ((st = br.readLine()) != null) {
    //        File xmlf = new File(dir + "/" + st);
    //        for (int i = 0; i < replication; i++) {
    //          CFG cfg = Util.readCFGFromXML(new FileInputStream(xmlf));
    //          cfg2file.put(cfg, xmlf.getName());
    //          res.add(cfg);
    //        }
    //      }
    //    } catch (IOException e) {
    //      throw new RuntimeException(e);
    //    }
    System.out.println(res.size() + " CFGs created successfully.");
    return res;
  }

  // not all dynamic CFGs have the same entry node; find the most
  // common entry node among all of them
  String decideEntryNode(Collection<CFG> dyn_cfgs) {
    Map<String, Integer> entry_counts = new HashMap<>();
    System.out.print("Finding entry node ... ");
    for (CFG cfg : dyn_cfgs) {
      String entry_id = cfg.getRoot().getId();
      Integer c = entry_counts.get(entry_id);
      int newc;
      if (c == null) newc = 1;
      else newc = 1 + c.intValue();
      entry_counts.put(entry_id, newc);
    }
    String maxs = "***";
    int maxc = 0;
    for (String s : entry_counts.keySet())
      if (entry_counts.get(s) > maxc) {
        maxc = entry_counts.get(s);
        maxs = s;
      }
    System.out.println("entry node \"" + maxs + "\" with " + maxc + " witnesses.");

    return maxs;
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

  CFG createAggregateCFG(Collection<CFG> dyn_cfgs, String entry_id) {
    // System.out.print("Creating aggregate CFG ... ");
    CFG cfg = new CFG();
    cfg.setRoot(cfg.addNode(entry_id));
    for (CFG c : dyn_cfgs) for (Node n : c.getNodes()) cfg.addNode(n.getId());
    for (CFG c : dyn_cfgs)
      for (Edge e : c.getEdges()) {
        String src = e.getSource().getId();
        String tgt = e.getTarget().getId();
        long fr = c.getFreq(e);
        cfg.addEdge(cfg.getNode(src), cfg.getNode(tgt), fr);
      }
    cfg.computeNodeWeights();
    printCFGInfo(cfg);
    performSanityChecks(cfg);
    System.out.println("; sanity checks passed.");
    return cfg;
  }

  // hailong
  CFG createCompleteCFG(Collection<CFG> dyn_cfgs, String dir, String entry_id) {
    String dictFile = "dictionary/" + dir + ".xml";
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    Set<String> dict = new HashSet<>();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new File(dictFile));
      doc.getDocumentElement().normalize();
      org.w3c.dom.Node node = doc.getElementsByTagName("universe").item(0);
      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        org.w3c.dom.Node curNode = children.item(i);
        String nodeName = curNode.getNodeName();
        if (nodeName.equals("name")) {
          dict.add(curNode.getTextContent());
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    //    System.out.print("size of dictionary: " + dict.size() + ", ");

    CFG cfg = new CFG();
    cfg.setRoot(cfg.addNode(entry_id));
    for (String d : dict) cfg.addNode(d);
    for (Node s : cfg.getNodes()) {
      for (Node t : cfg.getNodes()) {
        cfg.addEdge(s, t);
      }
    }
    for (CFG c : dyn_cfgs)
      for (Edge e : c.getEdges()) {
        String src = e.getSource().getId();
        String tgt = e.getTarget().getId();
        long fr = c.getFreq(e);
        cfg.addEdge(cfg.getNode(src), cfg.getNode(tgt), fr);
      }
    cfg.computeNodeWeights();
    printCFGInfo(cfg);
    performSanityChecks(cfg);
    System.out.println("; sanity checks passed.");
    return cfg;
  }

  void printCFGInfo(CFG cfg) {
    long total_e_freq = 0;
    long total_e_self = 0;
    for (Edge e : cfg.getEdges()) {
      total_e_freq += cfg.getFreq(e);
      if (e.getSource().equals(e.getTarget())) total_e_self++;
    }
    long total_n_freq = 0;
    for (Node n : cfg.getNodes()) total_n_freq += cfg.getNodeWeight(n);
    System.out.print(
        "nodes "
            + cfg.getNodes().size()
            + ", edges "
            + cfg.getEdges().size()
            + ", selfedges "
            + total_e_self
            + ", edgeweights "
            + total_e_freq
            + ", nodeweights "
            + total_n_freq);
  }

  long laplace(double beta) {
    double x1 = beta * Math.log(Math.random());
    double x2 = beta * Math.log(Math.random());
    return Math.round(x1 - x2);
  }

  double rel(long x, long y) {
    double res = ((double) x) / ((double) y);
    return res;
  }

  double[] computeAccuracy(CFG g1, CFG g2) {
    double[] res = new double[2];
    // need to consider normalized node/edge weights
    long g1_total_e = 0;
    for (Edge e : g1.getEdges()) g1_total_e += g1.getFreq(e);
    long g2_total_e = 0;
    for (Edge e : g2.getEdges()) g2_total_e += g2.getFreq(e);
    // L_infinity for edges
    double max_diff = 0.0;
    for (Edge e : g1.getEdges()) {
      long g1_e = g1.getFreq(e);
      long g2_e = g2.getFreq(e);
      double g1_rel = rel(g1_e, g1_total_e);
      double g2_rel = rel(g2_e, g2_total_e);
      double diff_rel = (g1_rel < g2_rel) ? (g2_rel - g1_rel) : (g1_rel - g2_rel);
      if (max_diff < diff_rel) max_diff = diff_rel;
    }
    res[0] = max_diff;
    // overlap for edges
    double overlap = 0.0;
    for (Edge e : g1.getEdges()) {
      long g1_e = g1.getFreq(e);
      long g2_e = g2.getFreq(e);
      double g1_rel = rel(g1_e, g1_total_e);
      double g2_rel = rel(g2_e, g2_total_e);
      overlap += (g1_rel < g2_rel) ? g1_rel : g2_rel;
    }
    res[1] = overlap;
    return res;
  }

  double median(double[] a) { // a is sorted
    if (a.length % 2 == 1) return a[a.length / 2];
    return (a[a.length / 2] + a[a.length / 2 - 1]) / 2;
  }

  int median(int[] a) { // a is sorted
    if (a.length % 2 == 1) return a[a.length / 2];
    return (a[a.length / 2] + a[a.length / 2 - 1]) / 2;
  }

  Map<CFG, String> cfg2file = new HashMap<>();
}
