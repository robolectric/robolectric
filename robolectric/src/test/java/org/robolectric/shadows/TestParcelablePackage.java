package org.robolectric.shadows;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is intentionally package private to verify that Robolectric is able to parcel
 * non-public classes.
 *
 * <p>DO NOT CHANGE TO PUBLIC.
 */
class TestParcelablePackage implements Parcelable {
  int contents;

  public TestParcelablePackage(int contents) {
    this.contents = contents;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final CreatorImpl CREATOR = new CreatorImpl();

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(contents);
  }

  public static class CreatorImpl implements Creator<TestParcelablePackage> {
    @Override
    public TestParcelablePackage createFromParcel(Parcel source) {
      return new TestParcelablePackage(source.readInt());
    }

    @Override
    public TestParcelablePackage[] newArray(int size) {
      return new TestParcelablePackage[size];
    }
  }
}
