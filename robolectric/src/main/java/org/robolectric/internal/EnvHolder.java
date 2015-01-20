package org.robolectric.internal;

import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class EnvHolder {
  public final Map<FsFile, AndroidManifest> appManifestsByFile = new HashMap<>();
  private final Map<SdkConfig, SoftReference<SdkEnvironment>> sdkToEnvironmentSoft = new HashMap<>();

  synchronized public SdkEnvironment getSdkEnvironment(SdkConfig sdkConfig, SdkEnvironment.Factory factory) {
    SoftReference<SdkEnvironment> reference = sdkToEnvironmentSoft.get(sdkConfig);
    SdkEnvironment sdkEnvironment = reference == null ? null : reference.get();
    if (sdkEnvironment == null) {
      sdkEnvironment = factory.create();
      sdkToEnvironmentSoft.put(sdkConfig, new SoftReference<>(sdkEnvironment));
    }
    return sdkEnvironment;
  }
}
