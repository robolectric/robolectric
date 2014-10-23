package org.robolectric.shadows;

import android.widget.LinearLayout;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Field;


@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
  @RealObject LinearLayout realObject;

  public int getGravity() {
    try {
      Field mGravity = LinearLayout.class.getDeclaredField("mGravity");
      mGravity.setAccessible(true);
      return mGravity.getInt(realObject);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
