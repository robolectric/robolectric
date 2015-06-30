package org.robolectric.shadows;

import static android.content.Intent.FILL_IN_ACTION;
import static android.content.Intent.FILL_IN_CATEGORIES;
import static android.content.Intent.FILL_IN_COMPONENT;
import static android.content.Intent.FILL_IN_DATA;
import static android.content.Intent.FILL_IN_PACKAGE;
import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.Shadow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Shadow for {@link android.content.Intent}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Intent.class)
public class ShadowIntent {
  @RealObject private Intent realIntent;

  /**
   * Non-Android accessor that returns the {@code Class} object set by
   * {@link #setClass(android.content.Context, Class)}
   *
   * @return the {@code Class} object set by
   *         {@link #setClass(android.content.Context, Class)}
   */
  public Class<?> getIntentClass() {
    try {
      return Class.forName(realIntent.getComponent().getClassName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
