/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.ContentValues;
import android.util.Log;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

/**
 * Shadows the {@code android.content.ContentValues} class.
 * <p/>
 * This is a fancy map from String to... something. Works just like the Android class it shadows.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContentValues.class)
public final class ShadowContentValues {
    private static final String TAG = "ShadowContentValues";
    private HashMap<String, Object> values = new HashMap<String, Object>();

    public void __constructor__(ContentValues from) {
        values = new HashMap<String, Object>(shadowOf(from).values);
    }

    @Implementation
    public void put(String key, String value) {
        values.put(key, value);
    }

    @Implementation
    public void putAll(ContentValues other) {
        values.putAll(shadowOf(other).values);
    }

    @Implementation
    public void put(String key, Byte value) {
        values.put(key, value);
    }

    @Implementation
    public void put(String key, Short value) {
        values.put(key, value);
    }

    @Implementation
    public void put(String key, Integer value) {
        values.put(key, value);
    }

    @Implementation
    public void put(String key, Long value) {
        values.put(key, value);
    }

    @Implementation
    public void put(String key, Float value) {
        values.put(key, value);
    }

    @Implementation
    public void put(String key, Double value) {
        values.put(key, value);
    }

    @Implementation
    public void put(String key, Boolean value) {
        values.put(key, value);
    }

    @Implementation
    public void put(String key, byte[] value) {
        values.put(key, value);
    }

    @Implementation
    public void putNull(String key) {
        values.put(key, null);
    }

    @Implementation
    public int size() {
        return values.size();
    }

    @Implementation
    public void remove(String key) {
        values.remove(key);
    }

    @Implementation
    public void clear() {
        values.clear();
    }

    @Implementation
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    @Implementation
    public Object get(String key) {
        return values.get(key);
    }

    @Implementation
    public String getAsString(String key) {
        Object value = values.get(key);
        return value != null ? value.toString() : null;
    }

    @Implementation
    public Long getAsLong(String key) {
        Object value = values.get(key);
        try {
            return value != null ? ((Number) value).longValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Long value for " + value + " at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Long: " + value, e);
                return null;
            }
        }
    }

    @Implementation
    public Integer getAsInteger(String key) {
        Object value = values.get(key);
        try {
            return value != null ? ((Number) value).intValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Integer value for " + value + " at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Integer: " + value, e);
                return null;
            }
        }
    }

    @Implementation
    public Short getAsShort(String key) {
        Object value = values.get(key);
        try {
            return value != null ? ((Number) value).shortValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Short.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Short value for " + value + " at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Short: " + value, e);
                return null;
            }
        }
    }

    @Implementation
    public Byte getAsByte(String key) {
        Object value = values.get(key);
        try {
            return value != null ? ((Number) value).byteValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Byte.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Byte value for " + value + " at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Byte: " + value, e);
                return null;
            }
        }
    }

    @Implementation
    public Double getAsDouble(String key) {
        Object value = values.get(key);
        try {
            return value != null ? ((Number) value).doubleValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Double.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Double value for " + value + " at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Double: " + value, e);
                return null;
            }
        }
    }

    @Implementation
    public Float getAsFloat(String key) {
        Object value = values.get(key);
        try {
            return value != null ? ((Number) value).floatValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Float value for " + value + " at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Float: " + value, e);
                return null;
            }
        }
    }

    @Implementation
    public Boolean getAsBoolean(String key) {
        Object value = values.get(key);
        try {
            return (Boolean) value;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                return Boolean.valueOf(value.toString());
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Boolean: " + value, e);
                return null;
            }
        }
    }

    @Implementation
    public byte[] getAsByteArray(String key) {
        Object value = values.get(key);
        if (value instanceof byte[]) {
            return (byte[]) value;
        } else {
            return null;
        }
    }

    @Implementation
    public Set<Map.Entry<String, Object>> valueSet() {
        return values.entrySet();
    }

    @Implementation
    public int describeContents() {
        return 0;
    }

    @Override @Implementation
    public boolean equals(Object object) {
        if (object == null) return false;
        Object o = shadowOf_(object);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        if (!(o instanceof ContentValues)) {
            return false;
        }
        return values.equals(shadowOf((ContentValues) o).values);
    }

    @Override @Implementation
    public int hashCode() {
        return values.hashCode();
    }

    @Override @Implementation
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : values.keySet()) {
            String value = getAsString(name);
            if (sb.length() > 0) sb.append(" ");
            sb.append(name + "=" + value);
        }
        return sb.toString();
    }
}
