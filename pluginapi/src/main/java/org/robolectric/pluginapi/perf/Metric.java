package org.robolectric.pluginapi.perf;

/**
 * Metric for perf stats collection.
 */
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

  public int getCount() {
    return count;
  }

  public long getElapsedNs() {
    return elapsedNs;
  }

  public long getMinNs() {
    return minNs;
  }

  public long getMaxNs() {
    return maxNs;
  }

  public boolean isSuccess() {
    return success;
  }

  public void record(long elapsedNs) {
    if (count == 0 || elapsedNs < minNs) {
      minNs = elapsedNs;
    }

    if (elapsedNs > maxNs) {
      maxNs = elapsedNs;
    }

    this.elapsedNs += elapsedNs;

    count++;
  }

  public void incrementCount() {
    this.count++;
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
    return name != null ? name.equals(metric.name) : metric.name == null;
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
        + "name='" + name + '\''
        + ", count=" + count
        + ", minNs=" + minNs
        + ", maxNs=" + maxNs
        + ", elapsedNs=" + elapsedNs
        + ", success=" + success
        + '}';
  }
}
