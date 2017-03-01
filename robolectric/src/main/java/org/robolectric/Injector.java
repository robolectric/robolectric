package org.robolectric;

import org.robolectric.internal.ManifestFactory;

import java.util.Iterator;

public interface Injector {
  Iterable<ManifestFactory> getManifestFactories();
}
