package org.robolectric.shadows;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;
import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import java.util.HashMap;
import java.util.Map;

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
        ShadowBuilder shadowBuilder = Robolectric.shadowOf(builder);
        shadowBuilder.setUri(uri);
        shadowBuilder.setInsert(true);
        return builder;
    }
    
    @Implementation
    public static Builder newUpdate(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowBuilder shadowBuilder = Robolectric.shadowOf(builder);
        shadowBuilder.setUri(uri);
        shadowBuilder.setUpdate(true);
        return builder;
    }
    
    @Implementation
    public static Builder newDelete(Uri uri) {
        Builder builder = Robolectric.newInstanceOf(Builder.class);
        ShadowBuilder shadowBuilder = Robolectric.shadowOf(builder);
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

    @Implements(Builder.class)
    public static class ShadowBuilder {
        @RealObject private Builder realBuilder;
        private ContentProviderOperation contentProviderOperation;
        private ShadowContentProviderOperation shadowContentProviderOperation;

        public ShadowBuilder() {
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
}
