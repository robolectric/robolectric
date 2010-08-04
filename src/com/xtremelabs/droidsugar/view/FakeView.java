package com.xtremelabs.droidsugar.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeView {
    public static final int UNINITIALIZED_ATTRIBUTE = -1000;

    private View realView;

    private int id;
    private List<View> children = new ArrayList<View>();
    private FakeView parent;
    private Context context;
    public boolean selected;
    private View.OnClickListener onClickListener;
    private Object tag;
    private boolean enabled = true;
    public int visibility = UNINITIALIZED_ATTRIBUTE;
    public int height;
    public int width;
    public int paddingLeft;
    public int paddingTop;
    public int paddingRight;
    public int paddingBottom;
    public ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(0, 0);
    private Map<Integer, Object> tags = new HashMap<Integer, Object>();
    public boolean clickable;
    public boolean focusable;

    public FakeView(View view) {
        this.realView = view;
    }

    public void __constructor__(Context context) {
        this.context = context;
    }

    public void __constructor__(Context context, AttributeSet attrs) {
        __constructor__(context);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public int getId() {
        return id;
    }

    public static View inflate(Context context, int resource, ViewGroup root) {
        View view = FakeContextWrapper.viewLoader.inflateView(context, resource);
        if (root != null) {
            root.addView(view);
        }
        return view;
    }

    public View findViewById(int id) {
        if (id == this.id) {
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

    public View getRootView() {
        FakeView root = this;
        while(root.parent != null) {
            root = root.parent;
        }
        return root.realView;
    }

    public void addView(View child) {
        children.add(child);
        childProxy(child).parent = this;
    }

    private FakeView childProxy(View child) {
        return (FakeView) ProxyDelegatingHandler.getInstance().proxyFor(child);
    }

    public int getChildCount() {
        return children.size();
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        layoutParams = params;
    }

    public View getChildAt(int index) {
        return children.get(index);
    }

    public void removeAllViews() {
        for (View child : children) {
            childProxy(child).parent = null;
        }
        children.clear();
    }

    public final Context getContext() {
        return context;
    }

    public Resources getResources() {
        return context.getResources();
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public boolean performClick() {
        if (onClickListener != null) {
            onClickListener.onClick(realView);
            return true;
        } else {
            return false;
        }
    }

    public Object getTag() {
        return this.tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public final int getHeight() {
        return height;
    }

    public final int getWidth() {
        return width;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        paddingLeft = left;
        paddingTop = top;
        paddingRight = right;
        paddingBottom = bottom;
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public Object getTag(int key) {
        return tags.get(key);
    }

    public void setTag(int key, Object value) {
        tags.put(key, value);
    }
}
