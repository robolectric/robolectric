package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow of {@code MenuInflater} that actually inflates menus into {@code View}s that are functional enough to
 * support testing.
 */

@Implements(MenuInflater.class)
public class ShadowMenuInflater {
    private Context context;

    public void __constructor__(Context context) {
        this.context = context;
    }

    @Implementation
    public void inflate(int resource, Menu root) {
        shadowOf(context.getApplicationContext()).getResourceLoader().inflateMenu(context, resource, root);
    }

}
