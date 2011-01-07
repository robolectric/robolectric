package com.xtremelabs.robolectric.shadows;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadow for {@code RemoteViews} that simulates its implementation. Supports pending intents for events, changing
 * visibility, setting text, updating etc...
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(RemoteViews.class)
public class ShadowRemoteViews {
    private String packageName;
    private int layoutId;
    private List<ViewUpdater> viewUpdaters = new ArrayList<ViewUpdater>();

    public void __constructor__(String packageName, int layoutId) {
        this.packageName = packageName;
        this.layoutId = layoutId;
    }

    @Implementation
    public String getPackage() {
        return packageName;
    }

    @Implementation
    public int getLayoutId() {
        return layoutId;
    }

    @Implementation
    public void setTextViewText(int viewId, final CharSequence text) {
        viewUpdaters.add(new ViewUpdater(viewId) {
            @Override
            public void doUpdate(View view) {
                ((TextView) view).setText(text);
            }
        });
    }

    @Implementation
    public void setOnClickPendingIntent(int viewId, final PendingIntent pendingIntent) {
        viewUpdaters.add(new ViewUpdater(viewId) {
            @Override void doUpdate(final View view) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            pendingIntent.send(view.getContext(), 0, null);
                        } catch (PendingIntent.CanceledException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }

    @Implementation
    public void setViewVisibility(int viewId, final int visibility) {
        viewUpdaters.add(new ViewUpdater(viewId) {
            @Override
            public void doUpdate(View view) {
                view.setVisibility(visibility);
            }
        });
    }

    @Implementation
    public void setImageViewResource(int viewId, final int resourceId) {
        viewUpdaters.add(new ViewUpdater(viewId) {
            @Override
            public void doUpdate(View view) {
                ((ImageView) view).setImageResource(resourceId);
            }
        });
    }

    @Implementation
    public void setImageViewBitmap(int viewId, final Bitmap bitmap) {
        viewUpdaters.add(new ViewUpdater(viewId) {
            @Override
            public void doUpdate(View view) {
                ((ImageView) view).setImageBitmap(bitmap);
            }
        });
    }

    @Implementation
    public void reapply(Context context, View v) {
        for (ViewUpdater viewUpdater : viewUpdaters) {
            viewUpdater.update(v);
        }
    }

    private abstract class ViewUpdater {
        private int viewId;

        public ViewUpdater(int viewId) {
            this.viewId = viewId;
        }

        final void update(View parent) {

            View view = parent.findViewById(viewId);
            if (view == null) {
                throw new NullPointerException("couldn't find view " + viewId
                        + " (" + ResourceLoader.getFrom(parent.getContext()).getNameForId(viewId) + ")");
            }
            doUpdate(view);
        }

        abstract void doUpdate(View view);
    }
}
