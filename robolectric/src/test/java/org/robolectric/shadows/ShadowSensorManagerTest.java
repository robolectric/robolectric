package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.MemoryFile;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowSensorManagerTest {

  private SensorManager sensorManager;
  private ShadowSensorManager shadow;

  @Before
  public void setUp() {
    sensorManager =
        (SensorManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
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
  public void shouldSendSensorEventToSingleRegisteredListener() {
    TestSensorEventListener listener = new TestSensorEventListener();
    Sensor sensor = sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
    sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    SensorEvent event = shadow.createSensorEvent();
    // Confirm that the listener has received no events yet.
    assertThat(listener.getLatestSensorEvent()).isAbsent();

    shadow.sendSensorEventToListeners(event);

    assertThat(listener.getLatestSensorEvent().get()).isEqualTo(event);
  }

  @Test
  public void shouldSendSensorEventToMultipleRegisteredListeners() {
    TestSensorEventListener listener1 = new TestSensorEventListener();
    TestSensorEventListener listener2 = new TestSensorEventListener();
    Sensor sensor = sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
    sensorManager.registerListener(listener1, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    sensorManager.registerListener(listener2, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    SensorEvent event = shadow.createSensorEvent();

    shadow.sendSensorEventToListeners(event);

    assertThat(listener1.getLatestSensorEvent().get()).isEqualTo(event);
    assertThat(listener2.getLatestSensorEvent().get()).isEqualTo(event);
  }

  @Test
  public void shouldNotSendSensorEventIfNoRegisteredListeners() {
    // Create a listener but don't register it.
    TestSensorEventListener listener = new TestSensorEventListener();
    Sensor sensor = sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
    SensorEvent event = shadow.createSensorEvent();

    shadow.sendSensorEventToListeners(event);

    assertThat(listener.getLatestSensorEvent()).isAbsent();
  }

  @Test
  public void shouldCreateSensorEvent() {
    assertThat(shadow.createSensorEvent() instanceof SensorEvent).isTrue();
  }

  @Test
  public void shouldCreateSensorEventWithValueArray() {
    SensorEvent event = shadow.createSensorEvent(3);
    assertThat(event.values.length).isEqualTo(3);
  }

  @Test
  public void createSensorEvent_shouldThrowExceptionWhenValueLessThan1() {
    try {
      shadow.createSensorEvent(/* valueArraySize= */ 0);
      fail("Expected IllegalArgumentException not thrown");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void getSensor_shouldBeConfigurable() {
    Sensor sensor = ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER);
    shadowOf(sensorManager).addSensor(sensor);
    assertThat(sensor).isSameAs(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
  }

  @Test
  public void shouldReturnASensorList() throws Exception {
    assertThat(sensorManager.getSensorList(0)).isNotNull();
  }

  private static class TestSensorEventListener implements SensorEventListener {
    private Optional<SensorEvent> latestSensorEvent = Optional.absent();

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      latestSensorEvent = Optional.of(event);
    }

    public Optional<SensorEvent> getLatestSensorEvent() {
      return latestSensorEvent;
    }
  }
}
