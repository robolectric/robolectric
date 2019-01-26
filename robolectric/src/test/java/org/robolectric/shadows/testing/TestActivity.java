package org.robolectric.shadows.testing;

import android.app.Activity;
import android.os.Bundle;
import org.robolectric.R;

/** Activity for tests. */
public class TestActivity extends Activity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.styles_button_layout);
  }
}
