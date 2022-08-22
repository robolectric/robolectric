package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import dalvik.system.CloseGuard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link CloseGuard}. {@code CloseGuardRule} can be used to easily verify all
 * CloseGuards have been closed.
 */
@Implements(value = CloseGuard.class, isInAndroidSdk = false)
public class ShadowCloseGuard {

  private static final Set<CloseGuard> openCloseGuards =
      Collections.synchronizedSet(new HashSet<>());
  private static final Set<Throwable> warnedThrowables =
      Collections.synchronizedSet(new HashSet<>());

  @RealObject private CloseGuard realCloseGuard;
  @ReflectorObject private CloseGuardReflector closeGuardReflector;

  @Implementation
  protected void open(String closer) {
    closeGuardReflector.open(closer);
    openCloseGuards.add(realCloseGuard);
  }

  @Implementation
  protected void close() {
    closeGuardReflector.close();
    openCloseGuards.remove(realCloseGuard);
  }

  @Implementation
  protected void warnIfOpen() {
    closeGuardReflector.warnIfOpen();
    if (openCloseGuards.contains(realCloseGuard)) {
      warnedThrowables.add(createThrowableFromCloseGuard(realCloseGuard));
    }
  }

  @Resetter
  public static void reset() {
    openCloseGuards.clear();
    warnedThrowables.clear();
  }

  public static ArrayList<Throwable> getErrors() {
    ArrayList<Throwable> errors = new ArrayList<>(openCloseGuards.size() + warnedThrowables.size());
    for (CloseGuard closeGuard : openCloseGuards) {
      errors.add(createThrowableFromCloseGuard(closeGuard));
    }
    errors.addAll(warnedThrowables);
    return errors;
  }

  private static Throwable createThrowableFromCloseGuard(CloseGuard closeGuard) {
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      Object closerNameOrAllocationInfo =
          reflector(CloseGuardReflector.class, closeGuard).getCloserNameOrAllocationInfo();
      if (closerNameOrAllocationInfo instanceof Throwable) {
        return (Throwable) closerNameOrAllocationInfo;
      } else if (closerNameOrAllocationInfo instanceof String) {
        return new Throwable((String) closerNameOrAllocationInfo);
      }
    } else {
      Throwable allocationSite =
          reflector(CloseGuardReflector.class, closeGuard).getAllocationSite();
      if (allocationSite != null) {
        return allocationSite;
      }
    }
    return new Throwable("CloseGuard with no allocation info");
  }

  @ForType(CloseGuard.class)
  interface CloseGuardReflector {

    @Direct
    void open(String closer);

    @Direct
    void close();

    @Direct
    void warnIfOpen();

    // For API 29+
    @Accessor("closerNameOrAllocationInfo")
    Object getCloserNameOrAllocationInfo();

    // For API <= 28
    @Accessor("allocationSite")
    Throwable getAllocationSite();
  }
}
