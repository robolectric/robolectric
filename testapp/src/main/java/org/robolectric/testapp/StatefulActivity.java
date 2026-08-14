package org.robolectric.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;

/** Activity for testing complex state preservation across configuration changes. */
public class StatefulActivity extends Activity {

  /** Custom Parcelable object for testing. */
  public static class UserData implements Parcelable {
    public String name;
    public int age;
    public String email;

    public UserData() {}

    public UserData(String name, int age, String email) {
      this.name = name;
      this.age = age;
      this.email = email;
    }

    protected UserData(Parcel in) {
      name = in.readString();
      age = in.readInt();
      email = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(name);
      dest.writeInt(age);
      dest.writeString(email);
    }

    @Override
    public int describeContents() {
      return 0;
    }

    public static final Creator<UserData> CREATOR =
        new Creator<UserData>() {
          @Override
          public UserData createFromParcel(Parcel in) {
            return new UserData(in);
          }

          @Override
          public UserData[] newArray(int size) {
            return new UserData[size];
          }
        };
  }

  /** Custom Serializable object for testing. */
  public static class AppSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean notificationsEnabled;
    public String theme;
    public int fontSize;

    public AppSettings() {}

    public AppSettings(boolean notificationsEnabled, String theme, int fontSize) {
      this.notificationsEnabled = notificationsEnabled;
      this.theme = theme;
      this.fontSize = fontSize;
    }
  }

  // State fields
  public int recreationCount = 0;
  public String simpleString;
  public int simpleInt;
  public boolean simpleBoolean;
  public UserData userData;
  public AppSettings appSettings;
  public ArrayList<String> stringList;
  public int[] intArray;

  private static final String KEY_RECREATION_COUNT = "recreation_count";
  private static final String KEY_SIMPLE_STRING = "simple_string";
  private static final String KEY_SIMPLE_INT = "simple_int";
  private static final String KEY_SIMPLE_BOOLEAN = "simple_boolean";
  private static final String KEY_USER_DATA = "user_data";
  private static final String KEY_APP_SETTINGS = "app_settings";
  private static final String KEY_STRING_LIST = "string_list";
  private static final String KEY_INT_ARRAY = "int_array";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int layoutId = getResources().getIdentifier("activity_stateful", "layout", getPackageName());
    setContentView(layoutId);

    if (savedInstanceState != null) {
      restoreState(savedInstanceState);
    } else {
      // Initialize with default values
      recreationCount = 0;
      simpleString = "";
      simpleInt = 0;
      simpleBoolean = false;
      userData = null;
      appSettings = null;
      stringList = new ArrayList<>();
      intArray = new int[0];
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(KEY_RECREATION_COUNT, recreationCount + 1);
    outState.putString(KEY_SIMPLE_STRING, simpleString);
    outState.putInt(KEY_SIMPLE_INT, simpleInt);
    outState.putBoolean(KEY_SIMPLE_BOOLEAN, simpleBoolean);
    outState.putParcelable(KEY_USER_DATA, userData);
    outState.putSerializable(KEY_APP_SETTINGS, appSettings);
    outState.putStringArrayList(KEY_STRING_LIST, stringList);
    outState.putIntArray(KEY_INT_ARRAY, intArray);
  }

  private void restoreState(Bundle savedInstanceState) {
    recreationCount = savedInstanceState.getInt(KEY_RECREATION_COUNT, 0);
    simpleString = savedInstanceState.getString(KEY_SIMPLE_STRING);
    simpleInt = savedInstanceState.getInt(KEY_SIMPLE_INT);
    simpleBoolean = savedInstanceState.getBoolean(KEY_SIMPLE_BOOLEAN);
    userData = savedInstanceState.getParcelable(KEY_USER_DATA);
    appSettings = (AppSettings) savedInstanceState.getSerializable(KEY_APP_SETTINGS);
    stringList = savedInstanceState.getStringArrayList(KEY_STRING_LIST);
    intArray = savedInstanceState.getIntArray(KEY_INT_ARRAY);
  }

  /** Set all state values for testing. */
  public void setAllState(
      String str,
      int integer,
      boolean bool,
      UserData user,
      AppSettings settings,
      ArrayList<String> list,
      int[] array) {
    this.simpleString = str;
    this.simpleInt = integer;
    this.simpleBoolean = bool;
    this.userData = user;
    this.appSettings = settings;
    this.stringList = list;
    this.intArray = array;
  }
}
