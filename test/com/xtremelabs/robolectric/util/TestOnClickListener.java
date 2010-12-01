package com.xtremelabs.robolectric.util;

import org.junit.Ignore;

import android.view.View;

@Ignore
public class TestOnClickListener implements View.OnClickListener {
    public boolean clicked = false;
    @Override public void onClick(View v) {
        clicked = true;
    }
}
