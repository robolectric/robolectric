package org.robolectric.integrationtests.axt;

import android.app.Activity;
import android.os.Bundle;
import org.robolectric.integration.axt.R;

/** Fixture activity for {@link EspressoTest} */
public class EspressoActivity extends Activity {

  boolean buttonClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.espresso_activity);

    findViewById(R.id.button).setOnClickListener(view -> buttonClicked = true);
  }
}
