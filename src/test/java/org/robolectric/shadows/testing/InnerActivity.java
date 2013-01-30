package com.xtremelabs.robolectric.shadows.testing;

import android.app.Activity;
import android.os.Bundle;
import com.xtremelabs.robolectric.R;

/**
 *
 */
public class InnerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lam_inner);
    }
}
