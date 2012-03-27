package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(CursorLoader.class)
public class ShadowCursorLoader {
    private Context context;
    private Uri uri;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String sortOrder;
    
    public void __constructor__(Context context) {
        this.context = context;
    }
    
    public void __constructor__(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        this.context = context;
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }
    
    @Implementation
    public Uri getUri() {
        return uri;
    }
    
    @Implementation
    public void setUri(Uri uri) {
        this.uri = uri;
    }
    
    @Implementation
    public String[] getProjection() {
        return projection;
    }
    
    @Implementation
    public void setProjection(String[] projection) {
        this.projection = projection;
    }
    
    @Implementation
    public String getSelection() {
        return selection;
    }
    
    @Implementation
    public void setSelection(String selection) {
        this.selection = selection;
    }
    
    @Implementation
    public String[] getSelectionArgs() {
        return selectionArgs;
    }
    
    @Implementation
    public void setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }
    
    @Implementation
    public String getSortOrder() {
        return sortOrder;
    }
    
    @Implementation
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
