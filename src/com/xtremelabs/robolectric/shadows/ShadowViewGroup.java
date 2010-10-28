package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.class)
public class ShadowViewGroup extends ShadowView {
    private List<View> children = new ArrayList<View>();

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
        shadowOf(child).parent = this;
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
            shadowOf(child).parent = null;
        }
        children.clear();
    }

    @Implementation
    public void removeViewAt(int position) {
        shadowOf(children.remove(position)).parent = null;
    }

    @Override
    public String innerText() {
        String innerText = "";
        String delimiter = "";

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            String childText = shadowOf(child).innerText();
            if (childText.length() > 0) {
                innerText += delimiter;
                delimiter = " ";
            }
            innerText += childText;
        }
        return innerText;
    }
}
