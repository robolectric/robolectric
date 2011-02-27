/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (C) 2011 Eric Bowman
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

import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Opaque, immutable representation of a signature associated with an
 * application package.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Signature.class)
public class ShadowSignature {
    private byte[] mSignature;
    private int mHashCode;
    private boolean mHaveHashCode;
    private String mString;
    @RealObject private Signature This;

    public void __constructor__(String text) {
        final int N = text.length()/2;
        byte[] sig = new byte[N];
        for (int i=0; i<N; i++) {
            char c = text.charAt(i*2);
            byte b = (byte)(
                    (c >= 'a' ? (c - 'a' + 10) : (c - '0'))<<4);
            c = text.charAt(i*2 + 1);
            b |= (byte)(c >= 'a' ? (c - 'a' + 10) : (c - '0'));
            sig[i] = b;
        }
        mSignature = sig;
    }

    public void __constructor__(byte[] sig) {
        mSignature = sig.clone();
    }

    /**
     * Encode the Signature as ASCII text.
     */
    @Implementation
    public char[] toChars() {
        return toChars(null, null);
    }

    /**
     * Encode the Signature as ASCII text in to an existing array.
     *
     * @param existingArray Existing char array or null.
     * @param outLen Output parameter for the number of characters written in
     * to the array.
     * @return Returns either <var>existingArray</var> if it was large enough
     * to hold the ASCII representation, or a newly created char[] array if
     * needed.
     */
    @Implementation
    public char[] toChars(char[] existingArray, int[] outLen) {
        byte[] sig = mSignature;
        final int N = sig.length;
        final int N2 = N*2;
        char[] text = existingArray == null || N2 > existingArray.length
                ? new char[N2] : existingArray;
        for (int j=0; j<N; j++) {
            byte v = sig[j];
            int d = (v>>4)&0xf;
            text[j*2] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
            d = v&0xf;
            text[j*2+1] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));
        }
        if (outLen != null) outLen[0] = N;
        return text;
    }

    /**
     * Return the result of {@link #toChars()} as a String.  This result is
     * cached so future calls will return the same String.
     */
    @Implementation
    public String toCharsString() {
        if (mString != null) return mString;
        String str = new String(toChars());
        mString = str;
        return mString;
    }

    /**
     * @return the contents of this signature as a byte array.
     */
    @Implementation
    public byte[] toByteArray() {
        byte[] bytes = new byte[mSignature.length];
        System.arraycopy(mSignature, 0, bytes, 0, mSignature.length);
        return bytes;
    }

    @Override
    @Implementation
    public boolean equals(Object obj) {
        try {
            if (obj != null) {
                ShadowSignature other = Robolectric.shadowOf((Signature) obj);
                return Arrays.equals(mSignature, other.mSignature);
            }
        } catch (ClassCastException e) {
        }
        return false;
    }

    @Override
    @Implementation
    public int hashCode() {
        if (mHaveHashCode) {
            return mHashCode;
        }
        mHashCode = Arrays.hashCode(mSignature);
        mHaveHashCode = true;
        return mHashCode;
    }

    @Implementation
    public int describeContents() {
        return 0;
    }

    @Implementation
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeByteArray(mSignature);
    }

    public static final Parcelable.Creator<Signature> CREATOR
            = new Parcelable.Creator<Signature>() {
        public Signature createFromParcel(Parcel source) {
            try {
                Constructor<?>[] constructor = Signature.class.getDeclaredConstructors();
                constructor[0].setAccessible(true);
                Signature signature = (Signature) constructor[0].newInstance(new byte[0]);
                ShadowSignature shadow = Robolectric.shadowOf(signature);
                shadow.mSignature = source.createByteArray();
                return shadow.This;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Signature[] newArray(int size) {
            return new Signature[size];
        }
    };
}
