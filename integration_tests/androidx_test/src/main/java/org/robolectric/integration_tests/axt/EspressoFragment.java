package org.robolectric.integration_tests.axt;

import android.os.Bundle;

public class EspressoFragment extends  androidx.fragment.app.Fragment {

  boolean buttonClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    setContentView(R.layout.espresso_activity);
//
//    Button button = findViewById(R.id.button);
//    button.setOnClickListener(
//        new OnClickListener() {
//          @Override
//          public void onClick(View view) {
//            buttonClicked = true;
//          }
//        });
  }
}
