package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotEquals;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowingTest {

  @Test
  public void testPrintlnWorks() {
    Log.println(1, "tag", "msg");
  }

  @Test
  public void shouldDelegateToObjectToStringIfShadowHasNone() {
    assertThat(new Toast(ApplicationProvider.getApplicationContext()).toString())
        .startsWith("android.widget.Toast@");
  }

  @Test
  public void shouldDelegateToObjectHashCodeIfShadowHasNone() {
    assertNotEquals(0, new View(ApplicationProvider.getApplicationContext()).hashCode());
  }

  @Test
  public void shouldDelegateToObjectEqualsIfShadowHasNone() {
    View view = new View(ApplicationProvider.getApplicationContext());
    new EqualsTester().addEqualityGroup(view).testEquals();
  }
}
