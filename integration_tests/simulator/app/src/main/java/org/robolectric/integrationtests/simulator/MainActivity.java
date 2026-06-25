package org.robolectric.integrationtests.simulator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

/** An activity that is used for Robolectric simulator integration tests. Just has a blue screen. */
public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout layout = new LinearLayout(this);
    layout.setId(android.R.id.content);
    layout.setBackgroundColor(Color.BLUE);
    setContentView(layout);
  }
}
