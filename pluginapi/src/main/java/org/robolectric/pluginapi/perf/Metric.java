package org.robolectric.pluginapi.perf;

/**
 * Metric for perf stats collection.
 */
public class Metric {
  private final String name;
  private int count;
  private long elapsedNs;
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

  public boolean isSuccess() {
    return success;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Metric metric = (Metric) o;

    if (count != metric.count) {
      return false;
    }
    if (elapsedNs != metric.elapsedNs) {
      return false;
    }
    if (success != metric.success) {
      return false;
    }
    return name != null ? name.equals(metric.name) : metric.name == null;
  }

  public void record(long elapsedNs) {
    count++;
    this.elapsedNs += elapsedNs;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + count;
    result = 31 * result + (int) (elapsedNs ^ (elapsedNs >>> 32));
    result = 31 * result + (success ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Metric{"
        + "name='" + name + '\''
        + ", count=" + count
        + ", elapsedNs=" + elapsedNs
        + ", success=" + success
        + '}';
  }
}
