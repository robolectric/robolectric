package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.class)
public class FakeViewGroup extends FakeView {
    public FakeViewGroup(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public String innerText() {
        String innerText = "";
        boolean first = true;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            String childText = ((FakeView) ProxyDelegatingHandler.getInstance().proxyFor(child)).innerText();
            if (!first && childText.length() > 0) {
                innerText += " ";
            }
            innerText += childText;
            first = false;
        }
        return innerText;
    }
}
