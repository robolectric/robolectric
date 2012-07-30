/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xtremelabs.robolectric.shadows;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.net.Uri;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.shadows.ShadowContentProviderOperation.*;

@Implements(ContentProviderOperation.Builder.class)
public class ShadowContentProviderOperationBuilder {

    @RealObject
    private Builder realObject;

    int mType;
    Uri mUri;
    String mSelection;
    String[] mSelectionArgs;
    ContentValues mValues;
    Integer mExpectedCount;
    ContentValues mValuesBackReferences;
    Map<Integer, Integer> mSelectionArgsBackReferences;
    boolean mYieldAllowed;

    /** Create a {@link Builder} of a given type. The uri must not be null. */
    public void init(int type, Uri uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        mType = type;
        mUri = uri;
    }

    /** Create a ContentProviderOperation from this {@link Builder}. */
    @Implementation
    public ContentProviderOperation build() {
        if (mType == TYPE_UPDATE) {
            if ((mValues == null || mValues.size() == 0)
                    && (mValuesBackReferences == null || mValuesBackReferences.size() == 0)) {
                throw new IllegalArgumentException("Empty values");
            }
        }
        if (mType == TYPE_ASSERT) {
            if ((mValues == null || mValues.size() == 0)
                    && (mValuesBackReferences == null || mValuesBackReferences.size() == 0)
                    && (mExpectedCount == null)) {
                throw new IllegalArgumentException("Empty values");
            }
        }
        ContentProviderOperation op = Robolectric.newInstanceOf(ContentProviderOperation.class);
        shadowOf(op).init(this);
        return op;
    }

