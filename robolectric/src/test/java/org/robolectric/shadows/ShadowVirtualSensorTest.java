package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;

import android.companion.virtual.sensor.VirtualSensor;
import android.companion.virtual.sensor.VirtualSensorEvent;
import android.hardware.Sensor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit test for ShadowVirtualSensor. */
@Config(minSdk = UPSIDE_DOWN_CAKE)
@RunWith(RobolectricTestRunner.class)
public class ShadowVirtualSensorTest {

  @Test
  public void virtualSensor_getDeviceId() {
    VirtualSensor sensor = (VirtualSensor) createVirtualSensor();
    ((ShadowVirtualSensor) Shadow.extract(sensor)).setDeviceId(121);
    assertThat(sensor.getDeviceId()).isEqualTo(121);
  }

  @Test
  public void virtualSensor_getEvents() {
    VirtualSensor sensor = (VirtualSensor) createVirtualSensor();
    VirtualSensorEvent event =
        new VirtualSensorEvent.Builder(new float[] {12f})
            .setTimestampNanos(System.nanoTime())
            .build();

    sensor.sendEvent(event);

    assertThat(((ShadowVirtualSensor) Shadow.extract(sensor)).getSentEvents())
        .containsExactly(event);
  }

  @Test
  public void virtualSensor_clearEvents() {
    VirtualSensor sensor = (VirtualSensor) createVirtualSensor();
    ShadowVirtualSensor virtualSensor = (ShadowVirtualSensor) Shadow.extract(sensor);
    VirtualSensorEvent event =
        new VirtualSensorEvent.Builder(new float[] {12f})
            .setTimestampNanos(System.nanoTime())
            .build();

    sensor.sendEvent(event);
    assertThat(virtualSensor.getSentEvents()).containsExactly(event);
    virtualSensor.clearSentEvents();
    assertThat(virtualSensor.getSentEvents()).isEmpty();
  }

  private /* VirtualSensor */ Object createVirtualSensor() {
    return new VirtualSensor(1, Sensor.TYPE_ACCELEROMETER, "some name", null, null);
  }
}
