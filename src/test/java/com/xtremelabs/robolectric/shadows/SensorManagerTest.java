package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class SensorManagerTest {

	private SensorManager sensorManager;
	private ShadowSensorManager shadow;
	
	@Before
	public void setup() {
		sensorManager = (SensorManager) Robolectric.application.getSystemService(Context.SENSOR_SERVICE);
		shadow = shadowOf(sensorManager);
	}
	
	@After
	public void tearDown() {
		sensorManager = null;
		shadow = null;
	}
	
	@Test
	public void shouldReturnHasListenerAfterRegisteringListener() {
		SensorEventListener listener = registerListener();

		assertTrue(shadow.hasListener(listener));
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
		
		assertFalse(shadow.hasListener(listener));
	}
	
	@Test
	public void shouldReturnHasNoListenerByDefault() {
		SensorEventListener listener = new TestSensorEventListener();
		
		assertFalse(shadow.hasListener(listener));
	}
	
	@Test 
	public void shouldCreateSensorEvent() {
		assertTrue(shadow.createSensorEvent() instanceof SensorEvent);
	}
	
	private class TestSensorEventListener implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
		}
		
	}
}
