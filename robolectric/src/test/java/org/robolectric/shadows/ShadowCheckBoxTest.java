package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.widget.CheckBox;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowCheckBoxTest {
  @Test
  public void testWorks() throws Exception {
    CheckBox checkBox = new CheckBox(ApplicationProvider.getApplicationContext());
    assertThat(checkBox.isChecked()).isFalse();

    checkBox.setChecked(true);
    assertThat(checkBox.isChecked()).isTrue();

    checkBox.toggle();
    assertThat(checkBox.isChecked()).isFalse();

    checkBox.performClick();  // Used to support performClick(), but Android doesn't. Sigh.
//        assertThat(checkBox.isChecked()).isFalse();
  }
}
