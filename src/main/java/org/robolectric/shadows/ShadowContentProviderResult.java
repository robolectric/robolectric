package com.xtremelabs.robolectric.shadows;

import java.lang.reflect.Field;

import android.content.ContentProviderResult;
import android.net.Uri;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(ContentProviderResult.class)
public class ShadowContentProviderResult {
    @RealObject ContentProviderResult realResult;
    
    public void __constructor__(Uri uri) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = realResult.getClass().getField("uri");
        field.setAccessible(true);
        field.set(realResult, uri);
    }
    
    public void __constructor__(int count) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = realResult.getClass().getField("count");
        field.setAccessible(true);
        field.set(realResult, count);
    }
}