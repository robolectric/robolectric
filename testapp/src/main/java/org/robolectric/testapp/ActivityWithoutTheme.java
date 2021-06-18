package org.robolectric.testapp;

import android.app.Activity;
import android.os.Bundle;

/** A test activity with no theme. */
public class ActivityWithoutTheme extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.styles_button_layout);
  }
}
