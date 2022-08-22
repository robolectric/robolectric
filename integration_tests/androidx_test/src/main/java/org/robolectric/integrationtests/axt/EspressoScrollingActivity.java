package org.robolectric.integrationtests.axt;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import org.robolectric.integration.axt.R;

/** Activity that inflates a {@link android.widget.ScrollView} . */
public class EspressoScrollingActivity extends Activity {
  Button button;
  boolean buttonClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.espresso_scrolling_activity);

    button = findViewById(R.id.button);
    button.setOnClickListener(view -> buttonClicked = true);
  }
}
