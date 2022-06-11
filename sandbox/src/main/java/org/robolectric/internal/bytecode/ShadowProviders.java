package org.robolectric.internal.bytecode;

import static java.util.Comparator.comparing;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Priority;
import org.robolectric.internal.ShadowProvider;

/** The set of {@link ShadowProvider} implementations found on the classpath. */
@SuppressWarnings("AndroidJdkLibsChecker")
public class ShadowProviders {

  private final ImmutableList<ShadowProvider> shadowProviders;
  private final ShadowMap baseShadowMap;

  public ShadowProviders(List<ShadowProvider> shadowProviders) {
    // Return providers sorted by descending priority.
    this.shadowProviders =
        ImmutableList.sortedCopyOf(
            comparing(ShadowProviders::priority).reversed().thenComparing(ShadowProviders::name),
            shadowProviders);

    this.baseShadowMap = ShadowMap.createFromShadowProviders(this.shadowProviders);
  }

  public ShadowMap getBaseShadowMap() {
    return baseShadowMap;
  }

  private static int priority(ShadowProvider shadowProvider) {
    Priority priority = shadowProvider.getClass().getAnnotation(Priority.class);
    return priority == null ? 0 : priority.value();
  }

  private static String name(ShadowProvider shadowProvider) {
    return shadowProvider.getClass().getName();
  }

  public List<String> getInstrumentedPackages() {
    Set<String> packages = new HashSet<>();
    for (ShadowProvider shadowProvider : shadowProviders) {
      Collections.addAll(packages, shadowProvider.getProvidedPackageNames());
    }
    return new ArrayList<>(packages);
  }

  public ShadowProvider[] inClassLoader(ClassLoader classLoader) {
    ShadowProvider[] inCL = new ShadowProvider[shadowProviders.size()];
    for (int i = 0; i < shadowProviders.size(); i++) {
      ShadowProvider shadowProvider = shadowProviders.get(i);
      String name = shadowProvider.getClass().getName();
      try {
        inCL[i] =
            classLoader
                .loadClass(name)
                .asSubclass(ShadowProvider.class)
                .getConstructor()
                .newInstance();
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("couldn't reload " + name + " in " + classLoader, e);
      }
    }
    return inCL;
  }
}
