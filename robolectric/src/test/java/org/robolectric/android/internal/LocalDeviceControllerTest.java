package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.res.Configuration;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.device.DeviceController;
import androidx.test.platform.device.DeviceController.ScreenOrientation;
import androidx.test.platform.device.UnsupportedDeviceOperationException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(qualifiers = "w411dp-h891dp-port")
public class LocalDeviceControllerTest {
  private final DeviceController deviceController = new LocalDeviceController();

  @Test
  public void isLoadedAsTheDeviceController() {
    List<Class<?>> deviceControllerClasses = new ArrayList<>();
    for (DeviceController loaded : ServiceLoader.load(DeviceController.class)) {
      deviceControllerClasses.add(loaded.getClass());
    }

    assertThat(deviceControllerClasses).containsExactly(LocalDeviceController.class);
  }

  @Test
  public void setScreenOrientation_changesTheOrientation() {
    deviceController.setScreenOrientation(ScreenOrientation.LANDSCAPE.ordinal());

    Configuration configuration =
        ApplicationProvider.getApplicationContext().getResources().getConfiguration();
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
    assertThat(configuration.screenWidthDp).isEqualTo(891);

    deviceController.setScreenOrientation(ScreenOrientation.PORTRAIT.ordinal());

    configuration = ApplicationProvider.getApplicationContext().getResources().getConfiguration();
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
    assertThat(configuration.screenWidthDp).isEqualTo(411);
  }

  @Test
  public void setDeviceMode_isUnsupported() {
    assertThrows(
        UnsupportedDeviceOperationException.class, () -> deviceController.setDeviceMode(0));
  }
}
