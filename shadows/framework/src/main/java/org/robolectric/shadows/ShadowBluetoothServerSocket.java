package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.ParcelUuid;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = BluetoothServerSocket.class)
public class ShadowBluetoothServerSocket {

  @SuppressLint("PrivateApi")
  @SuppressWarnings("unchecked")
  public static BluetoothServerSocket newInstance(
      int type, boolean auth, boolean encrypt, ParcelUuid uuid) {
    if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
      return Shadow.newInstance(
          BluetoothServerSocket.class,
          new Class<?>[] {Integer.TYPE, Boolean.TYPE, Boolean.TYPE, ParcelUuid.class},
          new Object[] {type, auth, encrypt, uuid});
    } else {
      return Shadow.newInstance(
          BluetoothServerSocket.class,
          new Class<?>[] {Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Integer.TYPE},
          new Object[] {type, auth, encrypt, getPort(uuid)});
    }
  }

  // Port ranges are valid from 1 to MAX_RFCOMM_CHANNEL.
  private static int getPort(ParcelUuid uuid) {
    return Math.abs(uuid.hashCode() % BluetoothSocket.MAX_RFCOMM_CHANNEL) + 1;
  }
}
