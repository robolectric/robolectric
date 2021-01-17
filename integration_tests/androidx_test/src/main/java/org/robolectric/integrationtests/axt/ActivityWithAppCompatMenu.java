package org.robolectric.integrationtests.axt;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.robolectric.integration.axt.R;

/** {@link EspressoWithMenuTest} fixture activity that uses appcompat menu's */
public class ActivityWithAppCompatMenu extends AppCompatActivity {

  boolean menuClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = new MenuInflater(this);

    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    menuClicked = true;
    return true;
  }
}
