package org.robolectric.integrationtests.axt;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.robolectric.integration.axt.R;

/** Fixture activity for {@link EspressoWithSwitchCompat} */
public class ActivityWithSwitchCompat extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_switch_compat);
  }
}
