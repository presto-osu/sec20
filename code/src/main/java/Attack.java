/*
 * Attack.java
 *
 * Copyright (c) 2019 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;
import org.apache.commons.beanutils.ContextClassLoaderLocal;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class Attack extends Main {
  private int userSize;
  private String gs;

  public static void main(String[] args) {
    new Attack().run(args);
  }

  @Override
  void parseParams(String[] a) {
    super.parseParams(a);
    userSize = 500;
    if (a.length > 3) {
      userSize = Integer.parseInt(a[3]);
    }
    if (a.length > 4) {
      epsilon = Double.parseDouble(a[4]);
    }
    gs = "b";
    if (a.length > 5) {
      gs = a[5];
    }
  }

  List<CFG> cfgs;
  //  , user, rest;

  @Override
  public void run(String[] args) {
    parseParams(args);
    settings();
    init();
    cfgs = new ArrayList<>(dyn_cfgs);
    Collections.shuffle(cfgs);
    //    user = cfgs.subList(0, userSize);
    //    rest = cfgs.subList(userSize, cfgs.size());

    if ("b".equals(gs)) {
      globalLS = static_cfg.getNodes().size() - 1;
    } else if ("t".equals(gs)) {
      globalLS = 3.15;
    } else if ("r".equals(gs)) {
//            globalLS = 0.5;
//      globalLS = 2;
       globalLS = 1;
    }
    //    globalLS = 3.688391057348952;
    //    globalLS = 2.0044653537344543;

    Map<Node, double[]> all_prior_er = new HashMap<>(static_cfg.getNodes().size());
    Map<Node, double[]> all_post_er = new HashMap<>(static_cfg.getNodes().size());
    Map<Node, Double> mean_errrate_prior = new HashMap<>(static_cfg.getNodes().size());
    Map<Node, Double> mean_errrate_post = new HashMap<>(static_cfg.getNodes().size());
    Map<Node, Double> mean_errate_ratio = new HashMap<>(static_cfg.getNodes().size());
    for (Node n : static_cfg.getNodes()) {
      all_prior_er.put(n, new double[trials]);
      all_post_er.put(n, new double[trials]);
      mean_errrate_prior.put(n, 0.);
      mean_errrate_post.put(n, 0.);
      mean_errate_ratio.put(n, 0.);
    }

    //    List<Double> all_prior_er_per_vec = new LinkedList<>();
    //    List<Double> all_post_er_per_vec = new LinkedList<>();

    //    List<Integer> headers = new LinkedList<>();
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
                Object[] res = runTrial(j);
                Map<Node, double[]> prior_error_num = (Map<Node, double[]>) res[0];
                Map<Node, double[]> post_error_num = (Map<Node, double[]>) res[1];
                for (Node n : prior_error_num.keySet()) {
                  double[] er_num = prior_error_num.get(n);
                  all_prior_er.get(n)[j] = er_num[0] / userSize;
                  //                  all_prior_er_per_vec.add(er_num);
                }
                for (Node n : post_error_num.keySet()) {
                  double[] er_num = post_error_num.get(n);
                  all_post_er.get(n)[j] = er_num[0] / userSize;
                  //                  all_post_er_per_vec.add(er_num);
                }
              }
            });
    System.out.println("\nApp: " + dir + " " + globalLS + " ");

    //    {
    //      Double[] errrate = all_prior_er_per_vec.toArray(new Double[0]);
    //      Statistics s = new Statistics(ArrayUtils.toPrimitive(errrate));
    //      System.out.println("\nmean_err_rate_prior: " + s.getMean() / userSize);
    //      errrate = all_post_er_per_vec.toArray(new Double[0]);
    //      s = new Statistics(ArrayUtils.toPrimitive(errrate));
    //      System.out.println("\nmean_err_rate_post: " + s.getMean() / userSize);
    //    }

    Map<Node, Integer> total = new ConcurrentHashMap<>();
    Map<Node, Integer> user_total = new ConcurrentHashMap<>();
    Map<Node, Integer> rest_total = new ConcurrentHashMap<>();
    for (Node n : static_cfg.getNodes()) {
      for (CFG d : cfgs) {
        if (d.hasNode(n.getId())) {
          total.put(n, total.getOrDefault(n, 0) + 1);
        }
      }
      //      for (CFG d : user) {
      //        if (d.hasNode(n.getId())) {
      //          user_total.put(n, user_total.getOrDefault(n, 0) + 1);
      //        }
      //      }
      //      for (CFG d : rest) {
      //        if (d.hasNode(n.getId())) {
      //          rest_total.put(n, rest_total.getOrDefault(n, 0) + 1);
      //        }
      //      }
      {
        Statistics s = new Statistics(all_prior_er.get(n));
        mean_errrate_prior.put(n, s.getMean());
      }
      {
        Statistics s = new Statistics(all_post_er.get(n));
        mean_errrate_post.put(n, s.getMean());
      }
      {
        double[] mean = new double[trials];
        double[] errrateprior = all_prior_er.get(n);
        double[] errratepost = all_post_er.get(n);
        for (int i = 0; i < trials; i++) {
          mean[i] = errratepost[i] / errrateprior[i];
        }
        Statistics s = new Statistics(mean);
        mean_errate_ratio.put(n, Math.min(1.0, s.getMean()));
      }
    }
    //    for (Node n : mean_errate_ratio.keySet()) {
    //      mean_errate_ratio.put(n, mean_errrate_post.get(n) / mean_errrate_prior.get(n));
    //    }
    System.out.println("\ntotal: " + total);
    System.out.println("\nuser_total: " + user_total);
    System.out.println("\nrest_total: " + rest_total);
//    System.out.println("\nmean_err_rate_prior_all: " + mean_errrate_prior);
//    System.out.println("\nmean_err_rate_post_all: " + mean_errrate_post);
//    System.out.println("\nmean_errate_ratio_all: " + mean_errate_ratio);
    for (Node n : static_cfg.getNodes()) {
      if (n.getId().startsWith("SettingsActivity")) {
        System.out.println("\nmean_err_rate_prior:\t" + mean_errrate_prior.get(n));
        System.out.println("\nmean_err_rate_post:\t" + mean_errrate_post.get(n));
        System.out.println("\nmean_errate_ratio:\t" + mean_errate_ratio.get(n));
      }
    }
  }

  boolean rand(Node mmm, CFG d, double p1, double p2) {
    if (d.getNodes().contains(mmm)) {
      // 1 bit
      if (Math.random() < p1) { // keep the 1 as 1
        return true;
      }
    } else {
      // 0 bit
      if (Math.random() < p2) { // flip the 0 to 1
        return true;
      }
    }
    return false;
  }

  boolean feasibilityAnalysis(CFG d, Node n, boolean true_ans) {
    //        CFG d0 = d.copy();
    //        d0.removeNode(n.getId());
    Set<Node> seen = new HashSet<>();
    List<Node> worklist = new LinkedList<>();
    worklist.add(d.getRoot());
    while (!worklist.isEmpty()) {
      Node mmm = worklist.remove(0);
      seen.add(mmm);
      for (Edge e : d.getEdges()) {
        Node src = e.getSource(), tgt = e.getTarget();
        if (src.equals(n) || tgt.equals(n)) continue;
        if (seen.contains(tgt)) continue;
        if (src.equals(mmm)) {
          seen.add(tgt);
          worklist.add(tgt);
        }
      }
      //          Node m = static_cfg.getNode(mmm.getId());
      //          for (Node succ : m.getSuccessors()) {
      //            if (succ.equals(n)) continue;
      //            if (!seen.contains(succ)) {
      //              seen.add(succ);
      //              worklist.add(succ);
      //            }
      //          }
    }
    //        System.out.println(
    //            "+++ " + seen.size() + "/" + d0.getNodes().size() + "\ttrue:" + true_ans);
    if (seen.size() < d.getNodes().size() - 1) {
      System.out.println(
              "!!!!!!!!!!!!!!!!!!!!!!! "
                      + seen.size()
                      + "/"
                      + d.getNodes().size()
                      + "\ttrue:"
                      + true_ans);
//      System.out.println(seen);
//      System.out.println(d.getNodes());
      return true;
    }
    return false;
  }

  Object[] runTrial(int trial_num) {
    Collections.shuffle(cfgs);
    List<CFG> user = cfgs.subList(0, userSize);
    List<CFG> rest = cfgs.subList(userSize, cfgs.size());

    double z = Math.exp(epsilon / globalLS);
    double p1 = z / (1 + z);
    double p2 = 1 / (1 + z);
    //    double p = 1 / (1 + z);

    Map<Node, double[]> prior_er_num = new HashMap<>(static_cfg.getNodes().size());
    Map<Node, double[]> post_er_num = new HashMap<>(static_cfg.getNodes().size());
    for (Node n : static_cfg.getNodes()) {
      if (!n.getId().startsWith("SettingsActivity")) continue;
      double num = 0.0;
      for (CFG d : rest) {
        if (d.getNodes().contains(n)) {
          num += 1;
        }
      }
      double q = num / rest.size();

      // make guesses
      int num_error_prior = 0;
      int num_error_post = 0;
      for (CFG d : user) {
        boolean true_ans = d.getNodes().contains(n);
        boolean prior_guess = q == 0.5 ? Math.random() < 0.5 : q > (1 - q);
//                        System.out.println("+++ Prior guess: " + prior_guess);

        boolean rand = rand(n, d, p1, p2);
        double a, b;
        if (rand) {
          a = p1 * q;
          b = p2 * (1 - q);
        } else {
          a = p2 * q;
          b = p1 * (1 - q);
        }
        boolean post_guess = a == b ? Math.random() <= 0.5 : a > b;

        if (feasibilityAnalysis(d, n, true_ans)) {
          prior_guess = post_guess = true;
        }

        if (prior_guess != true_ans) {
          num_error_prior += 1;
        }
        if (post_guess != true_ans) {
          num_error_post += 1;
        }
      }
      {
        double[] tmp = new double[1];
        tmp[0] = 1.0 * num_error_prior;
        prior_er_num.put(n, tmp);
      }
      {
        double[] tmp = new double[1];
        tmp[0] = 1.0 * num_error_post;
        post_er_num.put(n, tmp);
      }
    }
    return new Object[] {prior_er_num, post_er_num};
  }
}
