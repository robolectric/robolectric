import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.robolectric.util.ReflectionHelpers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class PerfRule implements MethodRule {
  public static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() {
    }
  };

  private Context perfContext;

  public PerfRule setUp(Runnable runnable) {
    perfContext.setUp = runnable;
    return this;
  }

  public PerfRule execute(Runnable runnable) {
    perfContext.execute = runnable;
    return this;
  }

  @Override
  public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
//    Class<?> declaringClass = frameworkMethod.getMethod().getDeclaringClass();
//    for (Method method : declaringClass.getDeclaredMethods()) {
//      Perf perf = method.getAnnotation(Perf.class);
//      if (perf != null) {
//      }
//    }

    final Perf perf = getAnnotation(Perf.class, frameworkMethod);

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        perfContext = new Context();
        statement.evaluate();

        if (perfContext.setUp != null) perfContext.setUp.run();

        for (int i = 0; i < perf.warmUp(); i++) {
          if (i > 0 && perf.intervalMs() > 0) Thread.sleep(perf.intervalMs());

          long startTime = System.nanoTime();
          perfContext.execute.run();
          long elapsedNs = System.nanoTime() - startTime;
          System.out.println("# Warm up: " + prettyNs(elapsedNs));
        }

        Stats stats = new Stats(perf.times());
        for (int i = 0; i < perf.times(); i++) {
          if (i > 0 && perf.intervalMs() > 0) Thread.sleep(perf.intervalMs());

          long startTime = System.nanoTime();
          perfContext.execute.run();
          long elapsedNs = System.nanoTime() - startTime;
          stats.addSample(elapsedNs);
        }
        String testName = frameworkMethod.getMethod().getDeclaringClass().getName() + "." + frameworkMethod.getName();
        System.out.println(testName + ":"
            + " min=" + prettyNs(stats.min)
            + " max=" + prettyNs(stats.max)
            + " mean=" + prettyNs(stats.mean())
            + " stddev=" + prettyNs((long) stats.stdDev())
        );
      }
    };
  }

  private String prettyNs(long elapsedNs) {
    return String.format("%01.2fms", elapsedNs / 1000000f);
  }

  private <T extends Annotation> T getAnnotation(Class<T> annotationType, FrameworkMethod frameworkMethod) {
    T annotation = frameworkMethod.getAnnotation(annotationType);
    if (annotation == null) annotation = ReflectionHelpers.defaultsFor(annotationType);
    return annotation;
  }

  class Stats {
    private final List<Long> samples;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private long sum = 0;

    public Stats() {
      samples = new ArrayList<>();
    }

    public Stats(int times) {
      samples = new ArrayList<>(times);
    }

    public void addSample(long elapsedNs) {
      if (min > elapsedNs) min = elapsedNs;
      if (max < elapsedNs) max = elapsedNs;
      sum += elapsedNs;

      samples.add(elapsedNs);
    }

    public long mean() {
      return sum / samples.size();
    }

    public double stdDev() {
      long mean = mean();
      double sum = 0;
      for (Long sample : samples) {
        sum += Math.pow(sample - mean, 2);
      }
      return Math.sqrt(sum / samples.size());
    }
  }

  private class Context {
    private Runnable setUp = NO_OP;
    private Runnable execute = NO_OP;
  }
}
