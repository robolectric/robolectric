package org.robolectric.shadows;

import android.os.Parcel;
import android.os.Parcelable;

public class TestComplexParcelable implements Parcelable {

    String stringContent;
    Double doubleContent;
    Integer integerContent;
    TestParcelable parcelableContent;

    public TestComplexParcelable(String stringContent, Double doubleContent, Integer integerContent, TestParcelable parcelableContent) {
        this.stringContent = stringContent;
        this.doubleContent = doubleContent;
        this.integerContent = integerContent;
        this.parcelableContent = parcelableContent;
    }

    public TestComplexParcelable(Parcel source) {
        this.stringContent = source.readString();
        this.doubleContent = source.readDouble();
        this.integerContent = source.readInt();
        this.parcelableContent = source.readParcelable(Parcelable.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stringContent);
        dest.writeDouble(doubleContent);
        dest.writeInt(integerContent);
        dest.writeParcelable(parcelableContent, flags);
    }

    public static final Creator<TestComplexParcelable> CREATOR = new Creator<TestComplexParcelable>() {
        @Override
        public TestComplexParcelable createFromParcel(Parcel source) {
            return new TestComplexParcelable(source);
        }

        @Override
        public TestComplexParcelable[] newArray(int size) {
            return new TestComplexParcelable[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || TestComplexParcelable.class != o.getClass()) return false;

        TestComplexParcelable that = (TestComplexParcelable) o;

        if (doubleContent != null ? !doubleContent.equals(that.doubleContent) : that.doubleContent != null)
            return false;
        if (integerContent != null ? !integerContent.equals(that.integerContent) : that.integerContent != null)
            return false;
        if (parcelableContent != null ? !((Object)parcelableContent).equals(that.parcelableContent) : that.parcelableContent != null)
            return false;
        if (stringContent != null ? !stringContent.equals(that.stringContent) : that.stringContent != null)
            return false;

        return true;
    }

}
