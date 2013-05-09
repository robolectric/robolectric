package org.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;
import org.robolectric.Robolectric;
import org.robolectric.internal.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceIndex;
import org.robolectric.util.Util;

import java.util.List;

import static org.fest.reflect.core.Reflection.constructor;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray {
    @RealObject private TypedArray realTypedArray;
    private CharSequence[] stringData;
    private int[] attrs;

    public static TypedArray create(Resources resources, List<Attribute> set, int[] attrs) {
        CharSequence[] stringData = new CharSequence[attrs.length];
        int[] data = new int[attrs.length * ShadowAssetManager.STYLE_NUM_ENTRIES];
        int[] indices = new int[attrs.length + 1];
        int nextIndex = 0;

        List<Integer> wantedAttrsList = Util.intArrayToList(attrs);

        for (int i = 0; i < attrs.length; i++) {
            int offset = i * ShadowAssetManager.STYLE_NUM_ENTRIES;

            int attr = attrs[i];
            ResourceIndex resourceIndex = shadowOf(resources).getResourceLoader().getResourceIndex();
            ResName attrName = resourceIndex.getResName(attr);
            if (attrName != null) {
                Attribute attribute = Attribute.find(set, attrName.getFullyQualifiedName());
                TypedValue typedValue = new TypedValue();
                Converter.convertAndFill(attribute, typedValue, shadowOf(resources).getResourceLoader(), shadowOf(resources.getAssets()).getQualifiers());

                if (attribute != null && !attribute.isNull()) {
                    //noinspection PointlessArithmeticExpression
                    data[offset + ShadowAssetManager.STYLE_TYPE] = typedValue.type;
                    data[offset + ShadowAssetManager.STYLE_DATA] = typedValue.type == TypedValue.TYPE_STRING ? i : typedValue.data;
                    data[offset + ShadowAssetManager.STYLE_ASSET_COOKIE] = typedValue.assetCookie;
                    data[offset + ShadowAssetManager.STYLE_RESOURCE_ID] = typedValue.resourceId;
                    data[offset + ShadowAssetManager.STYLE_CHANGING_CONFIGURATIONS] = typedValue.changingConfigurations;
                    data[offset + ShadowAssetManager.STYLE_DENSITY] = typedValue.density;
                    stringData[i] = typedValue.string;

                    indices[nextIndex + 1] = i;
                    nextIndex++;
                }
            }
        }

        indices[0] = nextIndex;

        TypedArray typedArray = constructor()
                .withParameterTypes(Resources.class, int[].class, int[].class, int.class)
                .in(TypedArray.class)
                .newInstance(resources, data, indices, nextIndex);
        TypedArray result = ShadowResources.inject(resources, typedArray);
        ShadowTypedArray shadowTypedArray = Robolectric.shadowOf(result);
        shadowTypedArray.stringData = stringData;
        shadowTypedArray.attrs = attrs;

        return result;
    }

    @HiddenApi @Implementation
    public CharSequence loadStringValueAt(int index) {
        return stringData[index / ShadowAssetManager.STYLE_NUM_ENTRIES];
    }
}
