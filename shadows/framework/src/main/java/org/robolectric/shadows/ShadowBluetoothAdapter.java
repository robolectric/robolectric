package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.ParcelUuid;
import android.provider.Settings;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BluetoothAdapter.class)
public class ShadowBluetoothAdapter {
  @RealObject private BluetoothAdapter realAdapter;

  private static final int ADDRESS_LENGTH = 17;
  private static final int LE_MAXIMUM_ADVERTISING_DATA_LENGTH = 31;
  private static final int LE_MAXIMUM_ADVERTISING_DATA_LENGTH_EXTENDED = 1650;

  private static boolean isBluetoothSupported = true;

  private Set<BluetoothDevice> bondedDevices = new HashSet<BluetoothDevice>();
  private Set<LeScanCallback> leScanCallbacks = new HashSet<LeScanCallback>();
  private boolean isDiscovering;
  private String address;
  private int state;
  private String name = "DefaultBluetoothDeviceName";
  private int scanMode = BluetoothAdapter.SCAN_MODE_NONE;
  private int discoverableTimeout = 0;
  private boolean isBleScanAlwaysAvailable = true;
  private boolean isMultipleAdvertisementSupported = true;
  private boolean isLeExtendedAdvertisingSupported = true;
  private boolean isOverridingProxyBehavior;
  private final Map<Integer, Integer> profileConnectionStateData = new HashMap<>();
  private final Map<Integer, BluetoothProfile> profileProxies = new HashMap<>();

