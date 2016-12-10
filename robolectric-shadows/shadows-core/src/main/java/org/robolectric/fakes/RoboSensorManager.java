package org.robolectric.fakes;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Robolectric implementation of {@link android.hardware.SensorManager}.
 */
public class RoboSensorManager extends SensorManager {

  @Override
  public List<Sensor> getSensorList(int type) {
    return new ArrayList<Sensor>();
  }

  @Override
  protected void unregisterListenerImpl(SensorEventListener listener, Sensor sensor) {
  }

  @Override
  protected boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delayUs, Handler handler, int maxBatchReportLatencyUs, int reservedFlags) {
    return false;
  }

  @Override
  protected boolean flushImpl(SensorEventListener listener) {
    return false;
  }

  protected boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delayUs, Handler handler) {
    return false;
  }

  @Override
  protected boolean requestTriggerSensorImpl(TriggerEventListener listener, Sensor sensor) {
    return false;
  }

  @Override
  protected boolean cancelTriggerSensorImpl(TriggerEventListener listener, Sensor sensor, boolean disable) {
    return false;
  }

  @Override
  protected List<Sensor> getFullSensorList() {
    return null;
  }

  @Override
  protected boolean initDataInjectionImpl(boolean enable) {
    return false;
  }

  @Override
  protected boolean injectSensorDataImpl(Sensor sensor, float[] values, int accuracy, long timestamp) {
    return false;
  }

  @Override
  protected void registerDynamicSensorCallbackImpl(
            DynamicSensorCallback callback, Handler handler) {

  }

  @Override
  protected void unregisterDynamicSensorCallbackImpl(
          DynamicSensorCallback callback) {

  }

  @Override
  protected List<Sensor> getFullDynamicSensorList() {
    return null;
  }
}
