package org.robolectric.shadows;

import static android.content.pm.ShortcutManager.FLAG_MATCH_CACHED;
import static android.content.pm.ShortcutManager.FLAG_MATCH_DYNAMIC;
import static android.content.pm.ShortcutManager.FLAG_MATCH_MANIFEST;
import static android.content.pm.ShortcutManager.FLAG_MATCH_PINNED;
import static android.os.Build.VERSION_CODES.R;
import static java.util.stream.Collectors.toCollection;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** */
@Implements(value = ShortcutManager.class, minSdk = Build.VERSION_CODES.N_MR1)
public class ShadowShortcutManager {

  private static final int MAX_ICON_DIMENSION = 128;

  private final Map<String, ShortcutInfo> dynamicShortcuts = new HashMap<>();
  private final Map<String, ShortcutInfo> activePinnedShortcuts = new HashMap<>();
  private final Map<String, ShortcutInfo> disabledPinnedShortcuts = new HashMap<>();

  private List<ShortcutInfo> manifestShortcuts = ImmutableList.of();

  private boolean isRequestPinShortcutSupported = true;
  private int maxShortcutCountPerActivity = 16;
  private int maxIconHeight = MAX_ICON_DIMENSION;
  private int maxIconWidth = MAX_ICON_DIMENSION;

