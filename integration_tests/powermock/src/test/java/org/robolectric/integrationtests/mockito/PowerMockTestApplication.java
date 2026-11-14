package org.robolectric.integrationtests.mockito;

import android.app.Application;

public class PowerMockTestApplication extends Application {
  private static PowerMockTestApplication instance;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
  }

  public static PowerMockTestApplication getInstance() {
    return instance;
  }
}
