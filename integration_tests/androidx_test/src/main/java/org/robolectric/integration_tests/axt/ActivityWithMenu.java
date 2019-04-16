package org.robolectric.integration_tests.axt;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.robolectric.integration.axt.R;

/** Fixture activity for {@link EspressoTest} */
public class ActivityWithMenu extends Activity {

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
