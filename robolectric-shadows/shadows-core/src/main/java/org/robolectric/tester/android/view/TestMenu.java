// Copyright 2015 Google Inc. All Rights Reserved.

package org.robolectric.tester.android.view;

import android.content.Context;
import org.robolectric.fakes.RoboMenu;

/**
 * @deprecated Please use {@link org.robolectric.fakes.RoboMenu} instead
  */
@Deprecated
public class TestMenu extends RoboMenu {

  public TestMenu() {
    super();
  }

  public TestMenu(Context context) {
    super(context);
  }
}
