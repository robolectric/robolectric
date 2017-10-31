package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.MemoryFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public class ShadowSensorManagerTest {

  private SensorManager sensorManager;
  private ShadowSensorManager shadow;

  @Before
  public void setup() {
    sensorManager = (SensorManager) RuntimeEnvironment.application.getSystemService(Context.SENSOR_SERVICE);
    shadow = shadowOf(sensorManager);
  }

  @After
  public void tearDown() {
    sensorManager = null;
    shadow = null;
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void createDirectChannel() throws Exception {
    SensorDirectChannel channel = (SensorDirectChannel) sensorManager.createDirectChannel(new MemoryFile("name", 10));
    assertThat(channel.isValid()).isTrue();

    channel.close();
    assertThat(channel.isValid()).isFalse();
  }

  @Test
  public void shouldReturnHasListenerAfterRegisteringListener() {
    SensorEventListener listener = registerListener();

    assertThat(shadow.hasListener(listener)).isTrue();
  }

  private SensorEventListener registerListener() {
    SensorEventListener listener = new TestSensorEventListener();
    Sensor sensor = sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
    sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    return listener;
  }

  @Test
  public void shouldReturnHasNoListenerAfterUnregisterListener() {
    SensorEventListener listener = registerListener();
    sensorManager.unregisterListener(listener, sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));

    assertThat(shadow.hasListener(listener)).isFalse();
  }

  @Test
  public void shouldReturnHasNoListenerAfterUnregisterListenerWithoutSpecificSensor() {
    SensorEventListener listener = registerListener();
    sensorManager.unregisterListener(listener);

    assertThat(shadow.hasListener(listener)).isFalse();
  }

  @Test
  public void shouldReturnHasNoListenerByDefault() {
    SensorEventListener listener = new TestSensorEventListener();

    assertThat(shadow.hasListener(listener)).isFalse();
  }

  @Test
  public void shouldCreateSensorEvent() {
    assertThat(shadow.createSensorEvent() instanceof SensorEvent).isTrue();
  }

  @Test
  public void getSensor_shouldBeConfigurable() {
    Sensor sensor = Shadow.newInstanceOf(Sensor.class);
    shadowOf(sensorManager).addSensor(Sensor.TYPE_ACCELEROMETER, sensor);
    assertThat(sensor).isSameAs(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
  }

  @Test
  public void shouldReturnASensorList() throws Exception {
    assertThat(sensorManager.getSensorList(0)).isNotNull();
  }

  private static class TestSensorEventListener implements SensorEventListener {

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

  }
}
