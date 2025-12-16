package org.robolectric.pluginapi.perf;

/** Environment metadata for perf stats collection. */
public class Metadata {
  private final int sdk;

  public Metadata(int sdk) {
    this.sdk = sdk;
  }

  /** Get the Android SDK API level the performance stats apply to. */
  public int getSdk() {
    return sdk;
  }
}