  @Implementation
  protected boolean addDynamicShortcuts(List<ShortcutInfo> shortcutInfoList) {
    for (ShortcutInfo shortcutInfo : shortcutInfoList) {
      shortcutInfo.addFlags(ShortcutInfo.FLAG_DYNAMIC);
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
  protected Intent createShortcutResultIntent(ShortcutInfo shortcut) {
    if (disabledPinnedShortcuts.containsKey(shortcut.getId())) {
      throw new IllegalArgumentException();
    }
    return new Intent();
  }

  @Implementation
  protected void disableShortcuts(List<String> shortcutIds) {
    disableShortcuts(shortcutIds, "Shortcut is disabled.");
  }

  @Implementation
  protected void disableShortcuts(List<String> shortcutIds, CharSequence unused) {
    for (String shortcutId : shortcutIds) {
      ShortcutInfo shortcut = activePinnedShortcuts.remove(shortcutId);
      if (shortcut != null) {
        disabledPinnedShortcuts.put(shortcutId, shortcut);
      }
    }
  }

  @Implementation
  protected void enableShortcuts(List<String> shortcutIds) {
    for (String shortcutId : shortcutIds) {
      ShortcutInfo shortcut = disabledPinnedShortcuts.remove(shortcutId);
      if (shortcut != null) {
        activePinnedShortcuts.put(shortcutId, shortcut);
      }
    }
  }

  @Implementation
  protected List<ShortcutInfo> getDynamicShortcuts() {
    return ImmutableList.copyOf(dynamicShortcuts.values());
  }

  @Implementation
  protected int getIconMaxHeight() {
    return maxIconHeight;
  }

  @Implementation
  protected int getIconMaxWidth() {
    return maxIconWidth;
  }

  /** Sets the value returned by {@link #getIconMaxHeight()}. */
  public void setIconMaxHeight(int height) {
    maxIconHeight = height;
  }

  /** Sets the value returned by {@link #getIconMaxWidth()}. */
  public void setIconMaxWidth(int width) {
    maxIconWidth = width;
  }

  @Implementation
  protected List<ShortcutInfo> getManifestShortcuts() {
    return manifestShortcuts;
  }

  /** Sets the value returned by {@link #getManifestShortcuts()}. */
  public void setManifestShortcuts(List<ShortcutInfo> manifestShortcuts) {
    for (ShortcutInfo shortcutInfo : manifestShortcuts) {
      shortcutInfo.addFlags(ShortcutInfo.FLAG_MANIFEST);
    }
    this.manifestShortcuts = manifestShortcuts;
  }

  @Implementation
  protected int getMaxShortcutCountPerActivity() {
    return maxShortcutCountPerActivity;
  }

  /** Sets the value returned by {@link #getMaxShortcutCountPerActivity()} . */
  public void setMaxShortcutCountPerActivity(int value) {
    maxShortcutCountPerActivity = value;
  }

  @Implementation
  protected List<ShortcutInfo> getPinnedShortcuts() {
    ImmutableList.Builder<ShortcutInfo> pinnedShortcuts = ImmutableList.builder();
    pinnedShortcuts.addAll(activePinnedShortcuts.values());
    pinnedShortcuts.addAll(disabledPinnedShortcuts.values());
    return pinnedShortcuts.build();
  }

  @Implementation
  protected boolean isRateLimitingActive() {
    return false;
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected boolean isRequestPinShortcutSupported() {
    return isRequestPinShortcutSupported;
  }

  public void setIsRequestPinShortcutSupported(boolean isRequestPinShortcutSupported) {
    this.isRequestPinShortcutSupported = isRequestPinShortcutSupported;
  }

  @Implementation
  protected void removeAllDynamicShortcuts() {
    dynamicShortcuts.clear();
  }

  @Implementation
  protected void removeDynamicShortcuts(List<String> shortcutIds) {
    for (String shortcutId : shortcutIds) {
      dynamicShortcuts.remove(shortcutId);
    }
  }

  @Implementation
  protected void reportShortcutUsed(String shortcutId) {}

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected boolean requestPinShortcut(ShortcutInfo shortcut, IntentSender resultIntent) {
    shortcut.addFlags(ShortcutInfo.FLAG_PINNED);
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
        resultIntent.sendIntent(RuntimeEnvironment.getApplication(), 0, null, null, null);
      } catch (SendIntentException e) {
        throw new IllegalStateException();
      }
    }
    return true;
  }

  @Implementation
  protected boolean setDynamicShortcuts(List<ShortcutInfo> shortcutInfoList) {
    dynamicShortcuts.clear();
    return addDynamicShortcuts(shortcutInfoList);
  }

  @Implementation
  protected boolean updateShortcuts(List<ShortcutInfo> shortcutInfoList) {
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

  /**
   * No-op on Robolectric. The real implementation calls out to a service, which will NPE on
   * Robolectric.
   */
  protected void updateShortcutVisibility(
      final String packageName, final byte[] certificate, final boolean visible) {}

  /**
   * In Robolectric, ShadowShortcutManager doesn't perform any caching so long lived shortcuts are
   * returned on place of shortcuts cached when shown in notifications.
   */
  @Implementation(minSdk = R)
  protected List<ShortcutInfo> getShortcuts(int matchFlags) {
    if (matchFlags == 0) {
      return Lists.newArrayList();
    }

    Set<ShortcutInfo> shortcutInfoSet = new HashSet<>();
    shortcutInfoSet.addAll(getManifestShortcuts());
    shortcutInfoSet.addAll(getDynamicShortcuts());
    shortcutInfoSet.addAll(getPinnedShortcuts());

    return shortcutInfoSet.stream()
        .filter(
            shortcutInfo ->
                ((matchFlags & FLAG_MATCH_MANIFEST) != 0 && shortcutInfo.isDeclaredInManifest())
                    || ((matchFlags & FLAG_MATCH_DYNAMIC) != 0 && shortcutInfo.isDynamic())
                    || ((matchFlags & FLAG_MATCH_PINNED) != 0 && shortcutInfo.isPinned())
                    || ((matchFlags & FLAG_MATCH_CACHED) != 0
                        && (shortcutInfo.isCached() || shortcutInfo.isLongLived())))
        .collect(toCollection(ArrayList::new));
  }

  /**
   * In Robolectric, ShadowShortcutManager doesn't handle rate limiting or shortcut count limits.
   * So, pushDynamicShortcut is similar to {@link #addDynamicShortcuts(List)} but with only one
   * {@link ShortcutInfo}.
   */
  @Implementation(minSdk = R)
  protected void pushDynamicShortcut(ShortcutInfo shortcut) {
    addDynamicShortcuts(Arrays.asList(shortcut));
  }

  /**
   * No-op on Robolectric. The real implementation calls out to a service, which will NPE on
   * Robolectric.
   */
  @Implementation(minSdk = VERSION_CODES.R)
  protected void removeLongLivedShortcuts(List<String> shortcutIds) {}
}
