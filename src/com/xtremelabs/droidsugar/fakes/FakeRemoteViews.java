package com.xtremelabs.droidsugar.fakes;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(RemoteViews.class)
public class FakeRemoteViews {
    private List<ViewUpdater> viewUpdaters = new ArrayList<ViewUpdater>();

    @Implementation
    public void setTextViewText(int viewId, final CharSequence text) {
        viewUpdaters.add(new ViewUpdater(viewId) {
            @Override public void doUpdate(View view) {
                ((TextView) view).setText(text);
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
            doUpdate(parent.findViewById(viewId));
        }

        abstract void doUpdate(View view);
    }
}
