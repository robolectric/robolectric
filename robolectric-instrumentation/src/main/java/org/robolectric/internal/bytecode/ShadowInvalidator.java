package org.robolectric.internal.bytecode;

import java.lang.invoke.SwitchPoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShadowInvalidator {
  private static final SwitchPoint DUMMY = new SwitchPoint();

  static {
    SwitchPoint.invalidateAll(new SwitchPoint[] { DUMMY });
  }

  private Map<String, SwitchPoint> switchPoints;

  public ShadowInvalidator() {
    this.switchPoints = new HashMap<>();
  }

  public SwitchPoint getSwitchPoint(Class<?> caller) {
    return getSwitchPoint(caller.getName());
  }

  public synchronized SwitchPoint getSwitchPoint(String className) {
    SwitchPoint switchPoint = switchPoints.get(className);
    if (switchPoint == null) switchPoints.put(className, switchPoint = new SwitchPoint());
    return switchPoint;
  }

  public synchronized void invalidateClasses(Collection<String> classNames) {
    if (classNames.isEmpty()) return;
    SwitchPoint[] points = new SwitchPoint[classNames.size()];
    int i = 0;
    for (String className : classNames) {
      SwitchPoint switchPoint = switchPoints.put(className, null);
      if (switchPoint == null) switchPoint = DUMMY;
      points[i++] = switchPoint;
    }

    SwitchPoint.invalidateAll(points);
  }
}
