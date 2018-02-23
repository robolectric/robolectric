package org.robolectric.integration_tests.atsl;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import org.robolectric.integration.atsl.R;

/** Fixture activity for {@link EspressoTest} */
public class EspressoActivity extends Activity {

    boolean buttonClicked;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.espresso_activity);

      Button button = findViewById(R.id.button);
      button.setOnClickListener(
          view -> buttonClicked = true);
    }
  }
