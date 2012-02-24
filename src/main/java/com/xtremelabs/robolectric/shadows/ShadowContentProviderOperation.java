package com.xtremelabs.robolectric.shadows;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ContentProviderOperation.class)
public class ShadowContentProviderOperation {
    @Implementation
    public static Builder newInsert(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowContentProviderOperationBuilder shadow = Robolectric.shadowOf(builder);
        shadow.setUri(uri);
        return builder;
    }
    
    @Implementation
    public static Builder newUpdate(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowContentProviderOperationBuilder shadow = Robolectric.shadowOf(builder);
        shadow.setUri(uri);
        return builder;
    }
    
    @Implementation
    public static Builder newDelete(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowContentProviderOperationBuilder shadow = Robolectric.shadowOf(builder);
        shadow.setUri(uri);
        return builder;
    }
}