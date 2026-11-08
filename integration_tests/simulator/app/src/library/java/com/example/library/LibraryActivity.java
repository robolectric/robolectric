package com.example.library;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

/**
 * Stands in for a launcher activity that a library adds to an app, such as LeakCanary's. It sorts
 * before the app's own launcher by name, and the simulator should not launch it.
 */
public class LibraryActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout layout = new LinearLayout(this);
    layout.setBackgroundColor(Color.RED);
    setContentView(layout);
  }
}
