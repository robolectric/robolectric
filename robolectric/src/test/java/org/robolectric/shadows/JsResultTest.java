package org.robolectric.shadows;

import android.webkit.JsResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class JsResultTest {

  @Test
  public void shouldRecordCanceled() throws Exception {
    JsResult jsResult = Robolectric.newInstanceOf(JsResult.class);

    assertFalse(shadowOf(jsResult).wasCancelled());

    jsResult.cancel();
    assertTrue(shadowOf(jsResult).wasCancelled());

  }

}
