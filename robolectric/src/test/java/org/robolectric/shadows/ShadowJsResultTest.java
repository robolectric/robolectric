package org.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.webkit.JsResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public class ShadowJsResultTest {

  @Test
  public void shouldRecordCanceled() throws Exception {
    JsResult jsResult = Shadow.newInstanceOf(JsResult.class);

    assertFalse(shadowOf(jsResult).wasCancelled());

    jsResult.cancel();
    assertTrue(shadowOf(jsResult).wasCancelled());

  }

}
