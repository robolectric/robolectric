package org.robolectric;

import org.robolectric.res.FsFile;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class EnvHolder {
  public final Map<FsFile, AndroidManifest> appManifestsByFile = new HashMap<FsFile, AndroidManifest>();
  private final Map<SdkConfig, SoftReference<SdkEnvironment>> sdkToEnvironmentSoft = new HashMap<SdkConfig, SoftReference<SdkEnvironment>>();

  synchronized public SdkEnvironment getSdkEnvironment(SdkConfig sdkConfig, SdkEnvironment.Factory factory) {
    SoftReference<SdkEnvironment> reference = sdkToEnvironmentSoft.get(sdkConfig);
    SdkEnvironment sdkEnvironment = reference == null ? null : reference.get();
    if (sdkEnvironment == null) {
      if (reference != null) {
        System.out.println("[DEBUG] ********************* GC'ed SdkEnvironment reused!");
      }

      sdkEnvironment = factory.create();
      sdkToEnvironmentSoft.put(sdkConfig, new SoftReference<SdkEnvironment>(sdkEnvironment));
    }

    return sdkEnvironment;
  }
}
