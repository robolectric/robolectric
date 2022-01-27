package org.robolectric.integrationtests.axt;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import org.robolectric.integration.axt.R;

/** Fixture activity for {@link EspressoTest} */
public class EspressoActivity extends Activity {

  EditText editText;
  Button button;
  boolean buttonClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.espresso_activity);

    editText = findViewById(R.id.edit_text);
    button = findViewById(R.id.button);
    button.setOnClickListener(view -> buttonClicked = true);
  }
}
