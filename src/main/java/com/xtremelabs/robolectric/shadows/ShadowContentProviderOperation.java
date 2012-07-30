/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(ContentProviderOperation.class)
public class ShadowContentProviderOperation implements Parcelable {
    /** @hide exposed for unit tests */
    public final static int TYPE_INSERT = 1;
    /** @hide exposed for unit tests */
    public final static int TYPE_UPDATE = 2;
    /** @hide exposed for unit tests */
    public final static int TYPE_DELETE = 3;
    /** @hide exposed for unit tests */
    public final static int TYPE_ASSERT = 4;

    private int mType;
    private Uri mUri;
    private String mSelection;
    private String[] mSelectionArgs;
    private ContentValues mValues;
    private Integer mExpectedCount;
    private ContentValues mValuesBackReferences;
    private Map<Integer, Integer> mSelectionArgsBackReferences;
    private boolean mYieldAllowed;

    private final static String TAG = "ContentProviderOperation";

    /**
     * Creates a {@link ContentProviderOperation} by copying the contents of a
     * {@link Builder}.
     */
    public void init(ShadowContentProviderOperationBuilder shadowBuilder) {
        mType = shadowBuilder.mType;
        mUri = shadowBuilder.mUri;
        mValues = shadowBuilder.mValues;
        mSelection = shadowBuilder.mSelection;
        mSelectionArgs = shadowBuilder.mSelectionArgs;
        mExpectedCount = shadowBuilder.mExpectedCount;
        mSelectionArgsBackReferences = shadowBuilder.mSelectionArgsBackReferences;
        mValuesBackReferences = shadowBuilder.mValuesBackReferences;
        mYieldAllowed = shadowBuilder.mYieldAllowed;
    }

    public void init(Parcel source) {
        mType = source.readInt();
        mUri = Uri.CREATOR.createFromParcel(source);
        mValues = source.readInt() != 0 ? ContentValues.CREATOR.createFromParcel(source) : null;
        mSelection = source.readInt() != 0 ? source.readString() : null;
        mSelectionArgs = source.readInt() != 0 ? source.createStringArray() : null;
        mExpectedCount = source.readInt() != 0 ? source.readInt() : null;
        mValuesBackReferences = source.readInt() != 0
                ? ContentValues.CREATOR.createFromParcel(source)
                : null;
        mSelectionArgsBackReferences = source.readInt() != 0
                ? new HashMap<Integer, Integer>()
                : null;
        if (mSelectionArgsBackReferences != null) {
            final int count = source.readInt();
            for (int i = 0; i < count; i++) {
                mSelectionArgsBackReferences.put(source.readInt(), source.readInt());
            }
        }
        mYieldAllowed = source.readInt() != 0;
    }

