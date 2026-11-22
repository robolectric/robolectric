package org.robolectric.integrationtests.dynamicfeature.feature1;

import android.app.Activity;
import android.os.Bundle;

/** An Activity that lives in the dynamic feature module. */
public class FeatureActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.feature_activity);
  }
}
