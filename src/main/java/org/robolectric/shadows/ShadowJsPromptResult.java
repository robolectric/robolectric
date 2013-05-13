package org.robolectric.shadows;

import android.webkit.JsPromptResult;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

@Implements(JsPromptResult.class)
public class ShadowJsPromptResult extends ShadowJsResult{

  public static JsPromptResult newInstance() {
    return Robolectric.newInstanceOf(JsPromptResult.class);
  }
}
