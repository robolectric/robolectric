package org.robolectric.shadows;

import android.text.style.URLSpan;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(URLSpan.class)
public class ShadowURLSpan {
  private String url;

  public void __constructor__(String url) {
    this.url = url;
  }

  @Implementation
  public String getURL() {
    return url;
  }
}
