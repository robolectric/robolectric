package org.robolectric.shadows;

import android.webkit.JsResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowJsResultTest {

  @Test
  public void shouldRecordCanceled() throws Exception {
    JsResult jsResult = Shadow.newInstanceOf(JsResult.class);

    assertFalse(shadowOf(jsResult).wasCancelled());

    jsResult.cancel();
    assertTrue(shadowOf(jsResult).wasCancelled());

  }

}
