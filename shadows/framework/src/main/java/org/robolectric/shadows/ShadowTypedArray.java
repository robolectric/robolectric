package org.robolectric.shadows;

import static org.robolectric.res.android.AttributeResolution.STYLE_ASSET_COOKIE;
import static org.robolectric.res.android.AttributeResolution.STYLE_CHANGING_CONFIGURATIONS;
import static org.robolectric.res.android.AttributeResolution.STYLE_DATA;
import static org.robolectric.res.android.AttributeResolution.STYLE_DENSITY;
import static org.robolectric.res.android.AttributeResolution.STYLE_NUM_ENTRIES;
import static org.robolectric.res.android.AttributeResolution.STYLE_RESOURCE_ID;
import static org.robolectric.res.android.AttributeResolution.STYLE_TYPE;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.StyleableRes;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray {
  @RealObject private TypedArray realTypedArray;
  private CharSequence[] stringData;
  public String positionDescription;

  public static TypedArray create(Resources realResources, int[] attrs, int[] data, int[] indices, int len, CharSequence[] stringData) {
    TypedArray typedArray;
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
      typedArray = ReflectionHelpers.callConstructor(TypedArray.class,
          ClassParameter.from(Resources.class, realResources));
      ReflectionHelpers.setField(typedArray, "mData", data);
      ReflectionHelpers.setField(typedArray, "mLength", len);
      ReflectionHelpers.setField(typedArray, "mIndices", indices);
    } else {
      typedArray = ReflectionHelpers.callConstructor(TypedArray.class,
          ClassParameter.from(Resources.class, realResources),
          ClassParameter.from(int[].class, data),
          ClassParameter.from(int[].class, indices),
          ClassParameter.from(int.class, len));
    }

    ShadowTypedArray shadowTypedArray = Shadow.extract(typedArray);
    shadowTypedArray.stringData = stringData;
    return typedArray;
  }

  @HiddenApi @Implementation
  protected CharSequence loadStringValueAt(int index) {
    if (ShadowAssetManager.useLegacy()) {
      return stringData[index / STYLE_NUM_ENTRIES];
    } else {
      return directlyOn(realTypedArray, TypedArray.class, "loadStringValueAt",
          new ClassParameter(int.class, index));
    }
  }

  @Implementation
  protected String getNonResourceString(@StyleableRes int index) {
    return directlyOn(realTypedArray, TypedArray.class).getString(index);
  }

  @Implementation
  protected String getNonConfigurationString(@StyleableRes int index, int allowedChangingConfigs) {
    return directlyOn(realTypedArray, TypedArray.class).getString(index);
  }

  @Implementation
  protected String getPositionDescription() {
    if (ShadowAssetManager.useLegacy()) {
      return positionDescription;
    } else {
      return directlyOn(realTypedArray, TypedArray.class, "getPositionDescription");
    }
  }

  public static void dump(TypedArray typedArray) {
    int[] data = ReflectionHelpers.getField(typedArray, "mData");

    StringBuilder result = new StringBuilder();
    for (int index = 0; index < data.length; index+= STYLE_NUM_ENTRIES) {
      final int type = data[index+STYLE_TYPE];
      result.append("Index: ").append(index / STYLE_NUM_ENTRIES).append(System.lineSeparator());
      result.append(Strings.padEnd("Type: ", 25, ' ')).append(TYPE_MAP.get(type)).append(System.lineSeparator());
      if (type != TypedValue.TYPE_NULL) {
        result.append(Strings.padEnd("Style data: ", 25, ' ')).append(data[index+ STYLE_DATA]).append(System.lineSeparator());
        result.append(Strings.padEnd("Asset cookie ", 25, ' ')).append(data[index+STYLE_ASSET_COOKIE]).append(System.lineSeparator());
        result.append(Strings.padEnd("Style resourceId: ", 25, ' ')).append(data[index+ STYLE_RESOURCE_ID]).append(System.lineSeparator());
        result.append(Strings.padEnd("Changing configurations ", 25, ' ')).append(data[index+STYLE_CHANGING_CONFIGURATIONS]).append(System.lineSeparator());
        result.append(Strings.padEnd("Style density: ", 25, ' ')).append(data[index+STYLE_DENSITY]).append(System.lineSeparator());
        if (type == TypedValue.TYPE_STRING) {
          ShadowTypedArray shadowTypedArray = Shadow.extract(typedArray);
          result.append(Strings.padEnd("Style value: ", 25, ' ')).append(shadowTypedArray.loadStringValueAt(index)).append(System.lineSeparator());
        }
      }
      result.append(System.lineSeparator());
    }
    System.out.println(result.toString());
  }

  private static final ImmutableMap<Integer, String> TYPE_MAP = ImmutableMap.<Integer, String>builder()
          .put(TypedValue.TYPE_NULL, "TYPE_NULL")
          .put(TypedValue.TYPE_REFERENCE, "TYPE_REFERENCE")
          .put(TypedValue.TYPE_ATTRIBUTE, "TYPE_ATTRIBUTE")
          .put(TypedValue.TYPE_STRING, "TYPE_STRING")
          .put(TypedValue.TYPE_FLOAT, "TYPE_FLOAT")
          .put(TypedValue.TYPE_DIMENSION, "TYPE_DIMENSION")
          .put(TypedValue.TYPE_FRACTION, "TYPE_FRACTION")
          .put(TypedValue.TYPE_INT_DEC, "TYPE_INT_DEC")
          .put(TypedValue.TYPE_INT_HEX, "TYPE_INT_HEX")
          .put(TypedValue.TYPE_INT_BOOLEAN, "TYPE_INT_BOOLEAN")
          .put(TypedValue.TYPE_INT_COLOR_ARGB8, "TYPE_INT_COLOR_ARGB8")
          .put(TypedValue.TYPE_INT_COLOR_RGB8, "TYPE_INT_COLOR_RGB8")
          .put(TypedValue.TYPE_INT_COLOR_ARGB4, "TYPE_INT_COLOR_ARGB4")
          .put(TypedValue.TYPE_INT_COLOR_RGB4, "TYPE_INT_COLOR_RGB4")
          .build();

}
