package com.xtremelabs.robolectric.shadows;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertSame;

@RunWith(WithTestDefaultsRunner.class)
public class SensorManagerTest {

    private SensorManager sensorManager;

    @Before
    public void setUp() throws Exception {
        sensorManager = Robolectric.newInstanceOf(SensorManager.class);
    }

    @Test
    public void getSensor_shouldBeConfigurable() {
        Sensor sensor = Robolectric.newInstanceOf(Sensor.class);
        shadowOf(sensorManager).addSensor(Sensor.TYPE_ACCELEROMETER, sensor);
        assertSame(sensor, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }
}
