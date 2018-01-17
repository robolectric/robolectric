package org.robolectric.android.fakes;

import android.support.test.runner.MonitoringInstrumentation;

public class RoboInstrumentation extends MonitoringInstrumentation {

  @Override
  protected void specifyDexMakerCacheProperty() {
    // ignore, unnecessary for robolectric
  }
}
