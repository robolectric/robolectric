package org.robolectric.res.android;

import static org.robolectric.res.android.Formatter.toHex;

import org.robolectric.res.android.ResourceTypes.Res_value;


/**
 * Representation of a value in a resource, supplying type information.
 *
 * <p>frameworks/base/include/androidfw/ResourceTypes.h (struct Res_value)
 */
public final class ResValue {
  // must be one of DataType
  public int dataType = DataType.NULL.code();
  public int data;

  public ResValue(int dataType, int data) {
    this.dataType = dataType;
    this.data = data;
  }

  // Copy constructor.
  ResValue(ResValue that) {
    this(that.dataType, that.data);
  }

  public ResValue() {
  }

  public void update(ResValue other) {
    dataType = other.dataType;
    data = other.data;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(23);
    sb.append("ResValue{t=")
        .append(toHex(dataType, 2))
        .append("/d=")
        .append(toHex(data, 8))
        .append("}");
    return sb.toString();
  }

  public void copyFrom_dtoh(ResValue resValue) {
    this.dataType = resValue.dataType;
    this.data = resValue.data;
  }

  public void copyFrom_dtoh(Res_value resValue) {
    this.dataType = resValue.dataType;
    this.data = resValue.data;
  }
}
