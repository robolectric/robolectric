package org.robolectric.shadows;

import android.content.ContentProviderResult;
import android.net.Uri;
import java.lang.reflect.Field;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(ContentProviderResult.class)
public class ShadowContentProviderResult {
  @RealObject ContentProviderResult realResult;

  @Implementation
  protected void __constructor__(Uri uri)
      throws SecurityException, NoSuchFieldException, IllegalArgumentException,
          IllegalAccessException {
    Field field = realResult.getClass().getField("uri");
    field.setAccessible(true);
    field.set(realResult, uri);
  }

  @Implementation
  protected void __constructor__(int count)
      throws SecurityException, NoSuchFieldException, IllegalArgumentException,
          IllegalAccessException {
    Field field = realResult.getClass().getField("count");
    field.setAccessible(true);
    field.set(realResult, count);
  }
}