package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.companion.AssociationInfo;
import android.net.MacAddress;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.versioning.AndroidVersions.U;

/** Builder for {@link AssociationInfo}. */
public class AssociationInfoBuilder {
  private int id;
  private int userId;
  private String packageName;
  private String tag;
  private String deviceMacAddress;
  private CharSequence displayName;
  private String deviceProfile;
  private Object associatedDevice;
  private boolean selfManaged;
  private boolean notifyOnDeviceNearby;
  private long approvedMs;
  // We have two different constructors for AssociationInfo across
  // T branches. aosp has the constructor that takes a new "revoked" parameter.
  private boolean revoked;
  private boolean pending;
  private long lastTimeConnectedMs;
  private int systemDataSyncFlags;

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

  public AssociationInfoBuilder setTag(String tag) {
    this.tag = tag;
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

  public AssociationInfoBuilder setAssociatedDevice(Object associatedDevice) {
    this.associatedDevice = associatedDevice;
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

  public AssociationInfoBuilder setRevoked(boolean revoked) {
    this.revoked = revoked;
    return this;
  }

  public AssociationInfoBuilder setLastTimeConnectedMs(long lastTimeConnectedMs) {
    this.lastTimeConnectedMs = lastTimeConnectedMs;
    return this;
  }

  public AssociationInfoBuilder setSystemDataSyncFlags(int systemDataSyncFlags) {
    this.systemDataSyncFlags = systemDataSyncFlags;
    return this;
  }

  public AssociationInfo build() {
    try {
      MacAddress macAddress =
          deviceMacAddress == null ? null : MacAddress.fromString(deviceMacAddress);
      if (RuntimeEnvironment.getApiLevel() <= TIRAMISU) {
        // We have two different constructors for AssociationInfo across
        // T branches. aosp has the constructor that takes a new "revoked" parameter.
        // Since there is not deterministic way to know which branch we are running in,
        // we will reflect on the class to see if it has the mRevoked member.
        // Based on the result we will either invoke the constructor with "revoked" or the
        // one without this parameter.
        if (ReflectionHelpers.hasField(AssociationInfo.class, "mRevoked")) {
          return ReflectionHelpers.callConstructor(
              AssociationInfo.class,
              ClassParameter.from(int.class, id),
              ClassParameter.from(int.class, userId),
              ClassParameter.from(String.class, packageName),
              ClassParameter.from(MacAddress.class, macAddress),
              ClassParameter.from(CharSequence.class, displayName),
              ClassParameter.from(String.class, deviceProfile),
              ClassParameter.from(boolean.class, selfManaged),
              ClassParameter.from(boolean.class, notifyOnDeviceNearby),
              ClassParameter.from(boolean.class, revoked /*revoked only supported in aosp*/),
              ClassParameter.from(long.class, approvedMs),
              ClassParameter.from(long.class, lastTimeConnectedMs));
        } else {
          return ReflectionHelpers.callConstructor(
              AssociationInfo.class,
              ClassParameter.from(int.class, id),
              ClassParameter.from(int.class, userId),
              ClassParameter.from(String.class, packageName),
              ClassParameter.from(MacAddress.class, macAddress),
              ClassParameter.from(CharSequence.class, displayName),
              ClassParameter.from(String.class, deviceProfile),
              ClassParameter.from(boolean.class, selfManaged),
              ClassParameter.from(boolean.class, notifyOnDeviceNearby),
              ClassParameter.from(long.class, approvedMs),
              ClassParameter.from(long.class, lastTimeConnectedMs));
        }
      } else if (RuntimeEnvironment.getApiLevel() <= U.SDK_INT) {
        // AOSP does not yet contains the new fields - mAssociatedDevice & mSystemDataSyncFlags yet
        if (ReflectionHelpers.hasField(AssociationInfo.class, "mAssociatedDevice")) {
          return ReflectionHelpers.callConstructor(
              AssociationInfo.class,
              ClassParameter.from(int.class, id),
              ClassParameter.from(int.class, userId),
              ClassParameter.from(String.class, packageName),
              ClassParameter.from(MacAddress.class, macAddress),
              ClassParameter.from(CharSequence.class, displayName),
              ClassParameter.from(String.class, deviceProfile),
              ClassParameter.from(
                  Class.forName("android.companion.AssociatedDevice"), associatedDevice),
              ClassParameter.from(boolean.class, selfManaged),
              ClassParameter.from(boolean.class, notifyOnDeviceNearby),
              ClassParameter.from(boolean.class, revoked),
              ClassParameter.from(long.class, approvedMs),
              ClassParameter.from(long.class, lastTimeConnectedMs),
              ClassParameter.from(int.class, systemDataSyncFlags));
        } else {
          return ReflectionHelpers.callConstructor(
              AssociationInfo.class,
              ClassParameter.from(int.class, id),
              ClassParameter.from(int.class, userId),
              ClassParameter.from(String.class, packageName),
              ClassParameter.from(MacAddress.class, macAddress),
              ClassParameter.from(CharSequence.class, displayName),
              ClassParameter.from(String.class, deviceProfile),
              ClassParameter.from(boolean.class, selfManaged),
              ClassParameter.from(boolean.class, notifyOnDeviceNearby),
              ClassParameter.from(boolean.class, revoked),
              ClassParameter.from(long.class, approvedMs),
              ClassParameter.from(long.class, lastTimeConnectedMs));
        }
      } else {
        return ReflectionHelpers.callConstructor(
            AssociationInfo.class,
            ClassParameter.from(int.class, id),
            ClassParameter.from(int.class, userId),
            ClassParameter.from(String.class, packageName),
            ClassParameter.from(String.class, tag),
            ClassParameter.from(MacAddress.class, macAddress),
            ClassParameter.from(CharSequence.class, displayName),
            ClassParameter.from(String.class, deviceProfile),
            ClassParameter.from(
                Class.forName("android.companion.AssociatedDevice"), associatedDevice),
            ClassParameter.from(boolean.class, selfManaged),
            ClassParameter.from(boolean.class, notifyOnDeviceNearby),
            ClassParameter.from(boolean.class, revoked),
            ClassParameter.from(boolean.class, pending),
            ClassParameter.from(long.class, approvedMs),
            ClassParameter.from(long.class, lastTimeConnectedMs),
            ClassParameter.from(int.class, systemDataSyncFlags));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
