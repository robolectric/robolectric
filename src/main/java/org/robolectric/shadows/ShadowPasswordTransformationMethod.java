package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PasswordTransformationMethod.class)
public class ShadowPasswordTransformationMethod implements TransformationMethod {

    private static ShadowPasswordTransformationMethod transformationMethod;

    @Implementation
    public CharSequence getTransformation(CharSequence charSequence, View view) {
        if(isEmpty(charSequence)) {
            return "";
        }
        return String.format(String.format("%%0%dd", charSequence.length()), 0).replace("0", "\u2022");
    }

    @Implementation
    public static ShadowPasswordTransformationMethod getInstance() {
        if(transformationMethod == null){
            transformationMethod = new ShadowPasswordTransformationMethod();
        }
        return transformationMethod;
    }

    @Implementation @Override
    public void onFocusChanged(View view, CharSequence charSequence, boolean b, int i, Rect rect) { }

    private boolean isEmpty(CharSequence value) {
        return (value == null || value.length() == 0);
    }
}
