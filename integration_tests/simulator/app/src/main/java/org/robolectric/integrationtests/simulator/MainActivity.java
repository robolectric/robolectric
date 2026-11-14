package org.robolectric.integrationtests.simulator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.LinearLayout;

/**
 * An activity that is used for Robolectric simulator integration tests. The screen is blue if the
 * app's content provider was created at startup, and red otherwise. Tapping it turns it green, and
 * pressing the space key turns it yellow.
 */
public class MainActivity extends Activity {
  private LinearLayout layout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    layout = new LinearLayout(this);
    layout.setId(android.R.id.content);
    layout.setBackgroundColor(StartupProvider.created ? Color.BLUE : Color.RED);
    layout.setOnClickListener(v -> layout.setBackgroundColor(Color.GREEN));
    setContentView(layout);
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