  @Resetter
  public static void reset() {
    setIsBluetoothSupported(true);
    BluetoothAdapterReflector bluetoothReflector = reflector(BluetoothAdapterReflector.class);
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel >= VERSION_CODES.LOLLIPOP && apiLevel <= VERSION_CODES.R) {
      bluetoothReflector.setSBluetoothLeAdvertiser(null);
      bluetoothReflector.setSBluetoothLeScanner(null);
    }
    bluetoothReflector.setAdapter(null);
  }

  @Implementation
  protected static BluetoothAdapter getDefaultAdapter() {
    if (!isBluetoothSupported) {
      return null;
    }
    return reflector(BluetoothAdapterReflector.class).getDefaultAdapter();
  }

  /** Determines if getDefaultAdapter() returns the default local adapter (true) or null (false). */
  public static void setIsBluetoothSupported(boolean supported) {
    isBluetoothSupported = supported;
  }

  /** @deprecated use real BluetoothLeAdvertiser instead */
  @Deprecated
  public void setBluetoothLeAdvertiser(BluetoothLeAdvertiser advertiser) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.LOLLIPOP_MR1) {
      reflector(BluetoothAdapterReflector.class, realAdapter).setSBluetoothLeAdvertiser(advertiser);
    } else {
      reflector(BluetoothAdapterReflector.class, realAdapter).setBluetoothLeAdvertiser(advertiser);
    }
  }

  @Implementation
  protected Set<BluetoothDevice> getBondedDevices() {
    return Collections.unmodifiableSet(bondedDevices);
  }

  public void setBondedDevices(Set<BluetoothDevice> bluetoothDevices) {
    bondedDevices = bluetoothDevices;
  }

  @Implementation
  protected BluetoothServerSocket listenUsingInsecureRfcommWithServiceRecord(
      String serviceName, UUID uuid) {
    return ShadowBluetoothServerSocket.newInstance(
        BluetoothSocket.TYPE_RFCOMM, /*auth=*/ false, /*encrypt=*/ false, new ParcelUuid(uuid));
  }

  @Implementation
  protected BluetoothServerSocket listenUsingRfcommWithServiceRecord(String serviceName, UUID uuid)
      throws IOException {
    return ShadowBluetoothServerSocket.newInstance(
        BluetoothSocket.TYPE_RFCOMM, /*auth=*/ false, /*encrypt=*/ true, new ParcelUuid(uuid));
  }

  @Implementation
  protected boolean startDiscovery() {
    isDiscovering = true;
    return true;
  }

  @Implementation
  protected boolean cancelDiscovery() {
    isDiscovering = false;
    return true;
  }

  /** When true, overrides the value of {@link #getLeState}. By default, this is false. */
  @Implementation(minSdk = M)
  protected boolean isBleScanAlwaysAvailable() {
    return isBleScanAlwaysAvailable;
  }

  /**
   * Decides the correct LE state. When off, BLE calls will fail or return null.
   *
   * <p>LE is enabled if either Bluetooth or BLE scans are enabled. LE is always off if Airplane
   * Mode is enabled.
   */
  @Implementation(minSdk = M)
  public int getLeState() {
    if (isAirplaneMode()) {
      return BluetoothAdapter.STATE_OFF;
    }

    if (isEnabled()) {
      return BluetoothAdapter.STATE_ON;
    }

    if (isBleScanAlwaysAvailable()) {
      return BluetoothAdapter.STATE_BLE_ON;
    }

    return BluetoothAdapter.STATE_OFF;
  }

  /**
   * True if either Bluetooth is enabled or BLE scanning is available. Always false if Airplane Mode
   * is enabled. When false, BLE scans will fail. @Implementation(minSdk = M) protected boolean
   * isLeEnabled() { if (isAirplaneMode()) { return false; } return isEnabled() ||
   * isBleScanAlwaysAvailable(); }
   */
  private static boolean isAirplaneMode() {
    Context context = RuntimeEnvironment.getApplication();
    return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0)
        != 0;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected boolean startLeScan(LeScanCallback callback) {
    return startLeScan(null, callback);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected boolean startLeScan(UUID[] serviceUuids, LeScanCallback callback) {
    if (Build.VERSION.SDK_INT >= M && !realAdapter.isLeEnabled()) {
      return false;
    }

    // Ignoring the serviceUuids param for now.
    leScanCallbacks.add(callback);
    return true;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected void stopLeScan(LeScanCallback callback) {
    leScanCallbacks.remove(callback);
  }

  public Set<LeScanCallback> getLeScanCallbacks() {
    return Collections.unmodifiableSet(leScanCallbacks);
  }

  public LeScanCallback getSingleLeScanCallback() {
    if (leScanCallbacks.size() != 1) {
      throw new IllegalStateException("There are " + leScanCallbacks.size() + " callbacks");
    }
    return leScanCallbacks.iterator().next();
  }

  @Implementation
  protected boolean isDiscovering() {
    return isDiscovering;
  }

  @Implementation
  protected boolean isEnabled() {
    return state == BluetoothAdapter.STATE_ON;
  }

  @Implementation
  protected boolean enable() {
    setState(BluetoothAdapter.STATE_ON);
    return true;
  }

  @Implementation
  protected boolean disable() {
    setState(BluetoothAdapter.STATE_OFF);
    return true;
  }

  @Implementation
  protected String getAddress() {
    return this.address;
  }

  @Implementation
  protected int getState() {
    return state;
  }

  @Implementation
  protected String getName() {
    return name;
  }

  @Implementation
  protected boolean setName(String name) {
    this.name = name;
    return true;
  }

  @Implementation
  protected boolean setScanMode(int scanMode) {
    if (scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE
        && scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
        && scanMode != BluetoothAdapter.SCAN_MODE_NONE) {
      return false;
    }

    this.scanMode = scanMode;
    return true;
  }

  @Implementation(maxSdk = Q)
  protected boolean setScanMode(int scanMode, int discoverableTimeout) {
    setDiscoverableTimeout(discoverableTimeout);
    return setScanMode(scanMode);
  }

  @Implementation(minSdk = R)
  protected boolean setScanMode(int scanMode, long durationMillis) {
    int durationSeconds = Math.toIntExact(durationMillis / 1000);
    setDiscoverableTimeout(durationSeconds);
    return setScanMode(scanMode);
  }

  @Implementation
  protected int getScanMode() {
    return scanMode;
  }

  @Implementation
  protected int getDiscoverableTimeout() {
    return discoverableTimeout;
  }

  @Implementation
  protected void setDiscoverableTimeout(int timeout) {
    discoverableTimeout = timeout;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean isMultipleAdvertisementSupported() {
    return isMultipleAdvertisementSupported;
  }

  /**
   * Validate a Bluetooth address, such as "00:43:A8:23:10:F0" Alphabetic characters must be
   * uppercase to be valid.
   *
   * @param address Bluetooth address as string
   * @return true if the address is valid, false otherwise
   */
  @Implementation
  protected static boolean checkBluetoothAddress(String address) {
    if (address == null || address.length() != ADDRESS_LENGTH) {
      return false;
    }
    for (int i = 0; i < ADDRESS_LENGTH; i++) {
      char c = address.charAt(i);
      switch (i % 3) {
        case 0:
        case 1:
          if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
            // hex character, OK
            break;
          }
          return false;
        case 2:
          if (c == ':') {
            break; // OK
          }
          return false;
      }
    }
    return true;
  }

  /**
   * Returns the connection state for the given Bluetooth {@code profile}, defaulting to {@link
   * BluetoothProfile.STATE_DISCONNECTED} if the profile's connection state was never set.
   *
   * <p>Set a Bluetooth profile's connection state via {@link #setProfileConnectionState(int, int)}.
   */
  @Implementation
  protected int getProfileConnectionState(int profile) {
    Integer state = profileConnectionStateData.get(profile);
    if (state == null) {
      return BluetoothProfile.STATE_DISCONNECTED;
    }
    return state;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setState(int state) {
    this.state = state;
  }

  /** @deprecated Use {@link BluetoothAdapter#enable()} or {@link BluetoothAdapter#disable()}. */
  @Deprecated
  public void setEnabled(boolean enabled) {
    if (enabled) {
      enable();
    } else {
      disable();
    }
  }

  /**
   * Sets the value for {@link isBleScanAlwaysAvailable}. If true, {@link getLeState} will always
   * return true.
   */
  public void setBleScanAlwaysAvailable(boolean alwaysAvailable) {
    isBleScanAlwaysAvailable = alwaysAvailable;
  }

  /** Sets the value for {@link isMultipleAdvertisementSupported}. */
  public void setIsMultipleAdvertisementSupported(boolean supported) {
    isMultipleAdvertisementSupported = supported;
  }

  /** Sets the connection state {@code state} for the given BluetoothProfile {@code profile} */
  public void setProfileConnectionState(int profile, int state) {
    profileConnectionStateData.put(profile, state);
  }

  /**
   * Sets the active BluetoothProfile {@code proxy} for the given {@code profile}. Will always
   * affect behavior of {@link BluetoothAdapter#getProfileProxy} and {@link
   * BluetoothAdapter#closeProfileProxy}. Call to {@link BluetoothAdapter#closeProfileProxy} can
   * remove the set active proxy.
   *
   * @param proxy can be 'null' to simulate the situation where {@link
   *     BluetoothAdapter#getProfileProxy} would return 'false'. This can happen on older Android
   *     versions for Bluetooth profiles introduced in later Android versions.
   */
  public void setProfileProxy(int profile, @Nullable BluetoothProfile proxy) {
    isOverridingProxyBehavior = true;
    if (proxy != null) {
      profileProxies.put(profile, proxy);
    }
  }

  /**
   * @return 'true' if active (non-null) proxy has been set by {@link
   *     ShadowBluetoothAdapter#setProfileProxy} for the given {@code profile} AND it has not been
   *     "deactivated" by a call to {@link BluetoothAdapter#closeProfileProxy}. Only meaningful if
   *     {@link ShadowBluetoothAdapter#setProfileProxy} has been previously called.
   */
  public boolean hasActiveProfileProxy(int profile) {
    return profileProxies.get(profile) != null;
  }

  /**
   * Overrides behavior of {@link getProfileProxy} if {@link ShadowBluetoothAdapter#setProfileProxy}
   * has been previously called.
   *
   * <p>If active (non-null) proxy has been set by {@link setProfileProxy} for the given {@code
   * profile}, {@link getProfileProxy} will immediately call {@code onServiceConnected} of the given
   * BluetoothProfile.ServiceListener {@code listener}.
   *
   * @return 'true' if a proxy object has been set by {@link setProfileProxy} for the given
   *     BluetoothProfile {@code profile}
   */
  @Implementation
  protected boolean getProfileProxy(
      Context context, BluetoothProfile.ServiceListener listener, int profile) {
    if (!isOverridingProxyBehavior) {
      return directlyOn(realAdapter, BluetoothAdapter.class)
          .getProfileProxy(context, listener, profile);
    }

    BluetoothProfile proxy = profileProxies.get(profile);
    if (proxy == null) {
      return false;
    } else {
      listener.onServiceConnected(profile, proxy);
      return true;
    }
  }

  /**
   * Overrides behavior of {@link closeProfileProxy} if {@link
   * ShadowBluetoothAdapter#setProfileProxy} has been previously called.
   *
   * If the given non-null BluetoothProfile {@code proxy} was previously set for the given {@code
   * profile} by {@link ShadowBluetoothAdapter#setProfileProxy}, this proxy will be "deactivated".
   */
  @Implementation
  protected void closeProfileProxy(int profile, BluetoothProfile proxy) {
    if (!isOverridingProxyBehavior) {
      directlyOn(realAdapter, BluetoothAdapter.class).closeProfileProxy(profile, proxy);
      return;
    }

    if (proxy != null && proxy.equals(profileProxies.get(profile))) {
      profileProxies.remove(profile);
    }
  }

  /** Returns the last value of {@link #setIsLeExtendedAdvertisingSupported}, defaulting to true. */
  @Implementation(minSdk = O)
  protected boolean isLeExtendedAdvertisingSupported() {
    return isLeExtendedAdvertisingSupported;
  }

  /**
   * Sets the isLeExtendedAdvertisingSupported to enable/disable LE extended advertisements feature
   */
  public void setIsLeExtendedAdvertisingSupported(boolean supported) {
    isLeExtendedAdvertisingSupported = supported;
  }

  @Implementation(minSdk = O)
  protected int getLeMaximumAdvertisingDataLength() {
    return isLeExtendedAdvertisingSupported
        ? LE_MAXIMUM_ADVERTISING_DATA_LENGTH_EXTENDED
        : LE_MAXIMUM_ADVERTISING_DATA_LENGTH;
  }

  @ForType(BluetoothAdapter.class)
  interface BluetoothAdapterReflector {

    @Static
    @Direct
    BluetoothAdapter getDefaultAdapter();

    @Accessor("sAdapter")
    @Static
    void setAdapter(BluetoothAdapter adapter);

    @Accessor("mBluetoothLeAdvertiser")
    @Deprecated
    void setBluetoothLeAdvertiser(BluetoothLeAdvertiser advertiser);

    @Accessor("sBluetoothLeAdvertiser")
    @Static
    void setSBluetoothLeAdvertiser(BluetoothLeAdvertiser advertiser);

    @Accessor("sBluetoothLeScanner")
    @Static
    void setSBluetoothLeScanner(BluetoothLeScanner scanner);
  }
}
