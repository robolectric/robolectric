package org.robolectric.shadows;

import android.graphics.Color;
import android.util.TypedValue;
import org.robolectric.res.ResType;
import org.robolectric.res.TypedResource;

public class Converter {
    public static Converter getConverter(ResType resType) {
        switch (resType) {
            case CHAR_SEQUENCE:
                return new FromCharSequence();
            case COLOR:
                return new FromColor();
            case DIMEN:
                return new FromDimen();
            case INTEGER:
                return new FromNumeric();
            case CHAR_SEQUENCE_ARRAY:
            case INTEGER_ARRAY:
                return new FromArray();
            case BOOLEAN:
                return new FromBoolean();
            default:
                throw new UnsupportedOperationException(resType.name());
        }
    }

    public CharSequence asCharSequence(TypedResource typedResource) {
        throw cantDo("asCharSequence");
    }

    public int asInt(TypedResource typedResource) {
        throw cantDo("asInt");
    }

    public TypedResource[] getItems(TypedResource typedResource) {
        throw cantDo("getItems");
    }

    public void fillTypedValue(TypedResource typedResource, TypedValue typedValue) {
        throw cantDo("fillTypedValue");
    }

    private UnsupportedOperationException cantDo(String operation) {
        return new UnsupportedOperationException(getClass().getName() + " doesn't support " + operation);
    }

    public static class FromColor extends Converter {

        @Override public void fillTypedValue(TypedResource typedResource, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_INT_COLOR_ARGB8;
            typedValue.data = Color.parseColor(typedResource.asString());
        }
    }

    public static class FromCharSequence extends Converter {
        @Override public CharSequence asCharSequence(TypedResource typedResource) {
            return typedResource.asString();
        }

        @Override public int asInt(TypedResource typedResource) {
            String rawValue = typedResource.asString();
            return convertInt(rawValue);
        }
    }

    public static class FromArray extends Converter {
        @Override public TypedResource[] getItems(TypedResource typedResource) {
            return (TypedResource[]) typedResource.getData();
        }
    }

    private static class FromNumeric extends Converter {
        @Override public void fillTypedValue(TypedResource typedResource, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_INT_HEX;
            typedValue.data = convertInt(typedResource.asString());
        }

        @Override public int asInt(TypedResource typedResource) {
            String rawValue = typedResource.asString();
            return convertInt(rawValue);
        }
    }

    private static class FromBoolean extends Converter {
        @Override public void fillTypedValue(TypedResource typedResource, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_INT_BOOLEAN;
            typedValue.data = convertBool(typedResource.asString()) ? 1 : 0;
        }

    }

    private static class FromDimen extends Converter {
        @Override public void fillTypedValue(TypedResource typedResource, TypedValue typedValue) {
            ResourceHelper.parseFloatAttribute(null, typedResource.asString(), typedValue, false);
        }
    }

    ///////////////////////

    private static int convertInt(String rawValue) {
        try {
            // Decode into long, because there are some large hex values in the android resource files
            // (e.g. config_notificationsBatteryLowARGB = 0xFFFF0000 in sdk 14).
            // Integer.decode() does not support large, i.e. negative values in hex numbers.
            // try parsing decimal number
            return (int) Long.parseLong(rawValue);
        } catch (NumberFormatException nfe) {
            // try parsing hex number
            try {
                return Long.decode(rawValue).intValue();
            } catch (NumberFormatException e) {
                throw new RuntimeException(rawValue + " is not an integer.", nfe);
            }
        }
    }

    private static boolean convertBool(String rawValue) {
        if ("true".equalsIgnoreCase(rawValue)) {
            return true;
        } else if ("false".equalsIgnoreCase(rawValue)) {
            return false;
        }

        int intValue = Integer.parseInt(rawValue);
        if (intValue == 0) {
            return false;
        }
        return true;
    }
}
