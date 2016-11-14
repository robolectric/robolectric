import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.*;

public class PerfMetrics implements MethodRule {
  @Override
  public Statement apply(final Statement statement, FrameworkMethod frameworkMethod, Object o) {
    String name = frameworkMethod.getName();
    System.out.println("name = " + name);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        List<Double> elapsed = new ArrayList<>();

        statement.evaluate();

        for (int i = 0; i < 3; i++) {
          long startNs = System.nanoTime();
          statement.evaluate();
          long elapsedNs = System.nanoTime() - startNs;
          System.out.println("elapsedNs = " + nsToMs(elapsedNs));
          elapsed.add((double) elapsedNs);
        }

        Statistics statistics = new Statistics(elapsed);
        System.out.println("stddev = " + nsToMs((long) statistics.getStdDev()));
      }

      private String nsToMs(long elapsedNs) {
        return String.format("%1.2fms", elapsedNs / 1000000f);
      }
    };
  }

  static class Statistics {
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

    double getMean() {
      double sum = 0.0;
      for (double a : data)
        sum += a;
      return sum / size;
    }

    double getVariance() {
      double mean = getMean();
      double temp = 0;
      for (double a : data)
        temp += (a - mean) * (a - mean);
      return temp / size;
    }

    double getStdDev() {
      return Math.sqrt(getVariance());
    }

    public double median() {
      Arrays.sort(data);

      if (data.length % 2 == 0) {
        return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
      } else {
        return data[data.length / 2];
      }
    }
  }
}
