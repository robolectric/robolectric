package org.robolectric.annotation;

import android.app.Application;

class DefaultApplication extends Application {
  @SuppressWarnings("UnusedParameters")
  private DefaultApplication(DefaultApplication defaultApplication) {
    // don't make one of me!
  }
}
