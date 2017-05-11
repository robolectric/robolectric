package org.robolectric.shadows;

import android.hardware.*;
import android.os.MemoryFile;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.O;

@Implements(SensorManager.class)
public class ShadowSensorManager {
  public boolean forceListenersToFail = false;
  private final Map<Integer, Sensor> sensorMap = new HashMap<>();
  private final ArrayList<SensorEventListener> listeners = new ArrayList<>();

  @RealObject private SensorManager realObject;

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
    if (forceListenersToFail) {
      return false;
    }
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
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
    return ReflectionHelpers.callConstructor(SensorEvent.class);
  }

  @Implementation(minSdk = O)
  public SensorDirectChannel createDirectChannel(MemoryFile mem) {
    return ReflectionHelpers.callConstructor(SensorDirectChannel.class,
        ClassParameter.from(SensorManager.class, realObject),
        ClassParameter.from(int.class, 0),
        ClassParameter.from(int.class, SensorDirectChannel.TYPE_ASHMEM),
        ClassParameter.from(long.class, mem.length()));
  }
}
