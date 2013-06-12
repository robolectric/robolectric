package android.hardware;

import java.util.ArrayList;
import java.util.List;

public class TestSensorManager extends SensorManager {

  @Override
  public List<Sensor> getSensorList(int type) {
    return new ArrayList<Sensor>();
  }

  public TestSensorManager() {
  }
}
