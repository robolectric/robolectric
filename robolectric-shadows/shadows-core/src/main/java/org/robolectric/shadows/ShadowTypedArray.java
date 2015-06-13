package org.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow for {@link android.content.res.TypedArray}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray {
  @RealObject private TypedArray realTypedArray;
  private CharSequence[] stringData;
  public String positionDescription;

  public static TypedArray create(Resources realResources, int[] attrs, int[] data, int[] indices, int len, CharSequence[] stringData) {
    TypedArray typedArray = ReflectionHelpers.callConstructor(TypedArray.class,
        ClassParameter.from(Resources.class, realResources),
        ClassParameter.from(int[].class, data),
        ClassParameter.from(int[].class, indices),
        ClassParameter.from(int.class, len));
    Shadows.shadowOf(typedArray).stringData = stringData;
    return typedArray;
  }

  @HiddenApi @Implementation
  public CharSequence loadStringValueAt(int index) {
    return stringData[index / ShadowAssetManager.STYLE_NUM_ENTRIES];
  }

  @Implementation
  public String getPositionDescription() {
    return positionDescription;
  }
}
