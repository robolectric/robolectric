package android.app;

import android.os.Bundle;
import org.robolectric.annotation.internal.DoNotInstrument;

@DoNotInstrument
public class ActivityWithAnotherTheme extends Activity {

  public static Integer setThemeBeforeContentView = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (setThemeBeforeContentView != null) {
      setTheme(setThemeBeforeContentView);
    }

    setContentView(org.robolectric.R.layout.styles_button_layout);
  }
}
