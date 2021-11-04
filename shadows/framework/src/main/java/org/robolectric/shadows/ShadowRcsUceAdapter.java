package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.Uri;
import android.telephony.ims.RcsContactUceCapability;
import android.telephony.ims.RcsUceAdapter;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
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
  private static final Map<Uri, RcsContactUceCapability> capabilitiesMap = new HashMap<>();
  private static final Map<Uri, CapabilityFailureInfo> capabilitiesFailureMap = new HashMap<>();

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
    capabilitiesMap.clear();
    capabilitiesFailureMap.clear();
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

  public static void setCapabilitiesForUri(Uri uri, RcsContactUceCapability capabilities) {
    capabilitiesMap.put(uri, capabilities);
  }

  public static void setCapabilitiesFailureForUri(Uri uri, CapabilityFailureInfo failureInfo) {
    capabilitiesFailureMap.put(uri, failureInfo);
  }

  @Implementation(minSdk = S)
  protected void requestCapabilities(
      Collection<Uri> contactNumbers, Executor executor, RcsUceAdapter.CapabilitiesCallback c) {
    boolean completedSuccessfully = true;
    for (Uri contact : contactNumbers) {
      if (capabilitiesFailureMap.containsKey(contact)) {
        CapabilityFailureInfo failureInfo = capabilitiesFailureMap.get(contact);
        executor.execute(() -> c.onError(failureInfo.errorCode(), failureInfo.retryMillis()));
        completedSuccessfully = false;
        break;
      }
      if (capabilitiesMap.containsKey(contact)) {
        executor.execute(
            () -> c.onCapabilitiesReceived(ImmutableList.of(capabilitiesMap.get(contact))));
      } else {
        executor.execute(
            () ->
                c.onCapabilitiesReceived(
                    ImmutableList.of(new RcsContactUceCapability.OptionsBuilder(contact).build())));
      }
    }
    if (completedSuccessfully) {
      executor.execute(c::onComplete);
    }
  }

  @Implementation(minSdk = S)
  protected void requestAvailability(
      Uri contactNumber, Executor executor, RcsUceAdapter.CapabilitiesCallback c) {
    requestCapabilities(ImmutableList.of(contactNumber), executor, c);
  }

  /** A data class holding the info for a failed capabilities exchange */
  @AutoValue
  public abstract static class CapabilityFailureInfo {
    public static CapabilityFailureInfo create(int errorCode, long retryMillis) {
      return new AutoValue_ShadowRcsUceAdapter_CapabilityFailureInfo(errorCode, retryMillis);
    }

    public abstract int errorCode();

    public abstract long retryMillis();
  }

  /** Accessor interface for {@link RcsUceAdapter}'s internals. */
  @ForType(RcsUceAdapter.class)
  private interface ReflectorRcsUceAdapter {
    @Accessor("mSubId")
    int getSubId();
  }
}
