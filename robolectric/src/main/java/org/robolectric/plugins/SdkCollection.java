package org.robolectric.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.inject.Inject;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;

/**
 * Holds and provides details on the list of known SDKs.
 */
@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class SdkCollection {

  private final SortedMap<Integer, Sdk> knownSdks = new TreeMap<>();
  private final SortedSet<Sdk> supportedSdks;

  @Inject
  public SdkCollection(SdkProvider sdkProvider) {
    Collection<Sdk> knownSdks = sdkProvider.getSdks();
    SortedSet<Sdk> supportedSdks = new TreeSet<>();
    knownSdks.forEach((sdk) -> {
      if (this.knownSdks.put(sdk.getApiLevel(), sdk) != null) {
        throw new IllegalArgumentException(
            String.format("duplicate SDKs for API level %d", sdk.getApiLevel()));
      }

      if (sdk.isSupported()) {
        supportedSdks.add(sdk);
      } else {
        System.err.printf(
            "[Robolectric] WARN: %s. Tests won't be run on SDK %d unless explicitly requested.\n",
            sdk.getUnsupportedMessage(), sdk.getApiLevel());
      }
    });
    this.supportedSdks = Collections.unmodifiableSortedSet(supportedSdks);
  }

  public Sdk getSdk(int apiLevel) {
    Sdk sdk = knownSdks.get(apiLevel);
    return sdk == null ? new UnknownSdk(apiLevel) : sdk;
  }

  public Sdk getMaxSupportedSdk() {
    return supportedSdks.last();
  }

  public SortedSet<Sdk> getKnownSdks() {
    return new TreeSet<>(knownSdks.values());
  }

  public SortedSet<Sdk> getSupportedSdks() {
    return supportedSdks;
  }
}
