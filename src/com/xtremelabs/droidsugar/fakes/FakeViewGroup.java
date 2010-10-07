package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.class)
public class FakeViewGroup extends FakeView {
    private List<View> children = new ArrayList<View>();

    public FakeViewGroup(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Implementation
    @Override
    public View findViewById(int id) {
        if (id == getId()) {
            return realView;
        }

        for (View child : children) {
            View found = child.findViewById(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Implementation
    public void addView(View child) {
        children.add(child);
        childProxy(child).parent = this;
    }

    @Implementation
    public int getChildCount() {
        return children.size();
    }

    @Implementation
    public View getChildAt(int index) {
        return children.get(index);
    }

    @Implementation
    public void removeAllViews() {
        for (View child : children) {
            childProxy(child).parent = null;
        }
        children.clear();
    }

    @Implementation
    public void removeViewAt(int position) {
        childProxy(children.remove(position)).parent = null;
    }

    private FakeView childProxy(View child) {
        return (FakeView) ProxyDelegatingHandler.getInstance().proxyFor(child);
    }

    @Override
    public String innerText() {
        String innerText = "";
        String delimiter = "";

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            String childText = ((FakeView) ProxyDelegatingHandler.getInstance().proxyFor(child)).innerText();
            if (childText.length() > 0) {
                innerText += delimiter;
                delimiter = " ";
            }
            innerText += childText;
        }
        return innerText;
    }
}
