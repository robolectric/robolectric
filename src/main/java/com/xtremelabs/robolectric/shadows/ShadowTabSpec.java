package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.TabHost;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(TabHost.TabSpec.class)
public class ShadowTabSpec {


    @RealObject
    TabHost.TabSpec realObject;

    private String tag;
    private View indicatorView;

    /**
     * Non-Android accessor, sets the tag on the TabSpec
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Implementation
    public java.lang.String getTag() {
        return tag;
    }

    /**
     * Non-Android accessor
     *
     * @return the view object set in a call to {@code TabSpec#setIndicator(View)}
     */
    public View getIndicatorAsView() {
        return this.indicatorView;
    }

    @Implementation
    public TabHost.TabSpec setIndicator(android.view.View view) {
        this.indicatorView = view;
        return realObject;
    }

}
