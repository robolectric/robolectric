package org.robolectric.integrationtests.axt;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.robolectric.integration.axt.R;

/** {@link EspressoWithMenuTest} fixture activity that uses appcompat menu's */
public class AppCompatActivityWithToolbarMenu extends AppCompatActivity {
  boolean menuClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.appcompat_activity_with_toolbar_menu);

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.inflateMenu(R.menu.menu);
    toolbar.setOnMenuItemClickListener(
        item -> {
          menuClicked = true;
          return true;
        });
  }
}
