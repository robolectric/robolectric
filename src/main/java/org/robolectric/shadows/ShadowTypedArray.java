package org.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.HiddenApi;

import static org.fest.reflect.core.Reflection.constructor;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray {
  @RealObject private TypedArray realTypedArray;
  private CharSequence[] stringData;
  public String positionDescription;

  public static TypedArray create(Resources realResources, int[] attrs, int[] data, int[] indices, int len, CharSequence[] stringData) {
    TypedArray typedArray = constructor()
        .withParameterTypes(Resources.class, int[].class, int[].class, int.class)
        .in(TypedArray.class)
        .newInstance(realResources, data, indices, len);
    shadowOf(typedArray).stringData = stringData;
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
