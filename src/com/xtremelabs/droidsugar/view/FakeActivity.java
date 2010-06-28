package com.xtremelabs.droidsugar.view;

import android.content.Intent;
import android.view.View;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeActivity {
  static public ViewLoader viewLoader;

  private Intent intent;
  private View contentView;

  public void setIntent(Intent intent) {
    this.intent = intent;
  }

  public Intent getIntent() {
    return intent;
  }

  public void setContentView(int layoutResID) {
    contentView = viewLoader.inflateView(null, layoutResID);
  }

  public View findViewById(int id) {
    if (contentView != null) {
      return contentView.findViewById(id);
    } else {
      return null;
    }
  }
}
