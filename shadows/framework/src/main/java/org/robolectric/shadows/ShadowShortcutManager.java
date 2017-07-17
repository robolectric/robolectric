package org.robolectric.shadows;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** */
@Implements(value = ShortcutManager.class, minSdk = Build.VERSION_CODES.N_MR1)
public class ShadowShortcutManager {

  private static final int MAX_ICON_DIMENSION = 128;
  private static final int MAX_NUM_SHORTCUTS_PER_ACTIVITY = 16;

  private final Map<String, ShortcutInfo> dynamicShortcuts = new HashMap<>();
  private final Map<String, ShortcutInfo> activePinnedShortcuts = new HashMap<>();
  private final Map<String, ShortcutInfo> disabledPinnedShortcuts = new HashMap<>();

  private boolean isRequestPinShortcutSupported = true;

  @Implementation
  public boolean addDynamicShortcuts(List<ShortcutInfo> shortcutInfoList) {
    for (ShortcutInfo shortcutInfo : shortcutInfoList) {
      if (activePinnedShortcuts.containsKey(shortcutInfo.getId())) {
        ShortcutInfo previousShortcut = activePinnedShortcuts.get(shortcutInfo.getId());
        if (!previousShortcut.isImmutable()) {
          activePinnedShortcuts.put(shortcutInfo.getId(), shortcutInfo);
        }
      } else if (disabledPinnedShortcuts.containsKey(shortcutInfo.getId())) {
        ShortcutInfo previousShortcut = disabledPinnedShortcuts.get(shortcutInfo.getId());
        if (!previousShortcut.isImmutable()) {
          disabledPinnedShortcuts.put(shortcutInfo.getId(), shortcutInfo);
        }
      } else if (dynamicShortcuts.containsKey(shortcutInfo.getId())) {
        ShortcutInfo previousShortcut = dynamicShortcuts.get(shortcutInfo.getId());
        if (!previousShortcut.isImmutable()) {
          dynamicShortcuts.put(shortcutInfo.getId(), shortcutInfo);
        }
      } else {
        dynamicShortcuts.put(shortcutInfo.getId(), shortcutInfo);
      }
    }
    return true;
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public Intent createShortcutResultIntent(ShortcutInfo shortcut) {
    if (disabledPinnedShortcuts.containsKey(shortcut.getId())) {
      throw new IllegalArgumentException();
    }
    return new Intent();
  }

  @Implementation
  public void disableShortcuts(List<String> shortcutIds) {
    disableShortcuts(shortcutIds, "Shortcut is disabled.");
  }

  @Implementation
  public void disableShortcuts(List<String> shortcutIds, CharSequence unused) {
    for (String shortcutId : shortcutIds) {
      ShortcutInfo shortcut = activePinnedShortcuts.remove(shortcutId);
      if (shortcut != null) {
        disabledPinnedShortcuts.put(shortcutId, shortcut);
      }
    }
  }

  @Implementation
  public void enableShortcuts(List<String> shortcutIds) {
    for (String shortcutId : shortcutIds) {
      ShortcutInfo shortcut = disabledPinnedShortcuts.remove(shortcutId);
      if (shortcut != null) {
        activePinnedShortcuts.put(shortcutId, shortcut);
      }
    }
  }

  @Implementation
  public List<ShortcutInfo> getDynamicShortcuts() {
    return ImmutableList.copyOf(dynamicShortcuts.values());
  }

  @Implementation
  public int getIconMaxHeight() {
    return MAX_ICON_DIMENSION;
  }

  @Implementation
  public int getIconMaxWidth() {
    return MAX_ICON_DIMENSION;
  }

  @Implementation
  public List<ShortcutInfo> getManifestShortcuts() {
    return ImmutableList.of();
  }

  @Implementation
  public int getMaxShortcutCountPerActivity() {
    return MAX_NUM_SHORTCUTS_PER_ACTIVITY;
  }

  @Implementation
  public List<ShortcutInfo> getPinnedShortcuts() {
    ImmutableList.Builder<ShortcutInfo> pinnedShortcuts = ImmutableList.builder();
    pinnedShortcuts.addAll(activePinnedShortcuts.values());
    pinnedShortcuts.addAll(disabledPinnedShortcuts.values());
    return pinnedShortcuts.build();
  }

  @Implementation
  public boolean isRateLimitingActive() {
    return false;
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public boolean isRequestPinShortcutSupported() {
    return isRequestPinShortcutSupported;
  }

  public void setIsRequestPinShortcutSupported(boolean isRequestPinShortcutSupported) {
    this.isRequestPinShortcutSupported = isRequestPinShortcutSupported;
  }

  @Implementation
  public void removeAllDynamicShortcuts() {
    dynamicShortcuts.clear();
  }

  @Implementation
  public void removeDynamicShortcuts(List<String> shortcutIds) {
    for (String shortcutId : shortcutIds) {
      dynamicShortcuts.remove(shortcutId);
    }
  }

  @Implementation
  public void reportShortcutUsed(String shortcutId) {}

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public boolean requestPinShortcut(ShortcutInfo shortcut, IntentSender resultIntent) {
    if (disabledPinnedShortcuts.containsKey(shortcut.getId())) {
      throw new IllegalArgumentException(
          "Shortcut with ID [" + shortcut.getId() + "] already exists and is disabled.");
    }
    if (dynamicShortcuts.containsKey(shortcut.getId())) {
      activePinnedShortcuts.put(shortcut.getId(), dynamicShortcuts.remove(shortcut.getId()));
    } else {
      activePinnedShortcuts.put(shortcut.getId(), shortcut);
    }
    if (resultIntent != null) {
      try {
        resultIntent.sendIntent(RuntimeEnvironment.application, 0, null, null, null);
      } catch (SendIntentException e) {
        throw new IllegalStateException();
      }
    }
    return true;
  }

  @Implementation
  public boolean setDynamicShortcuts(List<ShortcutInfo> shortcutInfoList) {
    dynamicShortcuts.clear();
    return addDynamicShortcuts(shortcutInfoList);
  }

  @Implementation
  public boolean updateShortcuts(List<ShortcutInfo> shortcutInfoList) {
    List<ShortcutInfo> existingShortcutsToUpdate = new ArrayList<>();
    for (ShortcutInfo shortcutInfo : shortcutInfoList) {
      if (dynamicShortcuts.containsKey(shortcutInfo.getId())
          || activePinnedShortcuts.containsKey(shortcutInfo.getId())
          || disabledPinnedShortcuts.containsKey(shortcutInfo.getId())) {
        existingShortcutsToUpdate.add(shortcutInfo);
      }
    }
    return addDynamicShortcuts(existingShortcutsToUpdate);
  }
}
