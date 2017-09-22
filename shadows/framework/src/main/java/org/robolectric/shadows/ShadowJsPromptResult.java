package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.newInstanceOf;

import android.webkit.JsPromptResult;
import org.robolectric.annotation.Implements;

@Implements(JsPromptResult.class)
public class ShadowJsPromptResult extends ShadowJsResult{

  public static JsPromptResult newInstance() {
    return newInstanceOf(JsPromptResult.class);
  }
}
