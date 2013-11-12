package android.hardware;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class TestSensorManager extends SensorManager {

  @Override
  public List<Sensor> getSensorList(int type) {
    return new ArrayList<Sensor>();
  }

  @Override
  protected void unregisterListenerImpl(SensorEventListener listener, Sensor sensor) {

  }

  @Override
  protected boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delay, Handler handler) {
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

  public TestSensorManager() {
  }

  @Override
  protected List<Sensor> getFullSensorList() {
    return null;
  }
}
