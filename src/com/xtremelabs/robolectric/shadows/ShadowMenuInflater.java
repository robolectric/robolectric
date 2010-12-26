package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.view.MenuInflater;
import android.view.Menu;
import com.xtremelabs.robolectric.res.MenuLoader;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

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
        getMenuLoader().inflateMenu(context, resource);
    }

    private MenuLoader getMenuLoader() {
        return shadowOf(context.getApplicationContext()).getResourceLoader().menuLoader;
    }
}
