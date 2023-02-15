package org.robolectric.shadows;

import android.companion.AssociationInfo;
import android.net.MacAddress;

/** Builder for {@link AssociationInfo}. */
public class AssociationInfoBuilder {
  private int id;
  private int userId;
  private String packageName;
  private String deviceMacAddress;
  private CharSequence displayName;
  private String deviceProfile;
  private boolean selfManaged;
  private boolean notifyOnDeviceNearby;
  private long approvedMs;
  private long lastTimeConnectedMs;

  private AssociationInfoBuilder() {}

  public static AssociationInfoBuilder newBuilder() {
    return new AssociationInfoBuilder();
  }

  public AssociationInfoBuilder setId(int id) {
    this.id = id;
    return this;
  }

  public AssociationInfoBuilder setUserId(int userId) {
    this.userId = userId;
    return this;
  }

  public AssociationInfoBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public AssociationInfoBuilder setDeviceMacAddress(String deviceMacAddress) {
    this.deviceMacAddress = deviceMacAddress;
    return this;
  }

  public AssociationInfoBuilder setDisplayName(CharSequence displayName) {
    this.displayName = displayName;
    return this;
  }

  public AssociationInfoBuilder setDeviceProfile(String deviceProfile) {
    this.deviceProfile = deviceProfile;
    return this;
  }

  public AssociationInfoBuilder setSelfManaged(boolean selfManaged) {
    this.selfManaged = selfManaged;
    return this;
  }

  public AssociationInfoBuilder setNotifyOnDeviceNearby(boolean notifyOnDeviceNearby) {
    this.notifyOnDeviceNearby = notifyOnDeviceNearby;
    return this;
  }

  public AssociationInfoBuilder setApprovedMs(long approvedMs) {
    this.approvedMs = approvedMs;
    return this;
  }

  public AssociationInfoBuilder setLastTimeConnectedMs(long lastTimeConnectedMs) {
    this.lastTimeConnectedMs = lastTimeConnectedMs;
    return this;
  }

  public AssociationInfo build() {
    return new AssociationInfo(
        id,
        userId,
        packageName,
        MacAddress.fromString(deviceMacAddress),
        displayName,
        deviceProfile,
        selfManaged,
        notifyOnDeviceNearby,
        approvedMs,
        lastTimeConnectedMs);
  }
}
