package org.robolectric.testapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** Test service. */
public class TestService extends Service {
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
