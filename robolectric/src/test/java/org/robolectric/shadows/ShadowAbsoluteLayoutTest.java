package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowAbsoluteLayoutTest {

  @Test
  public void getLayoutParams_shouldReturnAbsoluteLayoutParams() throws Exception {
    ViewGroup.LayoutParams layoutParams =
        new AbsoluteLayout(ApplicationProvider.getApplicationContext()) {
          @Override
          protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
            return super.generateDefaultLayoutParams();
          }
        }.generateDefaultLayoutParams();

    assertThat(layoutParams).isInstanceOf(AbsoluteLayout.LayoutParams.class);
  }
}
