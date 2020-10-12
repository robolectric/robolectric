package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.telephony.ims.RcsUceAdapter;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** A shadow for {@link RcsUceAdapter}. */
@Implements(
    value = RcsUceAdapter.class,
    // turn off shadowOf generation
    isInAndroidSdk = false,
    minSdk = R)
public class ShadowRcsUceAdapter {
  private static final Set<Integer> subscriptionIdsWithUceSettingEnabled = new HashSet<>();

  @RealObject private RcsUceAdapter realRcsUceAdapter;

  /**
   * Overrides the value returned by {@link RcsUceAdapter#isUceSettingEnabled()} for RcsUceAdapters
   * associated with {@code subscriptionId}.
   */
  public static void setUceSettingEnabledForSubscriptionId(
      int subscriptionId, boolean uceSettingEnabled) {
    if (uceSettingEnabled) {
      subscriptionIdsWithUceSettingEnabled.add(subscriptionId);
    } else {
      subscriptionIdsWithUceSettingEnabled.remove(subscriptionId);
    }
  }

  @Resetter
  public static void reset() {
    subscriptionIdsWithUceSettingEnabled.clear();
  }

  /**
   * Returns the value specified for the {@code subscriptionId} corresponding to the {@link
   * RcsUceAdapter} by {@link ShadowRcsUceAdapter#setUceSettingEnabledForSubscriptionId(int,
   * boolean)}. If no value has been specified, returns false.
   */
  @Implementation
  protected boolean isUceSettingEnabled() {
    int subscriptionId = reflector(ReflectorRcsUceAdapter.class, realRcsUceAdapter).getSubId();
    return subscriptionIdsWithUceSettingEnabled.contains(subscriptionId);
  }

  /** Accessor interface for {@link RcsUceAdapter}'s internals. */
  @ForType(RcsUceAdapter.class)
  private interface ReflectorRcsUceAdapter {
    @Accessor("mSubId")
    int getSubId();
  }
}
