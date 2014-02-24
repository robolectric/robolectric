package org.robolectric.res;

import android.content.pm.ActivityInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityData {
  private static final String ALLOW_TASK_REPARENTING = "allowTaskReparenting";
  private static final String ALWAYS_RETAIN_TASK_STATE = "alwaysRetainTaskState";
  private static final String CLEAR_TASK_ON_LAUNCH = "clearTaskOnLaunch";
  private static final String CONFIG_CHANGES = "configChanges";
  private static final String ENABLED = "enabled";
  private static final String EXCLUDE_FROM_RECENTS = "excludeFromRecents";
  private static final String EXPORTED = "exported";
  private static final String FINISH_ON_TASK_LAUNCH = "finishOnTaskLaunch";
  private static final String HARDWARE_ACCELERATED = "hardwareAccelerated";
  private static final String ICON = "icon";
  private static final String LABEL = "label";
  private static final String LAUNCH_MODE = "launchMode";
  private static final String MULTIPROCESS = "multiprocess";
  private static final String NAME = "name";
  private static final String NO_HISTORY = "noHistory";
  private static final String PARENT_ACTIVITY_NAME = "parentActivityName";
  private static final String PERMISSION = "permission";
  private static final String PROCESS = "process";
  private static final String SCREEN_ORIENTATION = "screenOrientation";
  private static final String STATE_NOT_NEEDED = "stateNotNeeded";
  private static final String TASK_AFFINITY = "taskAffinity";
  private static final String THEME = "theme";
  private static final String UI_OPTIONS = "uiOptions";
  private static final String WINDOW_SOFT_INPUT_MODE = "windowSoftInputMode";

  private final List<IntentFilterData> intentFilters;
  private final HashMap<String, String> attrs;

  /**
   * XML Namespace used for android.
   */
  private final String xmlns;

  public ActivityData(Map<String, String> attrMap, List<IntentFilterData> intentFilters) {
    this("android", attrMap, intentFilters);
  }

  public ActivityData(String xmlns, Map<String, String> attrMap, List<IntentFilterData> intentFilters) {
    this.xmlns = xmlns;
    attrs = new HashMap<String,String>();
    attrs.putAll(attrMap);
    this.intentFilters = new ArrayList<IntentFilterData>(intentFilters);
  }

  public boolean isAllowTaskReparenting() {
    return getBooleanAttr(withXMLNS(ALLOW_TASK_REPARENTING), false);
  }

  public boolean isAlwaysRetainTaskState() {
    return getBooleanAttr(withXMLNS(ALWAYS_RETAIN_TASK_STATE), false);
  }

  public boolean isClearTaskOnLaungh() {
    return getBooleanAttr(withXMLNS(CLEAR_TASK_ON_LAUNCH), false);
  }

  /**
   * @return Bit mask with the {@link android.content.pm.PackageManager} configuration changes flags.
   */
  public int getConfigChanges() {
    final String v = attrs.get(withXMLNS(CONFIG_CHANGES));
    final String[] options = {"mcc", "mnc", "locale",
      "touchscreen", "keyboard", "keyboardHidden",
      "navigation", "screenLayout", "fontScale",
      "uiMode", "orientation", "screenSize",
      "smallestScreenSize"};
    final int[] flags = {
      ActivityInfo.CONFIG_MCC, ActivityInfo.CONFIG_MNC, ActivityInfo.CONFIG_LOCALE,
        ActivityInfo.CONFIG_TOUCHSCREEN, ActivityInfo.CONFIG_KEYBOARD, ActivityInfo.CONFIG_KEYBOARD_HIDDEN,
        ActivityInfo.CONFIG_NAVIGATION, ActivityInfo.CONFIG_SCREEN_LAYOUT, ActivityInfo.CONFIG_FONT_SCALE,
        ActivityInfo.CONFIG_UI_MODE, ActivityInfo.CONFIG_ORIENTATION, ActivityInfo.CONFIG_SCREEN_SIZE,
        ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE
    };
    return decodeOptions(v, options, flags);
  }

  public boolean isEnabled() {
    return getBooleanAttr(withXMLNS(ENABLED), true);
  }

  public boolean isExcludedFromRecents() {
    return getBooleanAttr(withXMLNS(EXCLUDE_FROM_RECENTS), false);
  }

  public boolean isExported() {
    boolean defaultValue = false;
    return getBooleanAttr(withXMLNS(EXPORTED), defaultValue);
  }

  public boolean isFinishOnTaskLaunch() {
    return getBooleanAttr(withXMLNS(FINISH_ON_TASK_LAUNCH), false);
  }

  public boolean isHardwareAccelerated() {
    return getBooleanAttr(withXMLNS(HARDWARE_ACCELERATED), false);
  }

  /* TODO: public boolean getIcon() {} */

  public String getLabel() {
    return attrs.get(withXMLNS(LABEL));
  }

  public String getLaunchMode() {
    return attrs.get(withXMLNS(LAUNCH_MODE));
  }

  public boolean isMultiprocess() {
    return getBooleanAttr(withXMLNS(MULTIPROCESS), false);
  }

  public String getName() {
    return attrs.get(withXMLNS(NAME));
  }

  public boolean isNoHistory() {
    return getBooleanAttr(withXMLNS(NO_HISTORY), false);
  }

  public String getParentActivityName() {
    return attrs.get(withXMLNS(PARENT_ACTIVITY_NAME));
  }

  public String getPermission() {
    return attrs.get(withXMLNS(PERMISSION));
  }

  public String getProcess() {
    return attrs.get(withXMLNS(PROCESS));
  }

  public String getScreenOrientation() {
    return attrs.get(withXMLNS(SCREEN_ORIENTATION));
  }

  public boolean isStateNotNeeded() {
    return getBooleanAttr(withXMLNS(STATE_NOT_NEEDED), false);
  }

  public String getTaskAffinity() {
    return attrs.get(withXMLNS(TASK_AFFINITY));
  }

  /**
   * Convenience accessor for value of android:THEME attribute.
   */
  public String getThemeRef() {
    return attrs.get(withXMLNS(THEME));
  }

  public String getUIOptions() {
    return attrs.get(withXMLNS(UI_OPTIONS));
  }

  public String getWindowSoftInputMode() {
    return attrs.get(withXMLNS(WINDOW_SOFT_INPUT_MODE));
  }

  private boolean getBooleanAttr(String n, boolean defaultValue) {
    return (attrs.containsKey(n) ? Boolean.parseBoolean(attrs.get(n)): defaultValue);
  }

  private String withXMLNS(String attr) {
    return withXMLNS(xmlns, attr);
  }

  /**
   * Convert a string like 'mcc|keyboard|touchscreen' into the
   * correct integer.
   * @param v String to decode
   * @param possibleFlags Valid labels
   * @param flagValues flags to or together for the result. Must be the same length as possibleFlags
   * @return bitwise or of all the found flags
   */
  private int decodeOptions(String v, String[] possibleFlags, int[] flagValues) {
    int res = 0;
    //quick sanity check.
    if (v == null || "".equals(v)) {
      return res;
    }
    String[] pieces = v.split("\\|");
    for(String s : pieces) {
      s = s.trim();
      for(int i = 0; i < possibleFlags.length; i++) {
        if (s.equals(possibleFlags[i])) {
          res |= flagValues[i];
          break;
        }
      }
    }
    return res;
  }

  /**
   * Get the map for all attributes defined for the activity XML.
   * @return map of attributes names to values from the manifest. Not null.
   */
  public Map<String, String> getAllAttributes() {
    return attrs;
  }

  /**
   * Get the intent filters defined for activity.
   * @return A list of intent filters. Not null.
   */
  public List<IntentFilterData> getIntentFilters() {
    return intentFilters;
  }

  private static String withXMLNS(String xmlns, String attr) {
    return String.format("%s:%s", xmlns, attr);
  }

  public static String getNameAttr(String xmlns) {
    return withXMLNS(xmlns, NAME);
  }
}
