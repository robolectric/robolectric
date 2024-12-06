package org.robolectric.shadows;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import android.Manifest.permission;
import android.annotation.Nullable;
import android.app.ActivityThread;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.companion.DeviceNotAssociatedException;
import android.content.ComponentName;
import android.net.MacAddress;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import com.google.auto.value.AutoValue;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for CompanionDeviceManager. */
@Implements(value = CompanionDeviceManager.class, minSdk = VERSION_CODES.O)
public class ShadowCompanionDeviceManager {

  private final Set<RoboAssociationInfo> associations = new HashSet<>();
  private final Set<ComponentName> hasNotificationAccess = new HashSet<>();
  private ComponentName lastRequestedNotificationAccess;
  private AssociationRequest lastAssociationRequest;
  private MacAddress lastSystemApiAssociationMacAddress;
  private CompanionDeviceManager.Callback lastAssociationCallback;
  private String lastObservingDevicePresenceDeviceAddress;

  private static final int DEFAULT_SYSTEMDATASYNCFLAGS = -1;

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

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void associate(String packageName, MacAddress macAddress, byte[] certificate) {
    lastSystemApiAssociationMacAddress = macAddress;
    if (!checkPermission(permission.ASSOCIATE_COMPANION_DEVICES)) {
      throw new SecurityException("Permission ASSOCIATE_COMPANION_DEVICES not granted");
    }
    if (!RuntimeEnvironment.getApplication().getPackageName().equals(packageName)) {
      throw new SecurityException("Calling application package does not equal packageName");
    }
    if (certificate == null) {
      // Check the null case for now as {@link PackageManager#hasSigningCertificate} is not yet
      // supported.
      throw new SecurityException("Certificate is null");
    }
    associations.add(
        RoboAssociationInfo.builder().setDeviceMacAddress(macAddress.toString()).build());
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void startObservingDevicePresence(String deviceAddress) {
    lastObservingDevicePresenceDeviceAddress = deviceAddress;
    for (RoboAssociationInfo association : associations) {
      if (association.deviceMacAddress() != null
          && Ascii.equalsIgnoreCase(deviceAddress, association.deviceMacAddress())) {
        return;
      }
    }
    throw new DeviceNotAssociatedException("Association does not exist");
  }

  /**
   * This method will return the last {@link AssociationRequest} passed to {@code
   * CompanionDeviceManager#associate(AssociationRequest, CompanionDeviceManager.Callback, Handler)}
   * or {@code CompanionDeviceManager#associate(AssociationRequest, Executor,
   * CompanionDeviceManager.Callback, Handler)}.
   *
   * <p>Note that the value returned is only changed when calling {@code associate} and will be set
   * if that method throws an exception. Moreover, this value will unchanged if disassociate is
   * called.
   */
  public AssociationRequest getLastAssociationRequest() {
    return lastAssociationRequest;
  }

  /**
   * This method will return the last {@link CompanionDeviceManager.Callback} passed to {@code
   * CompanionDeviceManager#associate(AssociationRequest, CompanionDeviceManager.Callback, Handler)}
   * or {@code CompanionDeviceManager#associate(AssociationRequest, Executor,
   * CompanionDeviceManager.Callback, Handler)}.
   *
   * <p>Note that the value returned is only changed when calling {@code associate} and will be set
   * if that method throws an exception. Moreover, this value will unchanged if disassociate is
   * called.
   */
  public CompanionDeviceManager.Callback getLastAssociationCallback() {
    return lastAssociationCallback;
  }

  /**
   * If an association is set, this method will return the last {@link ComponentName} passed to
   * {@code CompanionDeviceManager#requestNotificationAccess(ComponentName)}.
   */
  public ComponentName getLastRequestedNotificationAccess() {
    return lastRequestedNotificationAccess;
  }

  /**
   * Returns the last {@link MacAddress} passed to systemApi {@code associate}.
   *
   * <p>Note that the value returned is only changed when calling {@code associate} and will be set
   * if that method throws an exception. Moreover, this value will unchanged if disassociate is
   * called.
   */
  public MacAddress getLastSystemApiAssociationMacAddress() {
    return lastSystemApiAssociationMacAddress;
  }

  /**
   * Returns the last device address passed to {@link
   * CompanionDeviceManager#startObservingDevicePresence(String)}.
   *
   * <p>Note that the value returned is only changed when calling {@link
   * CompanionDeviceManager#startObservingDevicePresence(String)} and will still be set in the event
   * that this method throws an exception. Moreover, this value will unchanged if disassociate is
   * called.
   */
  public String getLastObservingDevicePresenceDeviceAddress() {
    return lastObservingDevicePresenceDeviceAddress;
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
    AssociationInfoBuilder aiBuilder =
        AssociationInfoBuilder.newBuilder()
            .setId(info.id())
            .setUserId(info.userId())
            .setPackageName(info.packageName())
            .setDeviceMacAddress(info.deviceMacAddress())
            .setDisplayName(info.displayName())
            .setDeviceProfile(info.deviceProfile())
            .setAssociatedDevice(info.associatedDevice())
            .setSelfManaged(info.selfManaged())
            .setNotifyOnDeviceNearby(info.notifyOnDeviceNearby())
            .setApprovedMs(info.timeApprovedMs())
            .setLastTimeConnectedMs(info.lastTimeConnectedMs());

    if (ReflectionHelpers.hasField(AssociationInfo.class, "mTag")) {
      ReflectionHelpers.callInstanceMethod(
          aiBuilder, "setTag", ClassParameter.from(String.class, info.tag()));
    }
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mAssociatedDevice")) {
      ReflectionHelpers.callInstanceMethod(
          aiBuilder,
          "setAssociatedDevice",
          ClassParameter.from(Object.class, info.associatedDevice()));
      ReflectionHelpers.callInstanceMethod(
          aiBuilder,
          "setSystemDataSyncFlags",
          ClassParameter.from(int.class, info.systemDataSyncFlags()));
    }
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mRevoked")) {
      ReflectionHelpers.callInstanceMethod(
          aiBuilder, "setRevoked", ClassParameter.from(boolean.class, info.revoked()));
    }
    return aiBuilder.build();
  }

  private RoboAssociationInfo createShadowAssociationInfo(AssociationInfo info) {
    Object associatedDevice = null;
    int systemDataSyncFlags = DEFAULT_SYSTEMDATASYNCFLAGS;
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mAssociatedDevice")) {
      associatedDevice = ReflectionHelpers.callInstanceMethod(info, "getAssociatedDevice");
      systemDataSyncFlags = ReflectionHelpers.callInstanceMethod(info, "getSystemDataSyncFlags");
    }
    boolean revoked = false;
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mRevoked")) {
      revoked = ReflectionHelpers.callInstanceMethod(info, "isRevoked");
    }
    String tag = "";
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mTag")) {
      tag = ReflectionHelpers.callInstanceMethod(info, "getTag");
    }
    return RoboAssociationInfo.create(
        info.getId(),
        info.getUserId(),
        info.getPackageName(),
        tag,
        info.getDeviceMacAddress() == null ? null : info.getDeviceMacAddress().toString(),
        info.getDisplayName(),
        info.getDeviceProfile(),
        associatedDevice,
        info.isSelfManaged(),
        info.isNotifyOnDeviceNearby(),
        revoked,
        info.getTimeApprovedMs(),
        // return value of getLastTimeConnectedMs changed from a long to a Long
        (long) ReflectionHelpers.callInstanceMethod(info, "getLastTimeConnectedMs"),
        systemDataSyncFlags);
  }

  private static boolean checkPermission(String permission) {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation =
        Shadow.extract(activityThread.getInstrumentation());
    return shadowInstrumentation.checkPermission(
            permission, android.os.Process.myPid(), android.os.Process.myUid())
        == PERMISSION_GRANTED;
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
    public abstract String tag();

    @Nullable
    public abstract String deviceMacAddress();

    @Nullable
    public abstract CharSequence displayName();

    @Nullable
    public abstract String deviceProfile();

    @Nullable
    public abstract Object associatedDevice();

    public abstract boolean selfManaged();

    public abstract boolean notifyOnDeviceNearby();

    public abstract boolean revoked();

    public abstract long timeApprovedMs();

    public abstract long lastTimeConnectedMs();

    public abstract int systemDataSyncFlags();

    public static Builder builder() {
      return new AutoValue_ShadowCompanionDeviceManager_RoboAssociationInfo.Builder()
          .setId(1)
          .setUserId(1)
          .setSelfManaged(false)
          .setNotifyOnDeviceNearby(false)
          .setRevoked(false)
          .setAssociatedDevice(null)
          .setTimeApprovedMs(0)
          .setLastTimeConnectedMs(0L)
          .setSystemDataSyncFlags(DEFAULT_SYSTEMDATASYNCFLAGS);
    }

    public static RoboAssociationInfo create(
        int id,
        int userId,
        String packageName,
        String tag,
        String deviceMacAddress,
        CharSequence displayName,
        String deviceProfile,
        Object associatedDevice,
        boolean selfManaged,
        boolean notifyOnDeviceNearby,
        boolean revoked,
        long timeApprovedMs,
        long lastTimeConnectedMs,
        int systemDataSyncFlags) {
      return RoboAssociationInfo.builder()
          .setId(id)
          .setUserId(userId)
          .setPackageName(packageName)
          .setTag(tag)
          .setDeviceMacAddress(deviceMacAddress)
          .setDisplayName(displayName)
          .setDeviceProfile(deviceProfile)
          .setAssociatedDevice(associatedDevice)
          .setSelfManaged(selfManaged)
          .setNotifyOnDeviceNearby(notifyOnDeviceNearby)
          .setTimeApprovedMs(timeApprovedMs)
          .setRevoked(revoked)
          .setLastTimeConnectedMs(lastTimeConnectedMs)
          .setSystemDataSyncFlags(systemDataSyncFlags)
          .build();
    }

    /** Builder for {@link AssociationInfo}. */
    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder setId(int id);

      public abstract Builder setUserId(int userId);

      public abstract Builder setPackageName(String packageName);

      public abstract Builder setTag(String tag);

      public abstract Builder setDeviceMacAddress(String deviceMacAddress);

      public abstract Builder setDisplayName(CharSequence displayName);

      public abstract Builder setDeviceProfile(String deviceProfile);

      public abstract Builder setSelfManaged(boolean selfManaged);

      public abstract Builder setAssociatedDevice(Object device);

      public abstract Builder setNotifyOnDeviceNearby(boolean notifyOnDeviceNearby);

      public abstract Builder setRevoked(boolean revoked);

      public abstract Builder setTimeApprovedMs(long timeApprovedMs);

      public abstract Builder setLastTimeConnectedMs(long lastTimeConnectedMs);

      public abstract Builder setSystemDataSyncFlags(int flags);

      public abstract RoboAssociationInfo build();
    }
  }
}
