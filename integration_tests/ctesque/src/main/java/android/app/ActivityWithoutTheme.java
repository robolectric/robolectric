package android.app;

import android.os.Bundle;

public class ActivityWithoutTheme extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(org.robolectric.R.layout.styles_button_layout);
  }
}
