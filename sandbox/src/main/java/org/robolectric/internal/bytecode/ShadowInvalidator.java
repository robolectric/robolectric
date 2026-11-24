package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MutableCallSite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks the call sites that are bound to each class's shadow, so that they can be bound again when
 * the shadow of that class changes.
 *
 * <p>A bound call site calls its target directly. Invalidating a class points each of its call
 * sites at a handle that binds the site again on its next call.
 */
public class ShadowInvalidator {
  private final Map<String, List<BoundCallSite>> boundCallSites = new HashMap<>();

  /**
   * Points {@code site} at {@code target}, which was looked up with the current shadow of {@code
   * className}. When that class is invalidated the site is pointed at {@code rebind} instead.
   */
  public synchronized void bind(
      String className, MutableCallSite site, MethodHandle target, MethodHandle rebind) {
    boundCallSites
        .computeIfAbsent(className, k -> new ArrayList<>())
        .add(new BoundCallSite(site, rebind));
    site.setTarget(target);
  }

  /** Makes every call site that is bound for one of {@code classNames} bind again. */
  public synchronized void invalidateClasses(Collection<String> classNames) {
    List<MutableCallSite> invalidated = new ArrayList<>();
    for (String className : classNames) {
      List<BoundCallSite> callSites = boundCallSites.remove(className);
      if (callSites == null) {
        continue;
      }
      for (BoundCallSite callSite : callSites) {
        callSite.site.setTarget(callSite.rebind);
        invalidated.add(callSite.site);
      }
    }
    if (!invalidated.isEmpty()) {
      // Makes other threads, and code compiled against the old targets, see the new ones.
      MutableCallSite.syncAll(invalidated.toArray(new MutableCallSite[0]));
    }
  }

  private static final class BoundCallSite {
    final MutableCallSite site;
    final MethodHandle rebind;

    BoundCallSite(MutableCallSite site, MethodHandle rebind) {
      this.site = site;
      this.rebind = rebind;
    }
  }
}
