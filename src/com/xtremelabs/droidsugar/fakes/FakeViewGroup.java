package com.xtremelabs.droidsugar.fakes;

import android.view.ViewGroup;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.class)
public class FakeViewGroup extends FakeView {
    public FakeViewGroup(ViewGroup viewGroup) {
        super(viewGroup);
    }
}
