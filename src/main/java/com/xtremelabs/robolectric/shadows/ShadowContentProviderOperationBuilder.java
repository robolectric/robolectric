package com.xtremelabs.robolectric.shadows;

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
    @RealObject private Builder realBuilder;
    private ContentProviderOperation contentProviderOperation;
    private ShadowContentProviderOperation shadowContentProviderOperation;
    
    public void __constructor__() {
        contentProviderOperation = Robolectric.newInstanceOf(ContentProviderOperation.class);
        shadowContentProviderOperation = Robolectric.shadowOf(contentProviderOperation);
    }
    
    @Implementation
    public Builder withValue(String key, Object value) {
        shadowContentProviderOperation.getValues().put(key, value);
        return realBuilder;
    }
    
    @Implementation
    public Builder withSelection(String selection, String[] selectionArgs) {
        shadowContentProviderOperation.getSelections().put(selection, selectionArgs);
        return realBuilder;
    }
    
    @Implementation
    public Builder withValueBackReference(String key, int previousResult) {
        shadowContentProviderOperation.setWithValueBackReference(key, previousResult);
        return realBuilder;
    }
    
    @Implementation
    public ContentProviderOperation build() {
        return contentProviderOperation;
    }
    
    public Uri getUri() {
        return shadowContentProviderOperation.getUri();
    }
    
    public void setUri(Uri uri) {
        shadowContentProviderOperation.setUri(uri);
    }
    
    public Map<String, Object> getValues() {
        return shadowContentProviderOperation.getValues();
    }
    
    public Map<String, String[]> getSelections() {
        return shadowContentProviderOperation.getSelections();
    }
    
    public boolean isInsert() {
        return shadowContentProviderOperation.isInsert();
    }
    
    public void setInsert(boolean value) {
        shadowContentProviderOperation.setInsert(value);
    }
    
    public boolean isUpdate() {
        return shadowContentProviderOperation.isUpdate();
    }
    
    public void setUpdate(boolean value) {
        shadowContentProviderOperation.setUpdate(value);
    }
    
    public boolean isDelete() {
        return shadowContentProviderOperation.isDelete();
    }
    
    public void setDelete(boolean value) {
        shadowContentProviderOperation.setDelete(value);
    }
    
    public int getWithValueBackReference(String key) {
        return shadowContentProviderOperation.getWithValueBackReference(key);
    }
}