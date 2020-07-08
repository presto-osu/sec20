import com.google.common.collect.Maps;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

public class MainTighterRestricted extends Main {
  private double kratio;

  public static void main(String[] a) {
    new MainTighterRestricted().run(a);
  }

  @Override
  void init() {
    tag = "tighter";
    // compute a global value that can be used by all local
    // randomizers. the value is in globalLS.

    // option 1: worst case: use |N| from the static graph
    //    globalLS = static_cfg.getNodes().size() - 1;
    // -1 because the start node cannot ever be removed

    // option 2: compute per-dynamic-graph. more later.
    globalLS = static_cfg.getNodes().size() - 1;
    globalLS = globalLS * kratio;

    int i = 0;
    for (CFG d : dyn_cfgs) {
      DiGraph dtree = d.getDomTree();
//      try {
//        dtree.toDot(new PrintStream("dom/" + i + ".dot"));
//        i++;
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
      Node art_root = dtree.getRoot();
      Node realroot = art_root.getSuccessors().iterator().next();

      // TODO: TURN OFF THESE SANITY CHECKS IN THE FINAL RUNS
      if (art_root.getSuccessors().size() != 1) throw new RuntimeException("Bad art root");
      if (!realroot.equals(d.getRoot())) throw new RuntimeException("Bad real root");
      int x = domSubtreeSize(realroot);
      if (x != d.getNodes().size()) throw new RuntimeException("Bad domSubtreeSize");

      // compute the local sensitivity w.r.t. the removal of
      // each graph node (except for the root).
      Set<String> toRemove = new HashSet<>();
      for (Node n : realroot.getSuccessors()) {
        int dist = domSubtreeSize(dtree.getNode(n.getId()));
        List<String> dominatees = d.getDominatees(n.getId());
        while (dist-- > globalLS) {
          toRemove.add(dominatees.remove(0));
        }
      }

      for (String id : toRemove) d.removeNode(id);
    }

    //    System.out.println("ls: " + LS);
    //    List<Long> ls = new ArrayList<>(LS.values());
    //    Collections.shuffle(ls);
    //    int ss = 100;
    //    ls = ls.subList(0, ss);
    //    double[] arr = new double[ss];
    //    int i = 0;
    //    for (long v : ls) arr[i++] = v;
    //    Statistics statistics = new Statistics(arr);
    //    double bar_ls = statistics.getMean();
    //    LaplaceDistribution laplaceDistribution = new LaplaceDistribution(0, globalLS / epsilon /
    // ss);
    //    double bar_ls_priv = bar_ls + laplaceDistribution.sample();
    //    laplaceDistribution = new LaplaceDistribution(0, globalLS * globalLS / epsilon / ss);
    //    double var_priv = -1;
    //    while (var_priv < 0) var_priv = statistics.getVariance()+ laplaceDistribution.sample();
    //    double stddev_priv = Math.sqrt(var_priv);
    //    System.out.println("mean priv: " + bar_ls_priv);
    //    System.out.println("stddev priv: " + stddev_priv);
    //    System.out.println("0.999 percentile: " + (bar_ls_priv + 2 * stddev_priv) + "/" +
    // globalLS);
    //    System.out.println(
    //        "0.999 percentile: "
    //            + (Statistics.getConfidenceInterval(stddev_priv, ss, 1 - 0.999))
    //            + "/"
    //            + globalLS);
    //    int dev2 = 0, dev1 = 0;
    //    for (long v : ls) {
    //      if (v > statistics.getMean() + statistics.getStdDev() * 2) {
    //        dev2 += 1;
    //      }
    //      if (v > statistics.getMean() + statistics.getStdDev()) {
    //        dev1 += 1;
    //      }
    //    }
    //    //    System.out.println("0.999 percentile: " + (statistics.getMean() + 2 *
    //    // statistics.getStdDev()) + "/" + globalLS);
    //    //    System.out.println("e^-t: " + Math.exp(-1));
    //    //    System.out.println("dev1: " + (dev1 * 1.0 / ss));
    //    //    System.out.println("e^-t: " + Math.exp(-2));
    //    //    System.out.println("dev2: " + (dev2 * 1.0 / ss));
    //    System.exit(0);
  }

