package com.xtremelabs.robolectric.shadows;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ContentProviderOperation.class)
public class ShadowContentProviderOperation {
    private final Map<String, Object> values = new HashMap<String, Object>();
    private final Map<String, String[]> selections = new HashMap<String, String[]>();
    private final Map<String, Integer> withValueBackReferences = new HashMap<String, Integer>();
    private Uri uri;
    private boolean isInsert;
    private boolean isUpdate;
    private boolean isDelete;
    
    @Implementation
    public static Builder newInsert(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        shadowBuilder.setUri(uri);
        shadowBuilder.setInsert(true);
        return builder;
    }
    
    @Implementation
    public static Builder newUpdate(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        shadowBuilder.setUri(uri);
        shadowBuilder.setUpdate(true);
        return builder;
    }
    
    @Implementation
    public static Builder newDelete(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowContentProviderOperationBuilder shadowBuilder = Robolectric.shadowOf(builder);
        shadowBuilder.setUri(uri);
        shadowBuilder.setDelete(true);
        return builder;
    }
   
    @Implementation
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
    
    public boolean isInsert() {
        return isInsert;
    }
    
    public void setInsert(boolean value) {
        isInsert = value;
    }
    
    public boolean isUpdate() {
        return isUpdate;
    }
    
    public void setUpdate(boolean value) {
        isUpdate = value;
    }
    
    public boolean isDelete() {
        return isDelete;
    }
    
    public void setDelete(boolean value) {
        isDelete = value;
    }
    
    public void setWithValueBackReference(String key, int previousResult) {
        withValueBackReferences.put(key, previousResult);
    }
    
    public int getWithValueBackReference(String key) {
        return withValueBackReferences.get(key);
    }
}
