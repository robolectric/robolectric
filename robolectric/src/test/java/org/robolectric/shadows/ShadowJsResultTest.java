package org.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.webkit.JsResult;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowJsResultTest {

  @Test
  public void shouldRecordCanceled() throws Exception {
    JsResult jsResult = Shadow.newInstanceOf(JsResult.class);

    assertFalse(shadowOf(jsResult).wasCancelled());

    jsResult.cancel();
    assertTrue(shadowOf(jsResult).wasCancelled());

  }

}
