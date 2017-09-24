package org.robolectric.android;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowingTest {

  @Test
  public void testPrintlnWorks() throws Exception {
    Log.println(1, "tag", "msg");
  }

  @Test
  public void shouldDelegateToObjectToStringIfShadowHasNone() throws Exception {
    assertThat(new Toast(RuntimeEnvironment.application).toString()).startsWith("android.widget.Toast@");
  }

  @Test
  public void shouldDelegateToObjectHashCodeIfShadowHasNone() throws Exception {
    assertFalse(new View(RuntimeEnvironment.application).hashCode() == 0);
  }

  @Test
  public void shouldDelegateToObjectEqualsIfShadowHasNone() throws Exception {
    View view = new View(RuntimeEnvironment.application);
    assertEquals(view, view);
  }
}
