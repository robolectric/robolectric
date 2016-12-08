package org.robolectric.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Statistics {
  double[] data;
  int size;

  public Statistics(List<Double> data) {
    this(asDoubles(data));
  }

  private static double[] asDoubles(List<Double> data) {
    double[] doubles = new double[data.size()];
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] = data.get(i);
    }
    return doubles;
  }

  public Statistics(double[] data) {
    this.data = data;
    size = data.length;
  }

  double min() {
    double min = Double.MAX_VALUE;
    for (double v : data)
      if (v < min)
        min = v;
    return min;
  }

  double max() {
    double max = Double.MIN_VALUE;
    for (double v : data)
      if (v > max)
        max = v;
    return max;
  }

  double mean() {
    double sum = 0.0;
    for (double a : data)
      sum += a;
    return sum / size;
  }

  double variance() {
    double mean = mean();
    double temp = 0;
    for (double a : data)
      temp += (a - mean) * (a - mean);
    return temp / size;
  }

  double stdDev() {
    return Math.sqrt(variance());
  }

  public double median() {
    Arrays.sort(data);

    if (data.length % 2 == 0) {
      return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
    } else {
      return data[data.length / 2];
    }
  }

  public static class Runner {
    private Runnable runnable;

    public Runner(Runnable runnable) {
      this.runnable = runnable;
    }

    public void run(int count) {
      List<Double> elapsedTimesNs = new ArrayList<>();
      System.out.print("Elapsed times:");
      for (int i = 0; i < count; i++) {
        long startTimeNs = System.nanoTime();
        runnable.run();
        long elapsedTimeNs = System.nanoTime() - startTimeNs;
        elapsedTimesNs.add((double) elapsedTimeNs);
        System.out.print(" " + nsToMs(elapsedTimeNs));
      }
      System.out.print("\n");
      Statistics statistics = new Statistics(elapsedTimesNs);
      System.out.println("min: " + nsToMs((long) statistics.min())
          + " mean: " + nsToMs((long) statistics.mean())
          + " max: " + nsToMs((long) statistics.max())
          + " median: " + nsToMs((long) statistics.median())
          + " stddev: " + nsToMs((long) statistics.stdDev()));
    }

    private String nsToMs(long elapsedNs) {
      return String.format("%1.2fms", elapsedNs / 1000000f);
    }
  }
}
