package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowingTest {

  @Test
  public void testPrintlnWorks() throws Exception {
    Log.println(1, "tag", "msg");
  }

  @Test
  public void shouldDelegateToObjectToStringIfShadowHasNone() throws Exception {
    assertThat(new Toast(ApplicationProvider.getApplicationContext()).toString())
        .startsWith("android.widget.Toast@");
  }

  @Test
  public void shouldDelegateToObjectHashCodeIfShadowHasNone() throws Exception {
    assertFalse(new View(ApplicationProvider.getApplicationContext()).hashCode() == 0);
  }

  @Test
  public void shouldDelegateToObjectEqualsIfShadowHasNone() throws Exception {
    View view = new View(ApplicationProvider.getApplicationContext());
    assertEquals(view, view);
  }
}
