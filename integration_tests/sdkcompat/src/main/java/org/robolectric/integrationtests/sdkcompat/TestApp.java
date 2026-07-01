package org.robolectric.integrationtests.sdkcompat;

import android.app.Application;

public class TestApp extends Application {
  boolean instantiatedWithAppFactory = false;
}
