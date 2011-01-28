package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.internal.AppSingletonizer;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.ResourceLoader;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow of {@code LayoutInflater} that actually inflates layouts into {@code View}s that are functional enough to
 * support testing.
 */

@Implements(LayoutInflater.class)
public class ShadowLayoutInflater {
    private static AppSingletonizer<LayoutInflater> instances = new LayoutInflaterAppSingletonizer();

    private Context context;

    private static LayoutInflater bind(LayoutInflater layoutInflater, Context context) {
        shadowOf(layoutInflater).context = context;
        return layoutInflater;
    }

    @Implementation
    public static LayoutInflater from(Context context) {
        return bind(instances.getInstance(context), context);
    }

    @Implementation
    public Context getContext() {
        return context;
    }

    @Implementation
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        return getResourceLoader().inflateView(context, resource, attachToRoot ? root : null);
    }

    @Implementation
    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, root != null);
    }

    private ResourceLoader getResourceLoader() {
        return shadowOf(context.getApplicationContext()).getResourceLoader();
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
