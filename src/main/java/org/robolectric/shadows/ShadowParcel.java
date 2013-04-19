package org.robolectric.shadows;

import static org.fest.reflect.core.Reflection.constructor;
import static org.robolectric.Robolectric.shadowOf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

@Implements(Parcel.class)
@SuppressWarnings("unchecked")
public class ShadowParcel {

    private static final int BINDER_SIZE_BYTES = 20;
    private static final int VAL_NULL = -1;
    private static final int VAL_STRING = 0;
    private static final int VAL_INTEGER = 1;
    private static final int VAL_MAP = 2;
    private static final int VAL_BUNDLE = 3;
    private static final int VAL_PARCELABLE = 4;
    private static final int VAL_SHORT = 5;
    private static final int VAL_LONG = 6;
    private static final int VAL_FLOAT = 7;
    private static final int VAL_DOUBLE = 8;
    private static final int VAL_BOOLEAN = 9;
    private static final int VAL_CHARSEQUENCE = 10;
    private static final int VAL_BYTEARRAY = 13;
    private static final int VAL_STRINGARRAY = 14;
    private static final int VAL_OBJECTARRAY = 17;
    private static final int VAL_INTARRAY = 18;
    private static final int VAL_LONGARRAY = 19;
    private static final int VAL_BYTE = 20;
    private static final int VAL_BOOLEANARRAY = 23;

    private final ArrayList<Pair<Integer, ?>> parcelData = new ArrayList<Pair<Integer, ?>>();
    private int index = 0;

    @RealObject
    private Parcel realParcel;

    @Implementation
    public static Parcel obtain() {
        return Robolectric.newInstanceOf(Parcel.class);
    }

    @Implementation
    public int dataAvail() {
        return dataSize() - dataPosition();
    }

    @Implementation
    public int dataPosition() {
        return calculateSizeToIndex(index);
    }

    @Implementation
    public int dataSize() {
        return calculateSizeToIndex(parcelData.size());
    }

    @Implementation
    public int dataCapacity() {
        return dataSize();
    }

    @Implementation
    public void setDataPosition(int pos) {
        index = calculateIndexFromSizePosition(pos);
    }

    private int calculateSizeToIndex(int index) {
        int size = 0;
        for (int i = 0; i < index; i++) {
            size += parcelData.get(i).first;
        }
        return size;
    }

    private int calculateIndexFromSizePosition(int pos) {
        int size = 0;
        for (int i = 0; i < parcelData.size(); i++) {
            if (size >= pos) {
                return i;
            }
            size += parcelData.get(i).first;
        }
        return parcelData.size();
    }

    @Implementation
    public void appendFrom(Parcel parcel, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > parcel.dataSize()) {
            throw new IllegalArgumentException();
        }
        
        int otherParcelStartIndex = shadowOf(parcel).calculateIndexFromSizePosition(offset);
        int otherParcelEndIndex = shadowOf(parcel).calculateIndexFromSizePosition(offset + length);
        
