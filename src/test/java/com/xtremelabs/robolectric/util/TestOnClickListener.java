package com.xtremelabs.robolectric.util;

import android.view.View;

public class TestOnClickListener implements View.OnClickListener {
    public boolean clicked = false;

    @Override public void onClick(View v) {
        clicked = true;
    }
}
