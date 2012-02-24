package com.xtremelabs.robolectric.shadows;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(ContentProviderOperation.Builder.class)
public class ShadowContentProviderOperationBuilder {
    @RealObject private Builder builder;
    private final Map<String, Object> values = new HashMap<String, Object>();
    private final Map<String, String[]> selections = new HashMap<String, String[]>();
    private Uri uri;
    
    @Implementation
    public Builder withValue(String key, Object value) {
        values.put(key, value);
        return builder;
    }
    
    @Implementation
    public Builder withSelection(String selection, String[] selectionArgs) {
        selections.put(selection, selectionArgs);
        return builder;
    }
    
    @Implementation
    public ContentProviderOperation build() {
        return Robolectric.newInstanceOf(ContentProviderOperation.class);
    }
    
    public Uri getUri() {
        return uri;
    }
    
    public void setUri(Uri uri) {
        this.uri = uri;
    }
    
    public Map<String, Object> getValues() {
        return values;
    }
    
    public Map<String, String[]> getSelections() {
        return selections;
    }
}