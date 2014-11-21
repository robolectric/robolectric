package org.robolectric.shadows;

import android.webkit.JsPromptResult;
import org.robolectric.annotation.Implements;

import static org.robolectric.util.ShadowThingy.newInstanceOf;

@Implements(JsPromptResult.class)
public class ShadowJsPromptResult extends ShadowJsResult{

  public static JsPromptResult newInstance() {
    return newInstanceOf(JsPromptResult.class);
  }
}
