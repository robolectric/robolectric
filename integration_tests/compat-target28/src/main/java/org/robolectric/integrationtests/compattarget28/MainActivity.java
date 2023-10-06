package org.robolectric.integrationtests.compattarget28;

import android.app.Activity;

public class MainActivity extends Activity {
  public enum CreationSource {
    DEFAULT_CONSTRUCTOR,
    CUSTOM_CONSTRUCTOR
  }

  public final CreationSource creationSource;

  @SuppressWarnings("unused")
  public MainActivity() {
    this(CreationSource.DEFAULT_CONSTRUCTOR);
  }

  public MainActivity(CreationSource creationSource) {
    this.creationSource = creationSource;
  }
}
