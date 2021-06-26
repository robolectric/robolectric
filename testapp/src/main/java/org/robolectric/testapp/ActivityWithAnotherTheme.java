package org.robolectric.testapp;

import android.app.Activity;
import android.os.Bundle;

/** A test activity that can customize the theme. */
public class ActivityWithAnotherTheme extends Activity {

  public static Integer setThemeBeforeContentView = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (setThemeBeforeContentView != null) {
      setTheme(setThemeBeforeContentView);
    }

    setContentView(R.layout.styles_button_layout);
  }
}
