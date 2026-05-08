package org.robolectric.integrationtests.axt;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import androidx.appcompat.app.AppCompatActivity;

/** Fixture activity for testing Espresso interaction with real PopupWindow. */
public class ActivityWithPopupWindow extends AppCompatActivity {

  public boolean popupClicked;
  private boolean popupShown = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View root = new View(this);
    setContentView(root);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus && !popupShown) {
      showPopup(getWindow().getDecorView());
      popupShown = true;
    }
  }

  @SuppressLint("SetTextI18n")
  private void showPopup(View anchor) {
    Button button = new Button(this);
    button.setText("popup_item");
    button.setOnClickListener(v -> popupClicked = true);

    PopupWindow popupWindow =
        new PopupWindow(
            button, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

    popupWindow.showAtLocation(anchor, android.view.Gravity.CENTER, 0, 0);
  }
}
