package org.robolectric.integrationtests.simulator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

/**
 * An activity that is used for Robolectric simulator integration tests. The screen is blue if the
 * app's content provider was created at startup, and red otherwise.
 */
public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout layout = new LinearLayout(this);
    layout.setId(android.R.id.content);
    layout.setBackgroundColor(StartupProvider.created ? Color.BLUE : Color.RED);
    setContentView(layout);
  }
}
