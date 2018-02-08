package org.robolectric.shadows;

import android.os.Parcelable;

abstract class TestParcelableBase implements Parcelable {
  int contents;

  protected TestParcelableBase(int contents) {
    this.contents = contents;
  }

  @Override
  public int describeContents() {
    return 0;
  }
}
