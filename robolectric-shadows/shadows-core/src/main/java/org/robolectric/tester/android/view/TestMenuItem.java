// Copyright 2015 Google Inc. All Rights Reserved.

package org.robolectric.tester.android.view;

import org.robolectric.fakes.RoboMenuItem;

/**
 * @deprecated Please use {@link org.robolectric.fakes.RoboMenuItem} instead.
 */
@Deprecated
public class TestMenuItem extends RoboMenuItem {

  public TestMenuItem() {
    super();
  }

  public TestMenuItem(int itemId) {
    super(itemId);
  }
}
