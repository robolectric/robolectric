package com.xtremelabs.droidsugar.fakes;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(View.class)
public class FakeView {
    @Deprecated
    public static final int UNINITIALIZED_ATTRIBUTE = -1000;

    protected View realView;

    private int id;
    FakeView parent;
    private Context context;
    public boolean selected;
    private View.OnClickListener onClickListener;
    private Object tag;
    private boolean enabled = true;
    public int visibility = View.VISIBLE;
    public int left;
    public int top;
    public int right;
    public int bottom;
    public int paddingLeft;
    public int paddingTop;
    public int paddingRight;
    public int paddingBottom;
    public ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(0, 0);
    private Map<Integer, Object> tags = new HashMap<Integer, Object>();
    public boolean clickable;
    public boolean focusable;
    public int backgroundResourceId = -1;
    protected View.OnKeyListener onKeyListener;
    public boolean hasFocus;
    private View.OnFocusChangeListener onFocusChangeListener;
    public boolean wasInvalidated;
    private View.OnTouchListener onTouchListener;

    public FakeView(View view) {
        this.realView = view;
    }

    public void __constructor__(Context context) {
        this.context = context;
    }

    public void __constructor__(Context context, AttributeSet attrs) {
        __constructor__(context);
    }

    @Implementation
    public void setId(int id) {
        this.id = id;
    }

    @Implementation
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    @Implementation
    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    @Implementation
    public int getId() {
        return id;
    }

    @Implementation
    public static View inflate(Context context, int resource, ViewGroup root) {
        View view = FakeHelper.resourceLoader.viewLoader.inflateView(context, resource);
        if (root != null) {
            root.addView(view);
        }
        return view;
    }

    @Implementation
    public View findViewById(int id) {
        if (id == this.id) {
            return realView;
        }

        return null;
    }

    @Implementation
    public View getRootView() {
        FakeView root = this;
        while(root.parent != null) {
            root = root.parent;
        }
        return root.realView;
    }

    @Implementation
    public ViewGroup.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    @Implementation
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        layoutParams = params;
    }

    @Implementation
    public final ViewParent getParent() {
        return parent == null ? null : (ViewParent) parent.realView;
    }

    @Implementation
    public final Context getContext() {
        return context;
    }

    @Implementation
    public Resources getResources() {
        return context.getResources();
    }

    @Implementation
    public void setBackgroundResource(int backgroundResourceId) {
        this.backgroundResourceId = backgroundResourceId;
    }

    @Implementation
    public int getVisibility() {
        return visibility;
    }

    @Implementation
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    @Implementation
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Implementation
    public boolean isSelected() {
        return this.selected;
    }

    @Implementation
    public boolean isEnabled() {
        return this.enabled;
    }
    
    @Implementation
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Implementation
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Implementation
    public boolean performClick() {
        if (onClickListener != null) {
            onClickListener.onClick(realView);
            return true;
        } else {
            return false;
        }
    }

    @Implementation
    public void setOnKeyListener(View.OnKeyListener onKeyListener) {
        this.onKeyListener = onKeyListener;
    }

    @Implementation
    public Object getTag() {
        return this.tag;
    }

    @Implementation
    public void setTag(Object tag) {
        this.tag = tag;
    }

    @Implementation
    public final int getHeight() {
        return bottom - top;
    }

    @Implementation
    public final int getWidth() {
        return right - left;
    }

    @Implementation
    public final int getMeasuredWidth() {
        return getWidth();
    }

    @Implementation
    public final void layout(int l, int t, int r, int b) {
        left = l;
        top = t;
        right = r;
        bottom = b;

// todo:       realView.onLayout();
    }
    
    @Implementation
    public void setPadding(int left, int top, int right, int bottom) {
        paddingLeft = left;
        paddingTop = top;
        paddingRight = right;
        paddingBottom = bottom;
    }

    @Implementation
    public int getPaddingTop() {
        return paddingTop;
    }

    @Implementation
    public int getPaddingLeft() {
        return paddingLeft;
    }

    @Implementation
    public int getPaddingRight() {
        return paddingRight;
    }

    @Implementation
    public int getPaddingBottom() {
        return paddingBottom;
    }

    @Implementation
    public Object getTag(int key) {
        return tags.get(key);
    }

    @Implementation
    public void setTag(int key, Object value) {
        tags.put(key, value);
    }

    @Implementation
    public final boolean requestFocus() {
        return requestFocus(View.FOCUS_DOWN);
    }

    @Implementation
    public final boolean requestFocus(int direction) {
        setViewFocus(true);
        return true;
    }

    public void setViewFocus(boolean hasFocus) {
        this.hasFocus = hasFocus;
        if (onFocusChangeListener != null) {
            onFocusChangeListener.onFocusChange(realView, hasFocus);
        }
    }

    @Implementation
    public boolean isFocused() {
        return hasFocus;
    }

    @Implementation
    public boolean hasFocus() {
        return hasFocus;
    }

    @Implementation
    public void clearFocus() {
        setViewFocus(false);
    }

    @Implementation
    public void setOnFocusChangeListener(View.OnFocusChangeListener listener) {
        onFocusChangeListener = listener;
    }

    @Implementation
    public void invalidate() {
        wasInvalidated = true;
    }

    @Implementation
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    @Implementation
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (onTouchListener != null) {
            return onTouchListener.onTouch(realView, event);
        }
        return false;
    }

    public String innerText() {
        return "";
    }
}
