package org.robolectric.android;

import android.view.View;

public class TestOnLongClickListener implements View.OnLongClickListener {
  public boolean clicked = false;

  @Override
  public boolean onLongClick(View view) {
    clicked = true;
    return true;
  }
}
