package org.robolectric.shadows;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.UUID;
import org.robolectric.util.ReflectionHelpers;

/** Builder for {@link BluetoothGattCharacteristic}. */
public class BluetoothGattCharacteristicBuilder {

  private UUID uuid;
  private int properties;
  private int permissions;
  private int instanceId;

  private BluetoothGattCharacteristicBuilder() {}

  public static BluetoothGattCharacteristicBuilder newBuilder() {
    return new BluetoothGattCharacteristicBuilder();
  }

  @CanIgnoreReturnValue
  public BluetoothGattCharacteristicBuilder setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  @CanIgnoreReturnValue
  public BluetoothGattCharacteristicBuilder setProperties(int properties) {
    this.properties = properties;
    return this;
  }

  @CanIgnoreReturnValue
  public BluetoothGattCharacteristicBuilder setPermissions(int permissions) {
    this.permissions = permissions;
    return this;
  }

  @CanIgnoreReturnValue
  public BluetoothGattCharacteristicBuilder setInstanceId(int instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public BluetoothGattCharacteristic build() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return new BluetoothGattCharacteristic(uuid, instanceId, properties, permissions);
    }
    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(uuid, properties, permissions);
    ReflectionHelpers.setField(characteristic, "mInstance", instanceId);
    return characteristic;
  }
}
