package com.xtremelabs.robolectric.content;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class TestSharedPreferences implements SharedPreferences {

    public Map<String, Hashtable<String, Object>> content;
    private String filename;
    public int mode;

    public TestSharedPreferences(Map<String, Hashtable<String, Object>> content, String name, int mode) {
        this.content = content;
        this.filename = name;
        this.mode = mode;
        if (!content.containsKey(name)) {
            content.put(name, new Hashtable<String, Object>());
        }
    }

    @Override
    public Map<String, ?> getAll() {
        return null;
    }

    @Override
    public String getString(String key, String defValue) {
        return (String) getValue(key, defValue);
    }

    private Object getValue(String key, Object defValue) {
        Hashtable<String, Object> fileHash = content.get(filename);
        if (fileHash != null) {
            Object value = fileHash.get(key);
            if (value != null) {
                return value;
            }
        }
        return defValue;
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
        return content.get(filename).containsKey(key);
    }

    @Override
    public Editor edit() {
        return new TestSharedPreferencesEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    private class TestSharedPreferencesEditor implements Editor {

        Hashtable<String, Object> editsThatNeedCommit = new Hashtable<String, Object>();
        Set<String> editsThatNeedRemove = new HashSet<String>();
        private boolean shouldClearOnCommit = false;

        @Override
        public Editor putString(String key, String value) {
            editsThatNeedCommit.put(key, value);
            editsThatNeedRemove.remove(key);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            editsThatNeedCommit.put(key, value);
            editsThatNeedRemove.remove(key);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            editsThatNeedCommit.put(key, value);
            editsThatNeedRemove.remove(key);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            editsThatNeedCommit.put(key, value);
            editsThatNeedRemove.remove(key);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            editsThatNeedCommit.put(key, value);
            editsThatNeedRemove.remove(key);
            return this;
        }

        @Override
        public Editor remove(String key) {
            editsThatNeedRemove.add(key);
            return this;
        }

        @Override
        public Editor clear() {
            shouldClearOnCommit = true;
            return this;
        }

        @Override
        public boolean commit() {
            Map<String, Object> previousContent = content.get(filename);
            if (shouldClearOnCommit) {
                previousContent.clear();
            } else {
                for (String key : editsThatNeedCommit.keySet()) {
                    previousContent.put(key, editsThatNeedCommit.get(key));
                }
                for (String key : editsThatNeedRemove) {
                    previousContent.remove(key);
                }
            }
            return true;
        }

        @Override public void apply() {
            commit();
        }
    }
}
