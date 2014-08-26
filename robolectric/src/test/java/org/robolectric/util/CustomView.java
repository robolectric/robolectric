package org.robolectric.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import org.robolectric.R;

public class CustomView extends LinearLayout {
  public static final String ROBOLECTRIC_RES_URI = "http://schemas.android.com/apk/res/org.robolectric";
  public static final String FAKE_URI = "http://example.com/fakens";

  public int attributeResourceValue;
  public int namespacedResourceValue;

  public CustomView(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflate(context, R.layout.inner_merge, this);
    attributeResourceValue = attrs.getAttributeResourceValue(ROBOLECTRIC_RES_URI, "message", -1);
    namespacedResourceValue = attrs.getAttributeResourceValue(FAKE_URI, "message", -1);
  }
}
