package org.robolectric.shadows;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Implements(Parcel.class)
@SuppressWarnings("unchecked")
public class ShadowParcel {
  private ArrayList parcelData = new ArrayList();
  private int index = 0;

  @RealObject
  private Parcel realParcel;

  @Implementation
  public static Parcel obtain() {
    return Robolectric.newInstanceOf(Parcel.class);
  }

  @Implementation
  public void writeString(String str) {
    if (str == null) {
      return;
    }
    parcelData.add(str);
  }

  @Implementation
  public void writeInt(int i) {
    parcelData.add(i);
  }

  @Implementation
  public void writeLong(long i) {
    parcelData.add(i);
  }

  @Implementation
  public void writeFloat(float f) {
    parcelData.add(f);
  }

  @Implementation
  public void writeDouble(double f) {
    parcelData.add(f);
  }

  @Implementation
  @SuppressWarnings("unchecked")
  public void writeByte(byte b) {
    parcelData.add(b);
  }

  @Implementation
  public void writeStrongBinder(IBinder binder) {
    if (binder == null) {
      return;
    }
    parcelData.add(binder);
  }

  @Implementation
  public final void writeMap(Map map) {
    if (map == null) {
      writeInt(-1);
      return;
    }
    Set<Map.Entry<String,Object>> entries = map.entrySet();
    writeInt(entries.size());
    for (Map.Entry<String,Object> e : entries) {
      parcelData.add(e.getKey());
      parcelData.add(e.getValue());
    }
  }

  @Implementation
  public String readString() {
    return index < parcelData.size() ? (String) parcelData.get(index++) : null;
  }

  @Implementation
  public int readInt() {
    return index < parcelData.size() ? (Integer) parcelData.get(index++) : 0;
  }

  @Implementation
  public float readFloat() {
    return index < parcelData.size() ? (Float) parcelData.get(index++) : 0;
  }

  @Implementation
  public double readDouble() {
    return index < parcelData.size() ? (Double) parcelData.get(index++) : 0;
  }

  @Implementation
  public byte readByte() {
    return index < parcelData.size() ? (Byte) parcelData.get(index++) : 0;
  }

  @Implementation
  public long readLong() {
    return index < parcelData.size() ? (Long) parcelData.get(index++) : 0;
  }

  @Implementation
  public Bundle readBundle() {
    return index < parcelData.size() ? (Bundle) parcelData.get(index++) : null;
  }

  @Implementation
  public IBinder readStrongBinder() {
    return index < parcelData.size() ? (IBinder) parcelData.get(index++) : null;
  }

  @Implementation
  public final HashMap readHashMap(ClassLoader loader)
  {
    int count = readInt();
    if (count < 0) {
      return null;
    }
    HashMap map = new HashMap(count);
    for (int i = 0; i < count; ++i) {
      Object key = parcelData.get(index++);
      Object value = parcelData.get(index++);
      map.put(key, value);
    }
    return map;
  }

  @Implementation
  public Bundle readBundle(ClassLoader loader) {
    return readBundle();
  }

  @Implementation
  public void writeBundle(Bundle bundle) {
    parcelData.add(bundle);
  }

  @Implementation
  public void writeParcelable(Parcelable p, int flags) {
    parcelData.add(p);
  }

  @Implementation
  public Parcelable readParcelable(ClassLoader cl) {
    return index < parcelData.size() ? (Parcelable) parcelData.get(index++) : null;
  }

  @Implementation
  public void readFloatArray(float[] val) {
    int n = readInt();
    if (val.length != n) throw new RuntimeException("bad array lengths");
    for (int i = 0; i < val.length; i++) {
      val[i] = readFloat();
    }
  }

  @Implementation
  public void writeFloatArray(float[] val) {
    writeInt(val.length);
    for (float f : val) writeFloat(f);
  }

  @Implementation
  public void writeDoubleArray(double[] val) {
    writeInt(val.length);
    for (double f : val) writeDouble(f);
  }

  @Implementation
  public void readDoubleArray(double[] val) {
    int n = readInt();
    if (val.length != n) throw new RuntimeException("bad array lengths");
    for (int i = 0; i < val.length; i++) {
      val[i] = readDouble();
    }
  }

  @Implementation
  public void writeIntArray(int[] val) {
    writeInt(val.length);
    for (int f : val) writeInt(f);
  }

  @Implementation
  public void readIntArray(int[] val) {
    int n = readInt();
    if (val.length != n) throw new RuntimeException("bad array lengths");
    for (int i = 0; i < val.length; i++) {
      val[i] = readInt();
    }
  }

  @Implementation
  public void writeLongArray(long[] val) {
    writeInt(val.length);
    for (long f : val) writeLong(f);
  }

  @Implementation
  public void readLongArray(long[] val) {
    int n = readInt();
    if (val.length != n) throw new RuntimeException("bad array lengths");
    for (int i = 0; i < val.length; i++) {
      val[i] = readLong();
    }
  }

  @Implementation
  public void writeStringArray(String[] val) {
    writeInt(val.length);
    for (String f : val) writeString(f);
  }

  @Implementation
  public void readStringArray(String[] dest) {
    int n = readInt();
    if (dest.length != n) throw new RuntimeException("bad array lengths");
    for (int i = 0; i < dest.length; i++) {
      dest[i] = readString();
    }
  }

  @Implementation
  public final String[] createStringArray() {
    int N = readInt();
    if (N >= 0) {
      String[] val = new String[N];
      for (int i=0; i<N; i++) {
        val[i] = readString();
      }
      return val;
    } else {
      return null;
    }
  }

  @Implementation
  public void writeStringList(List<String> strings) {
    if (strings == null) {
      writeInt(-1);
      return;
    }
    int count = strings.size();
    int i=0;
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
    for (; i<addCount; i++) {
      list.add(readString());
    }
    for (; i<listSizeBeforeChange; i++) {
      list.remove(addCount);
    }
  }

  @Implementation
  public ArrayList<String> createStringArrayList() {
    int n = readInt();
    if (n < 0) {
      return null;
    }

    ArrayList<String> l = new ArrayList<String>(n);
    while (n > 0) {
      l.add(readString());
      n--;
    }
    return l;
  }

  @Implementation
  public ArrayList createTypedArrayList(Parcelable.Creator c) {
    int n = readInt();
    if (n < 0) {
      return null;
    }

    ArrayList l = new ArrayList(n);

    while (n > 0) {
      if (readInt() != 0) {
        l.add(c.createFromParcel(realParcel));
      } else {
        l.add(null);
      }
      n--;
    }
    return l;
  }

  @Implementation
  public void writeTypedList(List val) {
    if (val == null) {
      writeInt(-1);
      return;
    }

    int n = val.size();
    int i = 0;
    writeInt(n);
    while (i < n) {
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

  public int getIndex() {
    return index;
  }

  public List getParcelData() {
    return parcelData;
  }

}
