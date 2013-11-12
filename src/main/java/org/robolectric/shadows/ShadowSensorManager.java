package org.robolectric.shadows;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Implements(SensorManager.class)
public class ShadowSensorManager {

  private ArrayList<SensorEventListener> listeners = new ArrayList<SensorEventListener>();

  public boolean forceListenersToFail = false;

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

  @Implementation
  public boolean registerListener(SensorEventListener listener, Sensor sensor, int rate) {

    if(forceListenersToFail)
      return false;

    if(!listeners.contains(listener))
      listeners.add(listener);

    return true;
  }

  @Implementation
  public void unregisterListener(SensorEventListener listener, Sensor sensor) {
    listeners.remove(listener);
  }

  @Implementation
  public void unregisterListener(SensorEventListener listener) {
    listeners.remove(listener);
  }

  public boolean hasListener(SensorEventListener listener) {
    return listeners.contains(listener);
  }

  public SensorEvent createSensorEvent() {
    return Robolectric.newInstanceOf(SensorEvent.class);
  }
}
