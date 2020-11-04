package org.robolectric.integrationtests.axt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import org.robolectric.integration.axt.R;

/** Fixture activity for {@link EspressoTest} */
public class EspressoActivity extends Activity {

  boolean buttonClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.espresso_activity);

    Button button = findViewById(R.id.button);
    button.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            buttonClicked = true;
          }
        });
  }
}
