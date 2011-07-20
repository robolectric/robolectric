package com.xtremelabs.robolectric.shadows;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(SensorManager.class)
public class ShadowSensorManager {

    private final Map<Integer, Sensor> sensorMap = new HashMap<Integer, Sensor>();

    /**
     * Provide a Sensor for the indicated sensor type.
     * @param sensorType from Sensor constants
     * @param sensor Sensor instance
     */
    public void addSensor(int sensorType, Sensor sensor) {
        sensorMap.put(sensorType, sensor);
    }

    @Implementation
    public Sensor getDefaultSensor(int type) {
        return sensorMap.get(type);
    }

}
