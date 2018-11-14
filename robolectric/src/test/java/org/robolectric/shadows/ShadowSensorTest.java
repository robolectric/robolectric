package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.hardware.Sensor;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowSensor} */
@RunWith(AndroidJUnit4.class)
public class ShadowSensorTest {

  @Test
  public void getType() {
    Sensor sensor = ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER);
    assertThat(sensor.getType()).isEqualTo(Sensor.TYPE_ACCELEROMETER);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void getStringType() {
    Sensor sensor = ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER);
    assertThat(sensor.getStringType()).isEqualTo(Sensor.STRING_TYPE_ACCELEROMETER);
  }
}
