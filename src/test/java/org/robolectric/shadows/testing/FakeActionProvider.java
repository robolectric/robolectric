package org.robolectric.shadows.testing;

import android.content.Context;
import android.view.ActionProvider;
import android.view.View;

public class FakeActionProvider extends ActionProvider {
  public FakeActionProvider(Context context) {
    super(context);
  }

  @Override
  public View onCreateActionView() {
    return null;
  }
}