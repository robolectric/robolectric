package org.robolectric.integrationtests.androidx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import org.jspecify.annotations.Nullable;

public class ConstraintLayoutActivity extends Activity {
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.constraint_layout_activity);
  }

  public void showGroup() {
    findViewById(R.id.group).setVisibility(View.VISIBLE);
  }
}
