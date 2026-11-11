package org.robolectric.integrationtests.simulator;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;

/** A service that the app binds to for the color of its screen, through a local binder. */
public class ColorService extends Service {

  /** The binder that {@link #onBind} returns. */
  public static final class ColorBinder extends Binder {
    public int getColor() {
      return Color.BLUE;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return new ColorBinder();
  }
}
