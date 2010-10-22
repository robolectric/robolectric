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

package com.xtremelabs.robolectric.fakes;

import android.content.ContentValues;
import android.util.Log;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.SheepWrangler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContentValues.class)
public final class ShadowContentValues {
    @SheepWrangler private ProxyDelegatingHandler proxyDelegatingHandler;
    private HashMap<String, Object> values = new HashMap<String, Object>();
    private static final String TAG = "ShadowContentValues";

    public void __constructor__(ContentValues from) {
        values = new HashMap<String, Object>(shadowFor(from).values);
    }

    private void __constructor__(HashMap<String, Object> values) {
        this.values = values;
    }

    @Implementation
    public void put(String key, String value) {
        values.put(key, value);
    }

    @Implementation
    public void putAll(ContentValues other) {
        values.putAll(shadowFor(other).values);
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

    @Implementation
    @Deprecated
    public void putStringArrayList(String key, ArrayList<String> value) {
        values.put(key, value);
    }

    @Implementation
    @SuppressWarnings("unchecked")
    @Deprecated
    public ArrayList<String> getStringArrayList(String key) {
        return (ArrayList<String>) values.get(key);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ContentValues)) {
            return false;
        }
        return values.equals(shadowFor((ContentValues) object).values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : values.keySet()) {
            String value = getAsString(name);
            if (sb.length() > 0) sb.append(" ");
            sb.append(name + "=" + value);
        }
        return sb.toString();
    }

    private ShadowContentValues shadowFor(ContentValues other) {
        return ((ShadowContentValues) proxyDelegatingHandler.shadowFor(other));
    }
}
