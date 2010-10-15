package com.xtremelabs.droidsugar.fakes;

import android.graphics.drawable.BitmapDrawable;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class FakeBitmapDrawable extends FakeDrawable {
    public int loadedFromResourceId;
}
