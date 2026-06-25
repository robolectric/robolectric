package org.robolectric.pluginapi.perf;

import java.util.Objects;

/** Metric for perf stats collection. */
public class Metric {
  private final String name;
  private int count;
  private long elapsedNs;
  private long minNs;
  private long maxNs;
  private final boolean success;

  public Metric(String name, int count, int elapsedNs, boolean success) {
    this.name = name;
    this.count = count;
    this.elapsedNs = elapsedNs;
    this.success = success;
  }

  public Metric(String name, boolean success) {
    this(name, 0, 0, success);
  }

  public String getName() {
    return name;
  }

  public synchronized int getCount() {
    return count;
  }

  public synchronized long getElapsedNs() {
    return elapsedNs;
  }

  public synchronized long getMinNs() {
    return minNs;
  }

  public synchronized long getMaxNs() {
    return maxNs;
  }

  public boolean isSuccess() {
    return success;
  }

  public synchronized void record(long elapsedNs) {
    if (count == 0 || elapsedNs < minNs) {
      minNs = elapsedNs;
    }

    if (elapsedNs > maxNs) {
      maxNs = elapsedNs;
    }

    this.elapsedNs += elapsedNs;

    count++;
  }

  public synchronized void incrementCount() {
    this.count++;
  }

  /** Sets the count to the given value, overwriting any previous count. */
  public synchronized void recordCount(int count) {
    this.count = count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Metric)) {
      return false;
    }

    Metric metric = (Metric) o;

    if (success != metric.success) {
      return false;
    }
    return Objects.equals(name, metric.name);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (success ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Metric{"
        + "name='"
        + name
        + '\''
        + ", count="
        + count
        + ", minNs="
        + minNs
        + ", maxNs="
        + maxNs
        + ", elapsedNs="
        + elapsedNs
        + ", success="
        + success
        + '}';
  }
}
