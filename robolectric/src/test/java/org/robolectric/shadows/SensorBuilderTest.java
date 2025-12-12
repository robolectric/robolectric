package org.robolectric.shadows;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static com.google.common.truth.Truth.assertThat;

import android.hardware.Sensor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link SensorBuilder}. */
@RunWith(AndroidJUnit4.class)
public class SensorBuilderTest {

  @Test
  public void createSensor_defaults() {
    Sensor testSensor = SensorBuilder.newBuilder().build();
    assertThat(testSensor.getType()).isEqualTo(0);
    assertThat(testSensor.isWakeUpSensor()).isEqualTo(false);
    assertThat(testSensor.getMaximumRange()).isEqualTo(0f);
    assertThat(testSensor.getMinDelay()).isEqualTo(0);
    assertThat(testSensor.getMaxDelay()).isEqualTo(0);
    assertThat(testSensor.getName()).isEqualTo(null);
    assertThat(testSensor.getFifoMaxEventCount()).isEqualTo(0);
    assertThat(testSensor.getFifoReservedEventCount()).isEqualTo(0);
  }

  @Test
  public void createSensor() {
    Sensor testSensor =
        SensorBuilder.newBuilder()
            .setType(TYPE_ACCELEROMETER)
            .setWakeUpFlag(true)
            .setMaximumRange(10f)
            .setMinDelay(100)
            .setMaxDelay(200)
            .setName("test_sensor")
            .setFifoMaxEventCount(10)
            .setFifoReservedEventCount(5)
            .build();

    assertThat(testSensor.getType()).isEqualTo(TYPE_ACCELEROMETER);
    assertThat(testSensor.isWakeUpSensor()).isEqualTo(true);
    assertThat(testSensor.getMaximumRange()).isEqualTo(10f);
    assertThat(testSensor.getMinDelay()).isEqualTo(100);
    assertThat(testSensor.getMaxDelay()).isEqualTo(200);
    assertThat(testSensor.getName()).isEqualTo("test_sensor");
    assertThat(testSensor.getFifoMaxEventCount()).isEqualTo(10);
    assertThat(testSensor.getFifoReservedEventCount()).isEqualTo(5);
  }

  @Test
  public void createSensor_falseWakeUpFlag() {
    Sensor testSensor = SensorBuilder.newBuilder().setWakeUpFlag(true).setWakeUpFlag(false).build();
    assertThat(testSensor.isWakeUpSensor()).isEqualTo(false);
  }
}
