package org.robolectric.pluginapi;

import java.util.Collection;
import org.robolectric.internal.Sdk;

/**
 * A provider of known instances of {@link Sdk}.
 */
public interface SdkProvider {

  Sdk getMaxKnownSdk();

  Sdk getMaxSupportedSdk();

  Sdk getSdk(int apiLevel);

  Collection<Sdk> getSupportedSdks();

  Collection<Sdk> getKnownSdks();
}
