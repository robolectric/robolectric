package org.robolectric.integrationtests.simulator;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.LinearLayout;

/**
 * An activity that is used for Robolectric simulator integration tests. The screen is blue if the
 * app's content provider was created at startup and the activity bound to the app's {@link
 * ColorService}, which supplies the blue, and red otherwise. Tapping it turns it green, and
 * pressing the space key turns it yellow.
 */
public class MainActivity extends Activity {
  private final ServiceConnection connection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          // Without real service binding, the binder is null.
          if (StartupProvider.created && service instanceof ColorService.ColorBinder) {
            layout.setBackgroundColor(((ColorService.ColorBinder) service).getColor());
          }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
      };
  private LinearLayout layout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    layout = new LinearLayout(this);
    layout.setId(android.R.id.content);
    layout.setBackgroundColor(Color.RED);
    layout.setOnClickListener(v -> layout.setBackgroundColor(Color.GREEN));
    setContentView(layout);
    bindService(new Intent(this, ColorService.class), connection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    unbindService(connection);
    super.onDestroy();
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_SPACE) {
      layout.setBackgroundColor(Color.YELLOW);
      return true;
    }
    return super.onKeyUp(keyCode, event);
  }
}
