package org.robolectric.shadows;

import android.webkit.JsPromptResult;
import org.robolectric.annotation.Implements;

import static org.robolectric.internal.Shadow.newInstanceOf;

/**
 * Shadow for {@link android.webkit.JsPromptResult}.
 */
@Implements(JsPromptResult.class)
public class ShadowJsPromptResult extends ShadowJsResult{

  public static JsPromptResult newInstance() {
    return newInstanceOf(JsPromptResult.class);
  }
}
