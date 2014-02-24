package org.robolectric.tester.android.content;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestSharedPreferences implements SharedPreferences {

  public Map<String, Map<String, Object>> content;
  protected String filename;
  public int mode;

  private ArrayList<OnSharedPreferenceChangeListener> listeners;

  public TestSharedPreferences(Map<String, Map<String, Object>> content,
      String name, int mode) {
    this.content = content;
    this.filename = name;
    this.mode = mode;
    if (!content.containsKey(name)) {
      content.put(name, new HashMap<String, Object>());
    }

    listeners = new ArrayList<OnSharedPreferenceChangeListener>();
  }

  @Override
  public Map<String, ?> getAll() {
    return new HashMap<String, Object>(content.get(filename));
  }

  @Override
  public String getString(String key, String defValue) {
    return (String) getValue(key, defValue);
  }

  private Object getValue(String key, Object defValue) {
    Map<String, Object> fileHash = content.get(filename);
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
  public void registerOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    if(!listeners.contains(listener))
      listeners.add(listener);
  }

  @Override
  public void unregisterOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    if(listeners.contains(listener))
      listeners.remove(listener);
  }

  public boolean hasListener(OnSharedPreferenceChangeListener listener) {
    return listeners.contains(listener);
  }

  private class TestSharedPreferencesEditor implements Editor {

    Map<String, Object> editsThatNeedCommit = new HashMap<String, Object>();
    Set<String> editsThatNeedRemove = new HashSet<String>();
    private boolean shouldClearOnCommit = false;

    @Override
    public Editor putString(String key, String value) {
      if (value == null) {
        editsThatNeedRemove.add(key);
      } else {
        editsThatNeedCommit.put(key, value);
        editsThatNeedRemove.remove(key);
      }
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
    public Editor putStringSet(String key, Set<String> value ){
      if (value == null) {
        editsThatNeedRemove.add(key);
      } else {
        editsThatNeedCommit.put(key, value);
        editsThatNeedRemove.remove(key);
      }
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
      List<String> keysToPassToListeners = new ArrayList<String>();

      if (shouldClearOnCommit) {
        previousContent.clear();
      } else {
        for (String key : editsThatNeedRemove) {
          previousContent.remove(key);
          keysToPassToListeners.add(key);
        }
      }

      for (String key : editsThatNeedCommit.keySet()) {
        if (!editsThatNeedCommit.get(key).equals(previousContent.get(key))) {
          previousContent.put(key, editsThatNeedCommit.get(key));
          keysToPassToListeners.add(key);
        }
      }

      for (OnSharedPreferenceChangeListener listener : listeners) {
        for (String key : keysToPassToListeners) {
          listener.onSharedPreferenceChanged(TestSharedPreferences.this, key);
        }
      }

      return true;
    }

    @Override
    public void apply() {
      commit();
    }
  }

  @Override
  public Set< String > getStringSet( String key, Set< String > defValues ) {
    Set< String > v = ( Set< String > ) getValue( key, defValues );
    return v != null ? v : defValues;
  }
}
