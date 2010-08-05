package com.xtremelabs.droidsugar.fakes;

import android.view.ViewGroup;

public class FakeLayoutParams {
    private final ViewGroup.LayoutParams realLayoutParams;

    public FakeLayoutParams(ViewGroup.LayoutParams realLayoutParams) {
        this.realLayoutParams = realLayoutParams;
    }

    public void __constructor__(int w, int h) {
        realLayoutParams.width = w;
        realLayoutParams.height = h;
    }
}