  @Override
  void parseParams(String[] a) {
    super.parseParams(a);
    kratio = 0.5;
    if (a.length > 5 && !a[5].trim().isEmpty()) {
      kratio = Double.parseDouble(a[5]);
    }

    Map<String, Double> dir2kratio = Maps.newHashMap();
    dir2kratio.put("./dataset/screen/barometer", .25);
    dir2kratio.put("./dataset/screen/bible", .5);
    dir2kratio.put("./dataset/screen/dpm", .3);
    dir2kratio.put("./dataset/screen/drumpads", .65);
    dir2kratio.put("./dataset/screen/equibase", .9); // .1
    dir2kratio.put("./dataset/screen/localtv", .45);
    dir2kratio.put("./dataset/screen/loctracker", .25);
    dir2kratio.put("./dataset/screen/mitula", .8);
    dir2kratio.put("./dataset/screen/moonphases", .95);
    dir2kratio.put("./dataset/screen/parking", .35);
    dir2kratio.put("./dataset/screen/parrot", .7);
    dir2kratio.put("./dataset/screen/post", .5);
    dir2kratio.put("./dataset/screen/quicknews", .4);
    dir2kratio.put("./dataset/screen/speedlogic", .15);
    dir2kratio.put("./dataset/screen/vidanta", .95);

    dir2kratio.put("./dataset/cg/barometer", .01);
    dir2kratio.put("./dataset/cg/bible", .01);
    dir2kratio.put("./dataset/cg/dpm", .01);
    dir2kratio.put("./dataset/cg/drumpads", 0.05);
    dir2kratio.put("./dataset/cg/equibase", .05);
    dir2kratio.put("./dataset/cg/localtv", .01);
    dir2kratio.put("./dataset/cg/loctracker", .05);
    dir2kratio.put("./dataset/cg/mitula", .01);
    dir2kratio.put("./dataset/cg/moonphases", .05);
    dir2kratio.put("./dataset/cg/parking", .01);
    dir2kratio.put("./dataset/cg/parrot", .01);
    dir2kratio.put("./dataset/cg/post", .01);
    dir2kratio.put("./dataset/cg/quicknews", .01);
    dir2kratio.put("./dataset/cg/speedlogic", .1);
    dir2kratio.put("./dataset/cg/vidanta", .01);

    kratio = dir2kratio.get(dir);
    System.out.println("kratio:\t" + kratio);
  }
  //
  //  @Override
  //  public void run(String[] a) {
  //    parseParams(a);
  //    settings();
  //    init(static_path, dir);
  //
  //    // run actual experiments
  //    System.out.print("Running " + trials + " trials with epsilon ");
  //    System.out.printf("%.3f", epsilon);
  //    //    System.out.println(" and sensitivity by sampling " + sampleSize + " CFGs");
  //    all_linf = new double[trials];
  //    all_wjac = new double[trials];
  //    all_l1 = new double[trials];
  //    double[] all_globalLS = new double[trials];
  //    int[] all_numusers_exceed_gLS = new int[trials];
  //
  //    int[] tarr = new int[trials];
  //    for (int j = 0; j < trials; j++) tarr[j] = j;
  //    Arrays.stream(tarr)
  ////        .parallel()
  //        .forEach(
  //            new IntConsumer() {
  //              // Arrays.stream(tarr).forEach(new IntConsumer() {
  //              @Override
  //              public void accept(int j) {
  //                System.out.print(".");
  //                Object[] res = runTrial(j);
  //                all_globalLS[j] = (double) res[0];
  //                all_numusers_exceed_gLS[j] = (int) res[1];
  //              }
  //            });
  //    //    for (int j = 0; j < trials; j++) {
  //    //      //      runTrial(j);
  //    //      System.out.print(".");
  //    //      runTrialWithSampling(j, sampleSize, drop);
  //    //      all_globalLS[j] = globalLS;
  //    //    }
  //    Arrays.sort(all_linf);
  //    Arrays.sort(all_wjac);
  //    Arrays.sort(all_l1);
  //    printAppInfo(all_globalLS, all_numusers_exceed_gLS);
  //    //    System.exit(0);
  //    // System.out.printf(" %f %f\n", median(all_linf), median(all_wjac));
  //    Statistics slinf = new Statistics(all_linf);
  //    Statistics swjac = new Statistics(all_wjac);
  //    Statistics sl1 = new Statistics(all_l1);
  //    stats(slinf, swjac, sl1);
  //  }
  //
  //  void printAppInfo(double[] all_globalLS, int[] all_numusers_exceed_gLS) {
  //    System.out.println(
  //        "\nApp: "
  //            + dir
  //            + " "
  //            + Arrays.stream(all_globalLS).average().getAsDouble()
  //            + "/"
  //            + globalLS
  //            + " ="
  //            + Arrays.stream(all_numusers_exceed_gLS).average().getAsDouble()
  //            + "/"
  //            + dyn_cfgs.size());
  //  }
  //
  //  @Override
  //  Object[] runTrial(int trial_num) {
  //    // GS = max(LS_i) where i is sampled uniformly by Collections.shuffle()
  //    List<CFG> cfgs = new ArrayList<>(dyn_cfgs);
  //    Collections.shuffle(cfgs);
  //
  ////    double epsilon = this.epsilon * this.e_1_ratio;
  ////    double localGlobalSensitivity = this.globalLS;
  ////    double[] lsArr = new double[cfgs.size()];
  ////    int idx = 0;
  ////    for (CFG d : cfgs) {
  ////      if (epsilon <= 0.0001) {
  ////        //        System.out.println("no perturbation");
  ////        lsArr[idx++] = LS.get(d);
  ////        continue;
  ////      }
  ////      //      double beta = localGlobalSensitivity / epsilon;
  ////      //      LaplaceDistribution laplaceDistribution = new LaplaceDistribution(0, beta);
  ////      //      //        long y = Math.round(laplaceDistribution.sample());
  ////      //      double y = laplaceDistribution.sample();
  ////      //      double rand_ls = LS.get(d);
  ////      //      rand_ls = Math.max(0, Math.min(rand_ls, localGlobalSensitivity));
  ////      lsArr[idx++] = LS.get(d);
  ////    }
  //    //    }
  //    //    Statistics statistics = new Statistics(lsArr);
  //    //    double ls_bar = statistics.getMean();
  //    //    double ls_bar_priv;
  //    //    {
  //    //      double beta = localGlobalSensitivity / epsilon / lsArr.length;
  //    //      LaplaceDistribution laplaceDistribution = new LaplaceDistribution(0, beta);
  //    //      ls_bar_priv = ls_bar + laplaceDistribution.sample();
  //    //    }
  //    //    double var = statistics.getVariance();
  //    //    double var_priv;
  //    //    {
  //    //      double beta = localGlobalSensitivity * localGlobalSensitivity / epsilon /
  // lsArr.length;
  //    //      LaplaceDistribution laplaceDistribution = new LaplaceDistribution(0, beta);
  //    //      var_priv = var + laplaceDistribution.sample();
  //    //    }
  //    //    double sigma_priv = Math.sqrt(var_priv);
  //    //    double globalLS = ls_bar_priv + sigma_priv * sigma_mul;
  //    //    //    double globalLS = statistics.getMean() + statistics.getConfidenceInterval(1 -
  //    //    // 0.9999999999);
  //    //    globalLS = Math.min(localGlobalSensitivity, Math.round(globalLS));
  //    //    //    globalLS = Math.min(localGlobalSensitivity, Math.max(globalLS, rand_ls));
  //    //    //    int iterations = 1;
  //    //    //    double localEpsilon = epsilon / iterations;
  //    //    //    double globalLS = 0;
  //    //    //    for (int i = 0; i < iterations; i++) {
  //    //    //      //      System.out.println("App: " + localEpsilon + "   " +
  //    // localGlobalSensitivity);
  //    //    //      globalLS = 0;
  //    //    //      double[] lsArr = new double[cfgs.size()];
  //    //    //      int idx = 0;
  //    //    //      for (CFG d : cfgs) {
  //    //    //        double beta = localGlobalSensitivity / localEpsilon;
  //    //    //        LaplaceDistribution laplaceDistribution = new LaplaceDistribution(0, beta);
  //    //    //        //        long y = Math.round(laplaceDistribution.sample());
  //    //    //        double y = laplaceDistribution.sample();
  //    //    //        //      globalLS = globalLS + LS.get(d) + y;
  //    //    //        double rand_ls = LS.get(d) + y;
  //    //    //        //            System.out.println(rand_ls + " " + globalLS + " " +
  //    //    // localGlobalSensitivity);
  //    //    //        lsArr[idx] = rand_ls;
  //    //    //        //        globalLS = Math.min(localGlobalSensitivity, Math.max(globalLS,
  //    // rand_ls));
  //    //    //        //        globalLS = Math.min(localGlobalSensitivity, globalLS);
  //    //    //      }
  //    //    //      Statistics statistics = new Statistics(lsArr);
  //    //    //      globalLS = statistics.getMean() + statistics.getConfidenceInterval95();
  //    //    //      globalLS = Math.min(localGlobalSensitivity, globalLS);
  //    //    //      localGlobalSensitivity = globalLS;
  //    //    //    }
  //    //
  //    //    int num = 0;
  //    //    for (CFG d : cfgs) {
  //    //      if (LS.get(d) > globalLS) num += 1;
  //    //    }
  //    //    //    if (num > 0) System.out.println("ERROR: " + num + " users have LS > global LS "
  // +
  //    //    // globalLS);
  //
  //    Map<Node, Integer> serv_hist =
  //        new HashMap<>(); // Aggregate node coverage reported to the server
  //    Map<Node, Integer> adj_hist = new HashMap<>(); // Aggregate node coverage after
  // post-processing
  //    for (Node n : static_cfg.getNodes()) {
  //      serv_hist.put(n, 0);
  //      adj_hist.put(n, 0);
  //    }
  //    double z = Math.exp((this.epsilon - epsilon) / globalLS);
  //    double p1 = z / (1 + z);
  //    double p2 = 1 / (1 + z);
  //    // System.out.println("p1: " + p1 + ", p2: " + p2);
  //    // compute the aggregate "raw" histogram at the server
  //    int numReports = 0;
  //    for (CFG d : cfgs) {
  //      numReports += 1;
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
  //    // System.out.printf("Actual users: %d/%d\n", numReports, cfgs.size() - sampleSize);
  //
  //    // post-process at the server
  //    for (Node mmm : static_cfg.getNodes()) {
  //      double x = serv_hist.get(mmm);
  //      int estimate = (int) Math.round(((z + 1) * x - numReports) / (z - 1));
  //      if (estimate < 0) estimate = 0;
  //      if (estimate > numReports) estimate = numReports;
  //      adj_hist.put(mmm, estimate);
  //    }
  //
  //    // accuracy: L infinity and weighted Jaccard
  //    long real_sum = 0;
  //    for (Node n : real_hist.keySet()) real_sum += real_hist.get(n);
  //    long adj_sum = 0;
  //    for (Node n : adj_hist.keySet()) adj_sum += adj_hist.get(n);
  //    // System.out.println("Real sum: " + real_sum + ", adj sum: " + adj_sum);
  //    double linf = 0.;
  //    double l1 = 0.;
  //    double wjac1 = 0.;
  //    double wjac2 = 0.;
  //    //    System.out.println("####### real_sum=" + real_sum + ", adj_sum=" + adj_sum);
  //    for (Node mmm : static_cfg.getNodes()) {
  //      double x = rel(real_hist.get(mmm), real_sum);
  //      double y = rel(adj_hist.get(mmm), adj_sum);
  //      //      System.out.println("####### x=" + real_hist.get(mmm) + ", y=" +
  // adj_hist.get(mmm));
  //      // System.out.print(mmm + ": ");
  //      // System.out.printf("%.3f, %.3f\n", x, y);
  //      double diff = Math.abs(x - y);
  //      if (linf < diff) linf = diff;
  //      l1 += diff;
  //      wjac1 += Math.min(x, y);
  //      wjac2 += Math.max(x, y);
  //    }
  //    //    System.out.println("####### wjac1=" + wjac1 + ", wjac2=" + wjac2);
  //    all_linf[trial_num] = linf;
  //    all_l1[trial_num] = l1;
  //    all_wjac[trial_num] = 1 - wjac1 / wjac2;
  //    return new Object[] {globalLS, 0};
  //  }

  int domSubtreeSize(Node n) {
    if (n.getSuccessors().isEmpty()) return 1;
    int res = 1;
    for (Node m : n.getSuccessors()) res += domSubtreeSize(m);
    return res;
  }
}
