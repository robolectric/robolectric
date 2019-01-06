package org.robolectric.internal;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.robolectric.internal.dependency.DependencyJar;

public interface Sdk extends Comparable<Sdk> {

  int getApiLevel();

  String getAndroidVersion();

  String getAndroidCodeName();

  DependencyJar getAndroidSdkDependency();

  Path getJarPath();

  boolean isKnown();

  boolean isSupported();

  @Override
  int compareTo(@Nonnull Sdk o);
}