        for (int i = otherParcelStartIndex; i < otherParcelEndIndex; i++) {
            addValueToList(shadowOf(parcel).parcelData.get(i));
        }
    }
    
    @Implementation
    public void writeStrongBinder(IBinder binder) {
        if (binder == null) {
            writeInt(-1);
        } else {
            writeInt(1);
            addValueToList(Pair.create(BINDER_SIZE_BYTES, binder));
        }
    }
    
    @Implementation
    public IBinder readStrongBinder() {
        int N = readInt();
        if (N < 0) {
            return null;
        } else {
            return readValueFromList(null);
        }
    }
    
    @Implementation
    public void writeString(String str) {
        if (str == null) {
            writeInt(-1);
        } else {
            writeInt(str.length());
            addValueToList(Pair.create(str.length(), str));
        }
    }

    @Implementation
    public String readString() {
        int N = readInt();
        if (N < 0) {
            return null;
        } else {
            return readValueFromList(null);
        }
    }

    @Implementation
    public void writeInt(int i) {
        addValueToList(Pair.create(Integer.SIZE / 8, i));
    }

    @Implementation
    public int readInt() {
        return readValueFromList(0);
    }

    @Implementation
    public void writeLong(long i) {
        addValueToList(Pair.create(Long.SIZE / 8, i));
    }

    @Implementation
    public long readLong() {
        return readValueFromList((long) 0);
    }

    @Implementation
    public void writeFloat(float f) {
        addValueToList(Pair.create(Float.SIZE / 8, f));
    }

    @Implementation
    public float readFloat() {
        return readValueFromList((float) 0);
    }

    @Implementation
    public void writeDouble(double f) {
        addValueToList(Pair.create(Double.SIZE / 8, f));
    }

    @Implementation
    public double readDouble() {
        return readValueFromList((double) 0);
    }

    public void writeBoolean(boolean b) {
        addValueToList(Pair.create(1, b));
    }

    public boolean readBoolean() {
        return readValueFromList(false);
    }

    public void writeChar(char c) {
        addValueToList(Pair.create(Character.SIZE / 8, c));
    }

    public char readChar() {
        return readValueFromList((char) 0);
    }

    @Implementation
    @SuppressWarnings("unchecked")
    public void writeByte(byte b) {
        addValueToList(Pair.create(Byte.SIZE / 8, b));
    }

    @Implementation
    public byte readByte() {
        return readValueFromList((byte) 0);
    }

    @Implementation
    public void readBooleanArray(boolean[] val) {
        int N = readInt();
        if (val.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < val.length; i++) {
            val[i] = readBoolean();
        }
    }

    @Implementation
    public void writeBooleanArray(boolean[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (boolean b : val)
            writeBoolean(b);
    }

    @Implementation
    public boolean[] createBooleanArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        boolean[] val = new boolean[N];
        for (int i = 0; i < val.length; i++) {
            val[i] = readBoolean();
        }
        return val;
    }

    @Implementation
    public void readCharArray(char[] val) {
        int N = readInt();
        if (val.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < val.length; i++) {
            val[i] = readChar();
        }
    }

    @Implementation
    public void writeCharArray(char[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (char b : val)
            writeChar(b);
    }

    @Implementation
    public char[] createCharArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        char[] val = new char[N];
        for (int i = 0; i < val.length; i++) {
            val[i] = readChar();
        }
        return val;
    }

    @Implementation
    public void readFloatArray(float[] val) {
        int N = readInt();
        if (val.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < val.length; i++) {
            val[i] = readFloat();
        }
    }

    @Implementation
    public void writeFloatArray(float[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (float f : val)
            writeFloat(f);
    }

    @Implementation
    public float[] createFloatArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        float[] val = new float[N];
        for (int i = 0; i < val.length; i++) {
            val[i] = readFloat();
        }
        return val;
    }

    @Implementation
    public void writeDoubleArray(double[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (double f : val)
            writeDouble(f);
    }

    @Implementation
    public void readDoubleArray(double[] val) {
        int N = readInt();
        if (val.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < val.length; i++) {
            val[i] = readDouble();
        }
    }

    @Implementation
    public double[] createDoubleArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        double[] val = new double[N];
        for (int i = 0; i < val.length; i++) {
            val[i] = readDouble();
        }
        return val;
    }

    @Implementation
    public void writeIntArray(int[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (int f : val)
            writeInt(f);
    }

    @Implementation
    public void readIntArray(int[] val) {
        int N = readInt();
        if (val.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < val.length; i++) {
            val[i] = readInt();
        }
    }

    @Implementation
    public int[] createIntArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        int[] val = new int[N];
        for (int i = 0; i < val.length; i++) {
            val[i] = readInt();
        }
        return val;
    }

    @Implementation
    public void writeByteArray(byte[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (byte f : val)
            writeByte(f);
    }

    @Implementation
    public void readByteArray(byte[] val) {
        int N = readInt();
        if (val.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < val.length; i++) {
            val[i] = readByte();
        }
    }

    @Implementation
    public byte[] createByteArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        byte[] val = new byte[N];
        for (int i = 0; i < val.length; i++) {
            val[i] = readByte();
        }
        return val;
    }

    @Implementation
    public void writeLongArray(long[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (long f : val)
            writeLong(f);
    }

    @Implementation
    public void readLongArray(long[] val) {
        int N = readInt();
        if (val.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < val.length; i++) {
            val[i] = readLong();
        }
    }

    @Implementation
    public long[] createLongArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        long[] val = new long[N];
        for (int i = 0; i < val.length; i++) {
            val[i] = readLong();
        }
        return val;
    }

    @Implementation
    public void writeStringArray(String[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(val.length);
        for (String f : val)
            writeString(f);
    }

    @Implementation
    public String[] createStringArray() {
        String[] array = null;

        int N = readInt();
        if (N >= 0) {
            array = new String[N];
            for (int i = 0; i < N; i++) {
                array[i] = readString();
            }
        }
        return array;
    }

    @Implementation
    public void readStringArray(String[] dest) {
        int N = readInt();
        if (dest.length != N)
            throw new RuntimeException("bad array lengths");
        for (int i = 0; i < dest.length; i++) {
            dest[i] = readString();
        }
    }

    @Implementation
    public void writeStringList(List<String> strings) {
        if (strings == null) {
            writeInt(-1);
            return;
        }
        int count = strings.size();
        int i = 0;
        writeInt(count);
        while (i < count) {
            writeString(strings.get(i));
            i++;
        }
    }

    @Implementation
    public void readStringList(List<String> list) {
        int listSizeBeforeChange = list.size();
        int addCount = readInt();
        int i = 0;
        for (; i < listSizeBeforeChange && i < addCount; i++) {
            list.set(i, readString());
        }
        for (; i < addCount; i++) {
            list.add(readString());
        }
        for (; i < listSizeBeforeChange; i++) {
            list.remove(addCount);
        }
    }

    @Implementation
    public ArrayList<String> createStringArrayList() {
        int N = readInt();
        if (N < 0) {
            return null;
        }

        ArrayList<String> l = new ArrayList<String>(N);
        while (N > 0) {
            l.add(readString());
            N--;
        }
        return l;
    }

    @Implementation
    public void writeArray(Object[] values) {
        if (values == null) {
            writeInt(-1);
            return;
        }
        int N = values.length;
        writeInt(N);
        for (Object value : values) {
            writeValue(value);
        }
    }

    @Implementation
    public Object[] readArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Object[] l = new Object[N];
        readArrayInternal(l, N, loader);
        return l;
    }

    @Implementation
    public void writeValue(Object v) {
        if (v == null) {
            writeInt(VAL_NULL);
        } else if (v instanceof String) {
            writeInt(VAL_STRING);
            writeString((String) v);
        } else if (v instanceof Integer) {
            writeInt(VAL_INTEGER);
            writeInt((Integer) v);
        } else if (v instanceof Map) {
            writeInt(VAL_MAP);
            writeMap((Map) v);
        } else if (v instanceof Bundle) {
            // Must be before Parcelable
            writeInt(VAL_BUNDLE);
            writeBundle((Bundle) v);
        } else if (v instanceof Parcelable) {
            writeInt(VAL_PARCELABLE);
            writeParcelable((Parcelable) v, 0);
        } else if (v instanceof Short) {
            writeInt(VAL_SHORT);
            writeInt(((Short) v).intValue());
        } else if (v instanceof Long) {
            writeInt(VAL_LONG);
            writeLong((Long) v);
        } else if (v instanceof Float) {
            writeInt(VAL_FLOAT);
            writeFloat((Float) v);
        } else if (v instanceof Double) {
            writeInt(VAL_DOUBLE);
            writeDouble((Double) v);
        } else if (v instanceof Boolean) {
            writeInt(VAL_BOOLEAN);
            writeInt((Boolean) v ? 1 : 0);
        } else if (v instanceof CharSequence) {
            // Must be after String
            writeInt(VAL_CHARSEQUENCE);
            TextUtils.writeToParcel((CharSequence) v, realParcel, 0);
        } else if (v instanceof boolean[]) {
            writeInt(VAL_BOOLEANARRAY);
            writeBooleanArray((boolean[]) v);
        } else if (v instanceof byte[]) {
            writeInt(VAL_BYTEARRAY);
            writeByteArray((byte[]) v);
        } else if (v instanceof String[]) {
            writeInt(VAL_STRINGARRAY);
            writeStringArray((String[]) v);
        } else if (v instanceof Object[]) {
            writeInt(VAL_OBJECTARRAY);
            writeArray((Object[]) v);
        } else if (v instanceof int[]) {
            writeInt(VAL_INTARRAY);
            writeIntArray((int[]) v);
        } else if (v instanceof long[]) {
            writeInt(VAL_LONGARRAY);
            writeLongArray((long[]) v);
        } else if (v instanceof Byte) {
            writeInt(VAL_BYTE);
            writeByte((Byte) v);
        } else {
            throw new RuntimeException(
                    "Parcel: unable to marshal value with type" + v.getClass().getName());
        }
    }

    @Implementation
    public Object readValue(ClassLoader loader) {
        int type = readInt();

        switch (type) {
            case VAL_NULL:
                return null;

            case VAL_STRING:
                return readString();

            case VAL_INTEGER:
                return readInt();

            case VAL_MAP:
                return readHashMap(loader);

            case VAL_PARCELABLE:
                return readParcelable(loader);

            case VAL_SHORT:
                return (short) readInt();

            case VAL_LONG:
                return readLong();

            case VAL_FLOAT:
                return readFloat();

            case VAL_DOUBLE:
                return readDouble();

            case VAL_BOOLEAN:
                return readInt() == 1;

            case VAL_CHARSEQUENCE:
                return TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(realParcel);

            case VAL_BOOLEANARRAY:
                return createBooleanArray();

            case VAL_BYTEARRAY:
                return createByteArray();

            case VAL_STRINGARRAY:
                return createStringArray();

            case VAL_OBJECTARRAY:
                return readArray(loader);

            case VAL_INTARRAY:
                return createIntArray();

            case VAL_LONGARRAY:
                return createLongArray();

            case VAL_BYTE:
                return readByte();

            case VAL_BUNDLE:
                return readBundle(loader); // loading will be deferred

            default:
                int off = dataPosition() - 4;
                throw new RuntimeException(
                        "Parcel " + this + ": Unmarshalling unknown type code " + type
                        + " at offset " + off);
        }
    }

    @Implementation
    public Bundle readBundle() {
        return readBundle(null);
    }

    @Implementation
    public Bundle readBundle(ClassLoader loader) {
        int length = readInt();
        if (length < 0) {
            return null;
        }

        final Bundle bundle = constructor().withParameterTypes(Parcel.class, int.class)
            .in(Bundle.class).newInstance(realParcel, length);
        if (loader != null) {
            bundle.setClassLoader(loader);
        }
        return bundle;
    }

    @Implementation
    public void writeBundle(Bundle val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        val.writeToParcel(realParcel, 0);
    }

    @Implementation
    public void writeParcelable(Parcelable p, int flags) {
        if (p == null) {
            writeString(null);
            return;
        }
        String name = p.getClass().getName();
        writeString(name);
        p.writeToParcel(realParcel, flags);
    }

    @Implementation
    public <T extends Parcelable> T readParcelable(ClassLoader loader) {
        String name = readString();
        if (name == null) {
            return null;
        }
        Parcelable.Creator<T> creator;
        try {
            Class c = loader == null ? Class.forName(name) : Class.forName(name, true, loader);
            Field f = c.getField("CREATOR");
            creator = (Parcelable.Creator) f.get(null);
        } catch (IllegalAccessException e) {
            Log.e("Parcel", "Class not found when unmarshalling: " + name + ", e: " + e);
            throw new RuntimeException("IllegalAccessException when unmarshalling: " + name);
        } catch (ClassNotFoundException e) {
            Log.e("Parcel", "Class not found when unmarshalling: " + name + ", e: " + e);
            throw new RuntimeException("ClassNotFoundException when unmarshalling: " + name);
        } catch (ClassCastException e) {
            throw new RuntimeException("Parcelable protocol requires a "
                    + "Parcelable.Creator object called " + " CREATOR on class " + name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Parcelable protocol requires a "
                    + "Parcelable.Creator object called " + " CREATOR on class " + name);
        }
        if (creator == null) {
            throw new RuntimeException("Parcelable protocol requires a "
                    + "Parcelable.Creator object called " + " CREATOR on class " + name);
        }

        return creator.createFromParcel(realParcel);
    }

    @Implementation
    public ArrayList createTypedArrayList(Parcelable.Creator c) {
        int N = readInt();
        if (N < 0) {
            return null;
        }

        ArrayList l = new ArrayList(N);

        while (N > 0) {
            if (readInt() != 0) {
                l.add(c.createFromParcel(realParcel));
            } else {
                l.add(null);
            }
            N--;
        }
        return l;
    }

    @Implementation
    public void writeTypedList(List val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.size();
        int i = 0;
        writeInt(N);
        while (i < N) {
            Object item = val.get(i);
            if (item != null) {
                writeInt(1);
                ((Parcelable) item).writeToParcel(realParcel, 0);
            } else {
                writeInt(0);
            }
            i++;
        }
    }

    @Implementation
    public void writeMap(Map val) {
        writeMapInternal(val);
    }

    @Implementation
    public void readMap(Map outVal, ClassLoader loader) {
        int N = readInt();
        readMapInternal(outVal, N, loader);
    }

    @Implementation
    public HashMap readHashMap(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        HashMap m = new HashMap(N);
        readMapInternal(m, N, loader);
        return m;
    }

    private void writeMapInternal(Map<String, Object> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        Set<Map.Entry<String, Object>> entries = val.entrySet();
        writeInt(entries.size());
        for (Map.Entry<String, Object> e : entries) {
            writeValue(e.getKey());
            writeValue(e.getValue());
        }
    }

    private void readMapInternal(Map outVal, int N, ClassLoader loader) {
        for (int i = 0; i < N; i++) {
            Object key = readValue(loader);
            Object value = readValue(loader);
            outVal.put(key, value);
        }
    }

    private void readArrayInternal(Object[] outVal, int N, ClassLoader loader) {
        for (int i = 0; i < N; i++) {
            Object value = readValue(loader);
            // Log.d("Parcel", "Unmarshalling value=" + value);
            outVal[i] = value;
        }
    }

    private void addValueToList(Pair<Integer, ?> value) {
        if (index < parcelData.size()) {
            parcelData.set(index, value);
        } else {
            parcelData.add(value);
        }
        index++;
    }

    private <T extends Object> T readValueFromList(T defaultValue) {
        if (index < parcelData.size()) {
            return (T) parcelData.get(index++).second;
        } else {
            return defaultValue;
        }
    }
}