    /**
     * Add a {@link ContentValues} of back references. The key is the name of the column
     * and the value is an integer that is the index of the previous result whose
     * value should be used for the column. The value is added as a {@link String}.
     * A column value from the back references takes precedence over a value specified in
     * {@link #withValues}.
     * This can only be used with builders of type insert, update, or assert.
     * @return this builder, to allow for chaining.
     */
    @Implementation
    public Builder withValueBackReferences(ContentValues backReferences) {
        if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
            throw new IllegalArgumentException(
                    "only inserts, updates, and asserts can have value back-references");
        }
        mValuesBackReferences = backReferences;
        return realObject;
    }

    /**
     * Add a ContentValues back reference.
     * A column value from the back references takes precedence over a value specified in
     * {@link #withValues}.
     * This can only be used with builders of type insert, update, or assert.
     * @return this builder, to allow for chaining.
     */
    @Implementation
    public Builder withValueBackReference(String key, int previousResult) {
        if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
            throw new IllegalArgumentException(
                    "only inserts, updates, and asserts can have value back-references");
        }
        if (mValuesBackReferences == null) {
            mValuesBackReferences = new ContentValues();
        }
        mValuesBackReferences.put(key, previousResult);
        return realObject;
    }

    /**
     * Add a back references as a selection arg. Any value at that index of the selection arg
     * that was specified by {@link #withSelection} will be overwritten.
     * This can only be used with builders of type update, delete, or assert.
     * @return this builder, to allow for chaining.
     */
    @Implementation
    public Builder withSelectionBackReference(int selectionArgIndex, int previousResult) {
        if (mType != TYPE_UPDATE && mType != TYPE_DELETE && mType != TYPE_ASSERT) {
            throw new IllegalArgumentException("only updates, deletes, and asserts "
                    + "can have selection back-references");
        }
        if (mSelectionArgsBackReferences == null) {
            mSelectionArgsBackReferences = new HashMap<Integer, Integer>();
        }
        mSelectionArgsBackReferences.put(selectionArgIndex, previousResult);
        return realObject;
    }

    /**
     * The ContentValues to use. This may be null. These values may be overwritten by
     * the corresponding value specified by {@link #withValueBackReference} or by
     * future calls to {@link #withValues} or {@link #withValue}.
     * This can only be used with builders of type insert, update, or assert.
     * @return this builder, to allow for chaining.
     */
    @Implementation
    public Builder withValues(ContentValues values) {
        if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
            throw new IllegalArgumentException(
                    "only inserts, updates, and asserts can have values");
        }
        if (mValues == null) {
            mValues = new ContentValues();
        }
        mValues.putAll(values);
        return realObject;
    }

    /**
     * A value to insert or update. This value may be overwritten by
     * the corresponding value specified by {@link #withValueBackReference}.
     * This can only be used with builders of type insert, update, or assert.
     * @param key the name of this value
     * @param value the value itself. the type must be acceptable for insertion by
     * {@link ContentValues#put}
     * @return this builder, to allow for chaining.
     */
    @Implementation
    public Builder withValue(String key, Object value) {
        if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
            throw new IllegalArgumentException("only inserts and updates can have values");
        }
        if (mValues == null) {
            mValues = new ContentValues();
        }
        if (value == null) {
            mValues.putNull(key);
        } else if (value instanceof String) {
            mValues.put(key, (String) value);
        } else if (value instanceof Byte) {
            mValues.put(key, (Byte) value);
        } else if (value instanceof Short) {
            mValues.put(key, (Short) value);
        } else if (value instanceof Integer) {
            mValues.put(key, (Integer) value);
        } else if (value instanceof Long) {
            mValues.put(key, (Long) value);
        } else if (value instanceof Float) {
            mValues.put(key, (Float) value);
        } else if (value instanceof Double) {
            mValues.put(key, (Double) value);
        } else if (value instanceof Boolean) {
            mValues.put(key, (Boolean) value);
        } else if (value instanceof byte[]) {
            mValues.put(key, (byte[]) value);
        } else {
            throw new IllegalArgumentException("bad value type: " + value.getClass().getName());
        }
        return realObject;
    }

    /**
     * The selection and arguments to use. An occurrence of '?' in the selection will be
     * replaced with the corresponding occurence of the selection argument. Any of the
     * selection arguments may be overwritten by a selection argument back reference as
     * specified by {@link #withSelectionBackReference}.
     * This can only be used with builders of type update, delete, or assert.
     * @return this builder, to allow for chaining.
     */
    @Implementation
    public Builder withSelection(String selection, String[] selectionArgs) {
        if (mType != TYPE_UPDATE && mType != TYPE_DELETE && mType != TYPE_ASSERT) {
            throw new IllegalArgumentException(
                    "only updates, deletes, and asserts can have selections");
        }
        mSelection = selection;
        if (selectionArgs == null) {
            mSelectionArgs = null;
        } else {
            mSelectionArgs = new String[selectionArgs.length];
            System.arraycopy(selectionArgs, 0, mSelectionArgs, 0, selectionArgs.length);
        }
        return realObject;
    }

    /**
     * If set then if the number of rows affected by this operation do not match
     * this count {@link android.content.OperationApplicationException} will be throw.
     * This can only be used with builders of type update, delete, or assert.
     * @return this builder, to allow for chaining.
     */
    @Implementation
    public Builder withExpectedCount(int count) {
        if (mType != TYPE_UPDATE && mType != TYPE_DELETE && mType != TYPE_ASSERT) {
            throw new IllegalArgumentException(
                    "only updates, deletes, and asserts can have expected counts");
        }
        mExpectedCount = count;
        return realObject;
    }

    @Implementation
    public Builder withYieldAllowed(boolean yieldAllowed) {
        mYieldAllowed = yieldAllowed;
        return realObject;
    }

    public int getType() {
        return mType;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getSelection() {
        return mSelection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public ContentValues getValues() {
        return mValues;
    }

    public Integer getExpectedCount() {
        return mExpectedCount;
    }

    public ContentValues getValuesBackReferences() {
        return mValuesBackReferences;
    }

    public Object getValuesBackReferences(String key) {
        return mValuesBackReferences.get(key);
    }

    public Map<Integer, Integer> getSelectionArgsBackReferences() {
        return mSelectionArgsBackReferences;
    }

    public boolean isYieldAllowed() {
        return mYieldAllowed;
    }
}
