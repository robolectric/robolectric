package org.robolectric.internal;

import org.robolectric.util.Pair;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnvHolder {

  // Typical test suites will use a single test runner, therefore have a maximum of one SdkEnvironment per API level.
  private static final int CACHE_SIZE = SdkConfig.getSupportedApis().size();

  // Simple LRU Cache. SdkENvironments are unique across InstrumentingClassloaderConfig and SdkConfig
  // TODO: We use test runner class name as a standin for InstrumentingClassloaderConfig because the latter does not implement
  // equals+hashcode. Replace all subclasses of this class with a Builder and having properties we can implement equals+hashcode.
  private static final LinkedHashMap<Pair<String,SdkConfig>, SdkEnvironment> sdkToEnvironment = new LinkedHashMap<Pair<String,SdkConfig>, SdkEnvironment>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Pair<String, SdkConfig>, SdkEnvironment> eldest) {
      return size() > CACHE_SIZE;
    }
  };

  synchronized public SdkEnvironment getSdkEnvironment(String testRunnerClassName, SdkConfig sdkConfig, SdkEnvironment.Factory factory) {
    Pair<String, SdkConfig> key = Pair.create(testRunnerClassName, sdkConfig);

    SdkEnvironment sdkEnvironment = sdkToEnvironment.get(key);
    if (sdkEnvironment == null) {
      sdkEnvironment = factory.create();
      sdkToEnvironment.put(key, sdkEnvironment);
    }
    return sdkEnvironment;
  }
}
