package org.robolectric.internal;

import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;

import java.util.HashMap;
import java.util.Map;

public class EnvHolder {

  private final Map<SdkConfig, SdkEnvironment> sdkToEnvironment = new HashMap<>();

  synchronized public SdkEnvironment getSdkEnvironment(SdkConfig sdkConfig, SdkEnvironment.Factory factory) {
    SdkEnvironment sdkEnvironment = sdkToEnvironment.get(sdkConfig);
    if (sdkEnvironment == null) {
      sdkEnvironment = factory.create();
      sdkToEnvironment.put(sdkConfig, sdkEnvironment);
    }
    return sdkEnvironment;
  }
}
