package org.robolectric.integrationtests.axt;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
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

    // Keep the IME out of this window. Espresso types by injecting key events, and an IME such as
    // Gboard takes those keys and hands the text back later, after Espresso has stopped waiting.
    // On a slow emulator that loses, repeats or reorders typed characters.
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

    setContentView(R.layout.espresso_activity);

    editText = findViewById(R.id.edit_text);

    button = findViewById(R.id.button);
    button.setOnClickListener(view -> buttonClicked = true);
  }
}
