package org.robolectric.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Stub class that can be used for testing support-lib fragments.
 */
public class CustomFragment extends Fragment {

  private TextView textView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    textView = new TextView(inflater.getContext());
    textView.setText("CustomFragment text view");
    return textView;
  }
}
