package org.robolectric.shadows;

import android.bluetooth.BluetoothGattService;
import android.os.Build;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.UUID;
import org.robolectric.util.ReflectionHelpers;

/** Builder for {@link BluetoothGattService}. */
public class BluetoothGattServiceBuilder {

  private UUID uuid;
  private int serviceType;
  private int instanceId;

  private BluetoothGattServiceBuilder() {}

  public static BluetoothGattServiceBuilder newBuilder() {
    return new BluetoothGattServiceBuilder();
  }

  @CanIgnoreReturnValue
  public BluetoothGattServiceBuilder setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  @CanIgnoreReturnValue
  public BluetoothGattServiceBuilder setServiceType(int serviceType) {
    this.serviceType = serviceType;
    return this;
  }

  @CanIgnoreReturnValue
  public BluetoothGattServiceBuilder setInstanceId(int instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public BluetoothGattService build() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return new BluetoothGattService(uuid, instanceId, serviceType);
    }
    BluetoothGattService service = new BluetoothGattService(uuid, serviceType);
    ReflectionHelpers.setField(service, "mInstanceId", instanceId);
    return service;
  }
}
