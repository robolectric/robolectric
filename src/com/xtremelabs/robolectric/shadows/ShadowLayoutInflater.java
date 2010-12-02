package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.res.ViewLoader;
import com.xtremelabs.robolectric.util.AppSingletonizer;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow of {@code LayoutInflater} that actually inflates layouts into {@code View}s that are functional enough to
 * support testing.
 */

@Implements(LayoutInflater.class)
public class ShadowLayoutInflater {
    private static AppSingletonizer<LayoutInflater> instances = new LayoutInflaterAppSingletonizer();

    private Context context;

    @Implementation
    public static LayoutInflater from(Context context) {
        return bind(instances.getInstance(context), context);
    }

    @Implementation
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        return getViewLoader().inflateView(context, resource, attachToRoot ? root : null);
    }

    @Implementation
    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, root != null);
    }

    private ViewLoader getViewLoader() {
        return shadowOf(context.getApplicationContext()).getResourceLoader().viewLoader;
    }

    private static LayoutInflater bind(LayoutInflater layoutInflater, Context context) {
        shadowOf(layoutInflater).context = context;
        return layoutInflater;
    }

    private static class LayoutInflaterAppSingletonizer extends AppSingletonizer<LayoutInflater> {
        public LayoutInflaterAppSingletonizer() {
            super(LayoutInflater.class);
        }

        @Override protected LayoutInflater get(ShadowApplication shadowApplication) {
            return shadowApplication.getLayoutInflater();
        }

        @Override protected void set(ShadowApplication shadowApplication, LayoutInflater instance) {
            shadowApplication.layoutInflater = instance;
        }

        @Override protected LayoutInflater createInstance(Application applicationContext) {
            return new MyLayoutInflater(applicationContext);
        }

        private static class MyLayoutInflater extends LayoutInflater {
            public MyLayoutInflater(Context context) {
                super(context);
            }

            @Override public LayoutInflater cloneInContext(Context newContext) {
                return bind(new MyLayoutInflater(newContext), newContext);
            }
        }
    }
}
