package com.xtremelabs.droidsugar.view;

import android.content.SharedPreferences;

import java.util.Hashtable;
import java.util.Map;

public class TestSharedPreferences implements SharedPreferences {

    public static final Hashtable<String, Hashtable<String, Object>> content = new Hashtable<String, Hashtable<String, Object>>();
    private String filename;
    public int mode;

    public TestSharedPreferences(String name, int mode) {
        this.filename = name;
        this.mode = mode;
        if (!content.containsKey(name)) {
            content.put(name, new Hashtable<String, Object>());
        }
    }

    @Override
    public Map<String, ?> getAll() {
        return null; // NYI
    }

    @Override
    public String getString(String key, String defValue) {
        return (String) getValue(key, defValue);
    }

    private Object getValue(String key, Object defValue) {
        Object value = content.get(filename).get(key);
        if (value == null) {
            return defValue;
        } else {
            return value;
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        return (Integer) getValue(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return (Long) getValue(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return (Float) getValue(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return (Boolean) getValue(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return content.get(filename).get(key) == null;
    }

    @Override
    public Editor edit() {
        return new FakeSharedPreferencesEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        // NYI
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        // NYI
    }

    private class FakeSharedPreferencesEditor implements Editor {

        Hashtable<String, Object> editsThatNeedCommit = new Hashtable<String, Object>();
        private boolean shouldClearOnCommit = false;

        @Override
        public Editor putString(String key, String value) {
            editsThatNeedCommit.put(key, value);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            editsThatNeedCommit.put(key, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            editsThatNeedCommit.put(key, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            editsThatNeedCommit.put(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            editsThatNeedCommit.put(key, value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            return null; // NYI
        }

        @Override
        public Editor clear() {
            shouldClearOnCommit = true;
            return this;
        }

        @Override
        public boolean commit() {
            synchronized (content) {
                Hashtable<String, Object> previousContent = content.get(filename);
                if (shouldClearOnCommit) {
                    previousContent.clear();
                } else {
                    for (String key : editsThatNeedCommit.keySet()) {
                        previousContent.put(key, editsThatNeedCommit.get(key));
                    }
                }
                return true;
            }
        }
    }
}
