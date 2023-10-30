package org.robolectric.shadows;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link SensorEventBuilder}. */
@RunWith(AndroidJUnit4.class)
public class SensorEventBuilderTest {
  private static final Sensor TEST_ACCELEROMETER_SENSOR =
      ShadowSensor.newInstance(TYPE_ACCELEROMETER);

  @Test
  public void createSensorEvent_sensorPropertyDeeplyEqualsItsSource() {
    Sensor testSensor = ShadowSensor.newInstance(TYPE_ACCELEROMETER);

    SensorEvent sensorEvent =
        SensorEventBuilder.newBuilder()
            .setValues(new float[] {1f, 2f, 3f})
            .setSensor(testSensor)
            .build();

    assertThat(sensorEvent.sensor).isEqualTo(testSensor);
  }

  @Test
  public void createSensorEvent_failsPreconditionsWithoutValuesSpecified() {
    assertThrows(
        IllegalArgumentException.class,
        () -> SensorEventBuilder.newBuilder().setSensor(TEST_ACCELEROMETER_SENSOR).build());
  }

  @Test
  public void createSensorEvent_failsPreconditionsWithoutSensorSpecified() {
    assertThrows(
        IllegalArgumentException.class,
        () -> SensorEventBuilder.newBuilder().setValues(new float[] {1f, 2f, 3f}).build());
  }
}
