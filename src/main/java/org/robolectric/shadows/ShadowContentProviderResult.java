package org.robolectric.shadows;

import android.content.ContentProviderResult;
import android.net.Uri;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Field;

@Implements(ContentProviderResult.class)
public class ShadowContentProviderResult {
  @RealObject ContentProviderResult realResult;

  public void __constructor__(Uri uri) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    Field field = realResult.getClass().getField("uri");
    field.setAccessible(true);
    field.set(realResult, uri);
  }

  public void __constructor__(int count) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    Field field = realResult.getClass().getField("count");
    field.setAccessible(true);
    field.set(realResult, count);
  }
}