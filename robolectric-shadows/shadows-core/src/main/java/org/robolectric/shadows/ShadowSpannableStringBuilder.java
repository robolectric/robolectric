package org.robolectric.shadows;

import android.text.SpannableStringBuilder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow for {@link android.text.SpannableStringBuilder}.
 */
@Implements(SpannableStringBuilder.class)
public class ShadowSpannableStringBuilder {
  @RealObject SpannableStringBuilder realSpannableStringBuilder;

  @Implementation @Override public boolean equals(Object obj) {
    return obj != null && realSpannableStringBuilder.toString().equals(obj.toString());
  }

  @Implementation @Override public int hashCode() {
    return realSpannableStringBuilder.toString().hashCode();
  }
}