    @Implementation
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
        Uri.writeToParcel(dest, mUri);
        if (mValues != null) {
            dest.writeInt(1);
            mValues.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (mSelection != null) {
            dest.writeInt(1);
            dest.writeString(mSelection);
        } else {
            dest.writeInt(0);
        }
        if (mSelectionArgs != null) {
            dest.writeInt(1);
            dest.writeStringArray(mSelectionArgs);
        } else {
            dest.writeInt(0);
        }
        if (mExpectedCount != null) {
            dest.writeInt(1);
            dest.writeInt(mExpectedCount);
        } else {
            dest.writeInt(0);
        }
        if (mValuesBackReferences != null) {
            dest.writeInt(1);
            mValuesBackReferences.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (mSelectionArgsBackReferences != null) {
            dest.writeInt(1);
            dest.writeInt(mSelectionArgsBackReferences.size());
            for (Map.Entry<Integer, Integer> entry : mSelectionArgsBackReferences.entrySet()) {
                dest.writeInt(entry.getKey());
                dest.writeInt(entry.getValue());
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(mYieldAllowed ? 1 : 0);
    }

    /**
     * Create a {@link Builder} suitable for building an insert {@link ContentProviderOperation}.
     * @param uri The {@link Uri} that is the target of the insert.
     * @return a {@link Builder}
     */
    @Implementation
    public static Builder newInsert(Uri uri) {
        return newInstanceOfBuilder(TYPE_INSERT, uri);
    }

    /**
     * Create a {@link Builder} suitable for building an update {@link ContentProviderOperation}.
     * @param uri The {@link Uri} that is the target of the update.
     * @return a {@link Builder}
     */
    @Implementation
    public static Builder newUpdate(Uri uri) {
        return newInstanceOfBuilder(TYPE_UPDATE, uri);
    }

    /**
     * Create a {@link Builder} suitable for building a delete {@link ContentProviderOperation}.
     * @param uri The {@link Uri} that is the target of the delete.
     * @return a {@link Builder}
     */
    @Implementation
    public static Builder newDelete(Uri uri) {
        return newInstanceOfBuilder(TYPE_DELETE, uri);
    }

    /**
     * Create a {@link Builder} suitable for building a
     * {@link ContentProviderOperation} to assert a set of values as provided
     * through {@link Builder#withValues(ContentValues)}.
     */
    @Implementation
    public static Builder newAssertQuery(Uri uri) {
        return newInstanceOfBuilder(TYPE_ASSERT, uri);
    }

    private static Builder newInstanceOfBuilder(int type, Uri uri) {
        Builder builder = newInstanceOf(Builder.class);
        Robolectric.shadowOf(builder).init(type, uri);
        return builder;
    }

    @Implementation
    public Uri getUri() {
        return mUri;
    }

    @Implementation
    public boolean isYieldAllowed() {
        return mYieldAllowed;
    }

    /** @hide exposed for unit tests */
    public int getType() {
        return mType;
    }

    @Implementation
    public boolean isWriteOperation() {
        return mType == TYPE_DELETE || mType == TYPE_INSERT || mType == TYPE_UPDATE;
    }

    @Implementation
    public boolean isReadOperation() {
        return mType == TYPE_ASSERT;
    }

    /**
     * Applies this operation using the given provider. The backRefs array is used to resolve any
     * back references that were requested using
     * {@link Builder#withValueBackReferences(ContentValues)} and
     * {@link Builder#withSelectionBackReference}.
     * @param provider the {@link ContentProvider} on which this batch is applied
     * @param backRefs a {@link ContentProviderResult} array that will be consulted
     * to resolve any requested back references.
     * @param numBackRefs the number of valid results on the backRefs array.
     * @return a {@link ContentProviderResult} that contains either the {@link Uri} of the inserted
     * row if this was an insert otherwise the number of rows affected.
     * @throws OperationApplicationException thrown if either the insert fails or
     * if the number of rows affected didn't match the expected count
     */
    @Implementation
    public ContentProviderResult apply(ContentProvider provider, ContentProviderResult[] backRefs,
                                       int numBackRefs) throws OperationApplicationException {
        ContentValues values = resolveValueBackReferences(backRefs, numBackRefs);
        String[] selectionArgs =
                resolveSelectionArgsBackReferences(backRefs, numBackRefs);

        if (mType == TYPE_INSERT) {
            Uri newUri = provider.insert(mUri, values);
            if (newUri == null) {
                throw new OperationApplicationException("insert failed");
            }
            return new ContentProviderResult(newUri);
        }

        int numRows;
        if (mType == TYPE_DELETE) {
            numRows = provider.delete(mUri, mSelection, selectionArgs);
        } else if (mType == TYPE_UPDATE) {
            numRows = provider.update(mUri, values, mSelection, selectionArgs);
        } else if (mType == TYPE_ASSERT) {
            // Assert that all rows match expected values
            String[] projection =  null;
            if (values != null) {
                // Build projection map from expected values
                final ArrayList<String> projectionList = new ArrayList<String>();
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    projectionList.add(entry.getKey());
                }
                projection = projectionList.toArray(new String[projectionList.size()]);
            }
            final Cursor cursor = provider.query(mUri, projection, mSelection, selectionArgs, null);
            try {
                numRows = cursor.getCount();
                if (projection != null) {
                    while (cursor.moveToNext()) {
                        for (int i = 0; i < projection.length; i++) {
                            final String cursorValue = cursor.getString(i);
                            final String expectedValue = values.getAsString(projection[i]);
                            if (!TextUtils.equals(cursorValue, expectedValue)) {
                                // Throw exception when expected values don't match
                                Log.e(TAG, this.toString());
                                throw new OperationApplicationException("Found value " + cursorValue
                                        + " when expected " + expectedValue + " for column "
                                        + projection[i]);
                            }
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.e(TAG, this.toString());
            throw new IllegalStateException("bad type, " + mType);
        }

        if (mExpectedCount != null && mExpectedCount != numRows) {
            Log.e(TAG, this.toString());
            throw new OperationApplicationException("wrong number of rows: " + numRows);
        }

        return new ContentProviderResult(numRows);
    }

    /**
     * The ContentValues back references are represented as a ContentValues object where the
     * key refers to a column and the value is an index of the back reference whose
     * valued should be associated with the column.
     * <p>
     * This is intended to be a private method but it is exposed for
     * unit testing purposes
     * @param backRefs an array of previous results
     * @param numBackRefs the number of valid previous results in backRefs
     * @return the ContentValues that should be used in this operation application after
     * expansion of back references. This can be called if either mValues or mValuesBackReferences
     * is null
     */
    @Implementation
    public ContentValues resolveValueBackReferences(
            ContentProviderResult[] backRefs, int numBackRefs) {
        if (mValuesBackReferences == null) {
            return mValues;
        }
        final ContentValues values;
        if (mValues == null) {
            values = new ContentValues();
        } else {
            values = new ContentValues(mValues);
        }
        for (Map.Entry<String, Object> entry : mValuesBackReferences.valueSet()) {
            String key = entry.getKey();
            Integer backRefIndex = mValuesBackReferences.getAsInteger(key);
            if (backRefIndex == null) {
                Log.e(TAG, this.toString());
                throw new IllegalArgumentException("values backref " + key + " is not an integer");
            }
            values.put(key, backRefToValue(backRefs, numBackRefs, backRefIndex));
        }
        return values;
    }

    /**
     * The Selection Arguments back references are represented as a Map of Integer->Integer where
     * the key is an index into the selection argument array (see {@link Builder#withSelection})
     * and the value is the index of the previous result that should be used for that selection
     * argument array slot.
     * <p>
     * This is intended to be a private method but it is exposed for
     * unit testing purposes
     * @param backRefs an array of previous results
     * @param numBackRefs the number of valid previous results in backRefs
     * @return the ContentValues that should be used in this operation application after
     * expansion of back references. This can be called if either mValues or mValuesBackReferences
     * is null
     */
    @Implementation
    public String[] resolveSelectionArgsBackReferences(
            ContentProviderResult[] backRefs, int numBackRefs) {
        if (mSelectionArgsBackReferences == null) {
            return mSelectionArgs;
        }
        String[] newArgs = new String[mSelectionArgs.length];
        System.arraycopy(mSelectionArgs, 0, newArgs, 0, mSelectionArgs.length);
        for (Map.Entry<Integer, Integer> selectionArgBackRef
                : mSelectionArgsBackReferences.entrySet()) {
            final Integer selectionArgIndex = selectionArgBackRef.getKey();
            final int backRefIndex = selectionArgBackRef.getValue();
            newArgs[selectionArgIndex] =
                    String.valueOf(backRefToValue(backRefs, numBackRefs, backRefIndex));
        }
        return newArgs;
    }

    @Implementation
    @Override
    public String toString() {
        return "mType: " + mType + ", mUri: " + mUri +
                ", mSelection: " + mSelection +
                ", mExpectedCount: " + mExpectedCount +
                ", mYieldAllowed: " + mYieldAllowed +
                ", mValues: " + mValues +
                ", mValuesBackReferences: " + mValuesBackReferences +
                ", mSelectionArgsBackReferences: " + mSelectionArgsBackReferences;
    }

    /**
     * Return the string representation of the requested back reference.
     * @param backRefs an array of results
     * @param numBackRefs the number of items in the backRefs array that are valid
     * @param backRefIndex which backRef to be used
     * @throws ArrayIndexOutOfBoundsException thrown if the backRefIndex is larger than
     * the numBackRefs
     * @return the string representation of the requested back reference.
     */
    private long backRefToValue(ContentProviderResult[] backRefs, int numBackRefs,
                                Integer backRefIndex) {
        if (backRefIndex >= numBackRefs) {
            Log.e(TAG, this.toString());
            throw new ArrayIndexOutOfBoundsException("asked for back ref " + backRefIndex
                    + " but there are only " + numBackRefs + " back refs");
        }
        ContentProviderResult backRef = backRefs[backRefIndex];
        long backRefValue;
        if (backRef.uri != null) {
            backRefValue = ContentUris.parseId(backRef.uri);
        } else {
            backRefValue = backRef.count;
        }
        return backRefValue;
    }

    @Implementation
    public int describeContents() {
        return 0;
    }

    public static final Creator<ContentProviderOperation> CREATOR =
            new Creator<ContentProviderOperation>() {
                public ContentProviderOperation createFromParcel(Parcel source) {
                    ContentProviderOperation op = newInstanceOf(ContentProviderOperation.class);
                    shadowOf(op).init(source);
                    return op;
                }

                public ContentProviderOperation[] newArray(int size) {
                    return new ContentProviderOperation[size];
                }
            };

    public boolean isInsert() {
        return mType == TYPE_INSERT;
    }

    public boolean isUpdate() {
        return mType == TYPE_UPDATE;
    }

    public boolean isDelete() {
        return mType == TYPE_DELETE;
    }

    public ContentValues getValues() {
        return mValues;
    }

    public String getSelection() {
        return mSelection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }
}
