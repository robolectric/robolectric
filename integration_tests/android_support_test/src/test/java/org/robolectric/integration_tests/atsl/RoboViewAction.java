package org.robolectric.integration_tests.atsl;

import static android.support.test.espresso.action.ViewActions.click;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import org.hamcrest.Matcher;

/** Created by brettchabot on 2/23/18. */
class RoboViewAction implements ViewAction {

  static ViewAction roboClick() {
    return new RoboViewAction(click());
  }

  private final ViewAction viewActionDelegate;

  RoboViewAction(ViewAction viewActionDelegate) {
    this.viewActionDelegate = viewActionDelegate;
  }

  @Override
  public Matcher<View> getConstraints() {
    return viewActionDelegate.getConstraints();
  }

  @Override
  public String getDescription() {
    return viewActionDelegate.getDescription();
  }

  @Override
  public void perform(UiController uiController, View view) {
    viewActionDelegate.perform(new RoboUiController(), view);
  }
}
