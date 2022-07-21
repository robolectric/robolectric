package org.robolectric.shadows;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is intentionally package private to verify that Robolectric is able to parcel
 * non-public classes.
 *
 * <p>DO NOT CHANGE TO PUBLIC.
 */
class TestParcelableImpl extends TestParcelableBase implements Parcelable {

  public TestParcelableImpl(int contents) {
    super(contents);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<TestParcelableImpl> CREATOR =
      new Creator<TestParcelableImpl>() {
        @Override
        public TestParcelableImpl createFromParcel(Parcel source) {
          return new TestParcelableImpl(source.readInt());
        }

        @Override
        public TestParcelableImpl[] newArray(int size) {
          return new TestParcelableImpl[0];
        }
      };
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(contents);
  }
}
