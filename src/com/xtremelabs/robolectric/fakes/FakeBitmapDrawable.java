package com.xtremelabs.robolectric.fakes;

import android.graphics.drawable.BitmapDrawable;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class FakeBitmapDrawable extends FakeDrawable {
    public int loadedFromResourceId;
}
