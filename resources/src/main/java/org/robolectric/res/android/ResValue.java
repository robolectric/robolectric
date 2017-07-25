package org.robolectric.res.android;

import static org.robolectric.res.android.Formatter.toHex;


/**
 * Representation of a value in a resource, supplying type information.
 *
 * <p>frameworks/base/include/androidfw/ResourceTypes.h (struct Res_value)
 */
public final class ResValue {
  public int dataType;
  public int data;

  public ResValue(int dataType, int data) {
    this.dataType = dataType;
    this.data = data;
  }

  // Copy constructor.
  ResValue(ResValue that) {
    this(that.dataType, that.data);
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
}
