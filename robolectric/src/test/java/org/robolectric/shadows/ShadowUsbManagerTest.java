package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.getStaticField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowUsbManager._UsbManagerQ_;
import org.robolectric.shadows.ShadowUsbManager._UsbManager_;

/** Unit tests for {@link ShadowUsbManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowUsbManagerTest {
  private static final String DEVICE_NAME_1 = "usb1";
  private static final String DEVICE_NAME_2 = "usb2";

  private UsbManager usbManager;

  @Mock UsbDevice usbDevice1;
  @Mock UsbDevice usbDevice2;
  @Mock UsbAccessory usbAccessory;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    usbManager =
        (UsbManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.USB_SERVICE);

    when(usbDevice1.getDeviceName()).thenReturn(DEVICE_NAME_1);
    when(usbDevice2.getDeviceName()).thenReturn(DEVICE_NAME_2);
  }

  @Test
  public void getDeviceList() {
    assertThat(usbManager.getDeviceList()).isEmpty();
    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice1, true);
    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice2, true);
    assertThat(usbManager.getDeviceList().values()).containsExactly(usbDevice1, usbDevice2);
  }

  @Test
  public void hasPermission_device() {
    assertThat(usbManager.hasPermission(usbDevice1)).isFalse();

    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice1, false);
    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice2, false);

    assertThat(usbManager.hasPermission(usbDevice1)).isFalse();
    assertThat(usbManager.hasPermission(usbDevice2)).isFalse();

    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice1, true);

    assertThat(usbManager.hasPermission(usbDevice1)).isTrue();
    assertThat(usbManager.hasPermission(usbDevice2)).isFalse();
  }

  @Test
  public void hasPermission_accessory() {
    assertThat(usbManager.hasPermission(usbAccessory)).isFalse();
    shadowOf(usbManager).setAttachedUsbAccessory(usbAccessory);
    assertThat(usbManager.hasPermission(usbAccessory)).isFalse();
    shadowOf(usbManager).grantPermission(usbAccessory);
    assertThat(usbManager.hasPermission(usbAccessory)).isTrue();
    shadowOf(usbManager)
        .revokePermission(usbAccessory, RuntimeEnvironment.getApplication().getPackageName());
    assertThat(usbManager.hasPermission(usbAccessory)).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void grantPermission_selfPackage_shouldHavePermission() {
    usbManager.grantPermission(usbDevice1);

    assertThat(usbManager.hasPermission(usbDevice1)).isTrue();
  }

  @Test
  @Config(minSdk = N_MR1)
  public void grantPermission_differentPackage_shouldHavePermission() {
    usbManager.grantPermission(usbDevice1, "foo.bar");

    assertThat(shadowOf(usbManager).hasPermissionForPackage(usbDevice1, "foo.bar")).isTrue();
  }

  @Test
  @Config(minSdk = N_MR1)
  public void revokePermission_shouldNotHavePermission() {
    usbManager.grantPermission(usbDevice1, "foo.bar");
    assertThat(shadowOf(usbManager).hasPermissionForPackage(usbDevice1, "foo.bar")).isTrue();

    shadowOf(usbManager).revokePermission(usbDevice1, "foo.bar");

    assertThat(shadowOf(usbManager).hasPermissionForPackage(usbDevice1, "foo.bar")).isFalse();
  }

  @Test
  @Config(minSdk = M, maxSdk = P)
  public void getPorts_shouldReturnAddedPorts() {
    shadowOf(usbManager).addPort("port1");
    shadowOf(usbManager).addPort("port2");
    shadowOf(usbManager).addPort("port3");

    List<UsbPort> usbPorts = getUsbPorts();
    assertThat(usbPorts).hasSize(3);
    assertThat(usbPorts.stream().map(UsbPort::getId).collect(Collectors.toList()))
        .containsExactly("port1", "port2", "port3");
  }

  @Test
  @Config(minSdk = Q)
  public void getPortStatus_shouldReturnStatusForCorrespondingPort() {
    shadowOf(usbManager).addPort("port1");
    shadowOf(usbManager).addPort("port2");
    shadowOf(usbManager)
        .addPort(
            "port3",
            UsbPortStatus.MODE_DUAL,
            UsbPortStatus.POWER_ROLE_SINK,
            UsbPortStatus.DATA_ROLE_DEVICE,
            /*statusSupportedRoleCombinations=*/ 0);

    UsbPortStatus portStatus = (UsbPortStatus) shadowOf(usbManager).getPortStatus("port3");
    assertThat(portStatus.getCurrentMode()).isEqualTo(UsbPortStatus.MODE_DUAL);
    assertThat(portStatus.getCurrentPowerRole()).isEqualTo(UsbPortStatus.POWER_ROLE_SINK);
    assertThat(portStatus.getCurrentDataRole()).isEqualTo(UsbPortStatus.DATA_ROLE_DEVICE);
    assertThat(portStatus.getSupportedRoleCombinations()).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void clearPorts_shouldRemoveAllPorts() {
    shadowOf(usbManager).addPort("port1");
    shadowOf(usbManager).clearPorts();

    List<UsbPort> usbPorts = getUsbPorts();
    assertThat(usbPorts).isEmpty();
  }

  @Test
  @Config(minSdk = M, maxSdk = P)
  public void setPortRoles_sinkHost_shouldSetPortStatus() {
    final int powerRoleSink = getStaticField(UsbPort.class, "POWER_ROLE_SINK");
    final int dataRoleHost = getStaticField(UsbPort.class, "DATA_ROLE_HOST");

    shadowOf(usbManager).addPort("port1");

    List<UsbPort> usbPorts = getUsbPorts();

    _usbManager_().setPortRoles(usbPorts.get(0), powerRoleSink, dataRoleHost);

    UsbPortStatus usbPortStatus = _usbManager_().getPortStatus(usbPorts.get(0));
    assertThat(usbPortStatus.getCurrentPowerRole()).isEqualTo(powerRoleSink);
    assertThat(usbPortStatus.getCurrentDataRole()).isEqualTo(dataRoleHost);
  }

  @Test
  @Config(minSdk = Q)
  public void setPortRoles_sinkHost_shouldSetPortStatus_Q() {
    shadowOf(usbManager).addPort("port1");

    List<UsbPort> usbPorts = getUsbPorts();
    _usbManager_()
        .setPortRoles(usbPorts.get(0), UsbPortStatus.POWER_ROLE_SINK, UsbPortStatus.DATA_ROLE_HOST);

    UsbPortStatus usbPortStatus = _usbManager_().getPortStatus(usbPorts.get(0));
    assertThat(usbPortStatus.getCurrentPowerRole()).isEqualTo(UsbPortStatus.POWER_ROLE_SINK);
    assertThat(usbPortStatus.getCurrentDataRole()).isEqualTo(UsbPortStatus.DATA_ROLE_HOST);
  }

  @Test
  public void removeDevice() {
    assertThat(usbManager.getDeviceList()).isEmpty();
    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice1, false);
    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice2, false);

    assertThat(usbManager.getDeviceList().values()).containsExactly(usbDevice1, usbDevice2);

    shadowOf(usbManager).removeUsbDevice(usbDevice1);
    assertThat(usbManager.getDeviceList().values()).containsExactly(usbDevice2);
  }

  @Test
  public void openDevice() throws Exception {
    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice1, true);
    UsbDeviceConnection connection = usbManager.openDevice(usbDevice1);
    assertThat(connection).isNotNull();
  }

  @Test
  public void openAccessory() throws Exception {
    try (ParcelFileDescriptor pfd = usbManager.openAccessory(usbAccessory)) {
      assertThat(pfd).isNotNull();
    }
  }

  @Test
  public void setAccessory() {
    assertThat(usbManager.getAccessoryList()).isNull();
    shadowOf(usbManager).setAttachedUsbAccessory(usbAccessory);
    assertThat(usbManager.getAccessoryList()).hasLength(1);
    assertThat(usbManager.getAccessoryList()[0]).isEqualTo(usbAccessory);
  }

  /////////////////////////

  private List<UsbPort> getUsbPorts() {
    // return type changed from UsbPort[] to List<UsbPort> in Q...
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
      return reflector(_UsbManagerQ_.class, usbManager).getPorts();
    }
    return Arrays.asList(_usbManager_().getPorts());
  }

  private _UsbManager_ _usbManager_() {
    return reflector(_UsbManager_.class, usbManager);
  }
}
