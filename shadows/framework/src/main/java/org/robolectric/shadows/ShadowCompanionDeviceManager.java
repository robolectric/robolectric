package org.robolectric.shadows;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.net.MacAddress;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for CompanionDeviceManager. */
@Implements(value = CompanionDeviceManager.class, minSdk = VERSION_CODES.O)
public class ShadowCompanionDeviceManager {

  protected final Set<RoboAssociationInfo> associations = new HashSet<>();
  protected final Set<ComponentName> hasNotificationAccess = new HashSet<>();
  protected ComponentName lastRequestedNotificationAccess;
  protected AssociationRequest lastAssociationRequest;
  protected CompanionDeviceManager.Callback lastAssociationCallback;

  @Implementation
  @SuppressWarnings("JdkCollectors") // toImmutableList is only supported in Java 8+.
  protected List<String> getAssociations() {
    return ImmutableList.copyOf(
        associations.stream().map(RoboAssociationInfo::deviceMacAddress).collect(toList()));
  }

  public void addAssociation(String newAssociation) {
    associations.add(RoboAssociationInfo.builder().setDeviceMacAddress(newAssociation).build());
  }

  public void addAssociation(AssociationInfo info) {
    associations.add(createShadowAssociationInfo(info));
  }

  @Implementation
  protected void disassociate(String deviceMacAddress) {
    RoboAssociationInfo associationInfo =
        associations.stream()
            .filter(
                association ->
                    Ascii.equalsIgnoreCase(deviceMacAddress, association.deviceMacAddress()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Association does not exist"));
    associations.remove(associationInfo);
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void disassociate(int associationId) {
    RoboAssociationInfo associationInfo =
        associations.stream()
            .filter(association -> associationId == association.id())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Association does not exist"));
    associations.remove(associationInfo);
  }

  @Implementation
  protected boolean hasNotificationAccess(ComponentName component) {
    checkHasAssociation();
    return hasNotificationAccess.contains(component);
  }

  public void setNotificationAccess(ComponentName component, boolean hasAccess) {
    if (hasAccess) {
      hasNotificationAccess.add(component);
    } else {
      hasNotificationAccess.remove(component);
    }
  }

  @Implementation
  protected void requestNotificationAccess(ComponentName component) {
    checkHasAssociation();
    lastRequestedNotificationAccess = component;
  }

  @Implementation
  protected void associate(
      AssociationRequest request, CompanionDeviceManager.Callback callback, Handler handler) {
    lastAssociationRequest = request;
    lastAssociationCallback = callback;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void associate(
      AssociationRequest request, Executor executor, CompanionDeviceManager.Callback callback) {
    associate(request, callback, /* handler= */ null);
  }

  public AssociationRequest getLastAssociationRequest() {
    return lastAssociationRequest;
  }

  public CompanionDeviceManager.Callback getLastAssociationCallback() {
    return lastAssociationCallback;
  }

  public ComponentName getLastRequestedNotificationAccess() {
    return lastRequestedNotificationAccess;
  }

  private void checkHasAssociation() {
    if (associations.isEmpty()) {
      throw new IllegalStateException("App must have an association before calling this API");
    }
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected List<AssociationInfo> getMyAssociations() {
    return this.associations.stream()
        .map(this::createAssociationInfo)
        .collect(toCollection(ArrayList::new));
  }

  /** Convert {@link RoboAssociationInfo} to actual {@link AssociationInfo}. */
  private AssociationInfo createAssociationInfo(RoboAssociationInfo info) {
    return new AssociationInfo(
        info.id(),
        info.userId(),
        info.packageName(),
        MacAddress.fromString(info.deviceMacAddress()),
        info.displayName(),
        info.deviceProfile(),
        info.selfManaged(),
        info.notifyOnDeviceNearby(),
        info.timeApprovedMs(),
        info.lastTimeConnectedMs());
  }

  private RoboAssociationInfo createShadowAssociationInfo(AssociationInfo info) {
    return RoboAssociationInfo.create(
        info.getId(),
        info.getUserId(),
        info.getPackageName(),
        info.getDeviceMacAddress().toString(),
        info.getDisplayName(),
        info.getDeviceProfile(),
        info.isSelfManaged(),
        info.isNotifyOnDeviceNearby(),
        info.getTimeApprovedMs(),
        info.getLastTimeConnectedMs());
  }

  /**
   * This is a copy of frameworks/base/core/java/android/companion/AssociationInfo.java to store
   * full AssociationInfo data without breaking existing Android test dependencies.
   */
  @AutoValue
  abstract static class RoboAssociationInfo {
    public abstract int id();

    public abstract int userId();

    @Nullable
    public abstract String packageName();

    @Nullable
    public abstract String deviceMacAddress();

    @Nullable
    public abstract CharSequence displayName();

    @Nullable
    public abstract String deviceProfile();

    public abstract boolean selfManaged();

    public abstract boolean notifyOnDeviceNearby();

    public abstract long timeApprovedMs();

    public abstract long lastTimeConnectedMs();

    public static Builder builder() {
      return new AutoValue_ShadowCompanionDeviceManager_RoboAssociationInfo.Builder()
          .setId(1)
          .setUserId(1)
          .setSelfManaged(false)
          .setNotifyOnDeviceNearby(false)
          .setTimeApprovedMs(0)
          .setLastTimeConnectedMs(0);
    }

    public static RoboAssociationInfo create(
        int id,
        int userId,
        String packageName,
        String deviceMacAddress,
        CharSequence displayName,
        String deviceProfile,
        boolean selfManaged,
        boolean notifyOnDeviceNearby,
        long timeApprovedMs,
        long lastTimeConnectedMs) {
      return RoboAssociationInfo.builder()
          .setId(id)
          .setUserId(userId)
          .setPackageName(packageName)
          .setDeviceMacAddress(deviceMacAddress)
          .setDisplayName(displayName)
          .setDeviceProfile(deviceProfile)
          .setSelfManaged(selfManaged)
          .setNotifyOnDeviceNearby(notifyOnDeviceNearby)
          .setTimeApprovedMs(timeApprovedMs)
          .setLastTimeConnectedMs(lastTimeConnectedMs)
          .build();
    }

    /** Builder for {@link AssociationInfo}. */
    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder setId(int id);

      public abstract Builder setUserId(int userId);

      public abstract Builder setPackageName(String packageName);

      public abstract Builder setDeviceMacAddress(String deviceMacAddress);

      public abstract Builder setDisplayName(CharSequence displayName);

      public abstract Builder setDeviceProfile(String deviceProfile);

      public abstract Builder setSelfManaged(boolean selfManaged);

      public abstract Builder setNotifyOnDeviceNearby(boolean notifyOnDeviceNearby);

      public abstract Builder setTimeApprovedMs(long timeApprovedMs);

      public abstract Builder setLastTimeConnectedMs(long lastTimeConnectedMs);

      public abstract RoboAssociationInfo build();
    }
  }
}
