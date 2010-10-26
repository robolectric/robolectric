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
        View view = getViewLoader().inflateView(context, resource);
        if (root != null && attachToRoot) {
            root.addView(view);
        }
        return view;
    }

    private ViewLoader getViewLoader() {
        return ((ShadowApplication) shadowOf(context.getApplicationContext())).getResourceLoader().viewLoader;
    }

    @Implementation
    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, true);
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
            return shadowApplication.layoutInflater;
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
