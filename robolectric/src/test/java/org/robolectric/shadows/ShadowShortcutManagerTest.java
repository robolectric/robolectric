package org.robolectric.shadows;

import static android.content.pm.ShortcutManager.FLAG_MATCH_CACHED;
import static android.content.pm.ShortcutManager.FLAG_MATCH_DYNAMIC;
import static android.content.pm.ShortcutManager.FLAG_MATCH_MANIFEST;
import static android.content.pm.ShortcutManager.FLAG_MATCH_PINNED;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for ShadowShortcutManager. */
@Config(minSdk = Build.VERSION_CODES.N_MR1)
@RunWith(AndroidJUnit4.class)
public final class ShadowShortcutManagerTest {
  private static final int MANIFEST_SHORTCUT_COUNT = 5;
  private static final int DYNAMIC_SHORTCUT_COUNT = 4;
  private static final int CACHED_DYNAMIC_SHORTCUT_COUNT = 3;
  private static final int PINNED_SHORTCUT_COUNT = 2;

  private ShortcutManager shortcutManager;

  @Before
  public void setUp() {
    shortcutManager =
        (ShortcutManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SHORTCUT_SERVICE);
  }

  @Test
  public void setMaxIconWidth_iconWidthSetToNewMax() {
    int width = 10;
    shadowOf(shortcutManager).setIconMaxWidth(width);

    assertThat(shortcutManager.getIconMaxWidth()).isEqualTo(width);
  }

  @Test
  public void setMaxIconHeight_iconHeightSetToNewMax() {
    int height = 20;
    shadowOf(shortcutManager).setIconMaxHeight(height);

    assertThat(shortcutManager.getIconMaxHeight()).isEqualTo(height);
  }

  @Test
  public void testDynamicShortcuts_twoAdded() {
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(createShortcut("id1"), createShortcut("id2")));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);
  }

  @Test
  public void testDynamicShortcuts_duplicateGetsDeduped() {
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(createShortcut("id1"), createShortcut("id1")));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
  }

  @Test
  public void testDynamicShortcuts_immutableShortcutDoesntGetUpdated() {
    ShortcutInfo shortcut1 = createImmutableShortcut("id1");
    when(shortcut1.getLongLabel()).thenReturn("original");
    ShortcutInfo shortcut2 = createImmutableShortcut("id1");
    when(shortcut2.getLongLabel()).thenReturn("updated");

    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    assertThat(shortcutManager.getDynamicShortcuts().get(0).getLongLabel().toString())
        .isEqualTo("original");
  }

  @Test
  public void testShortcutWithIdenticalIdGetsUpdated() {

    ShortcutInfo shortcut1 = createShortcutWithLabel("id1", "original");
    ShortcutInfo shortcut2 = createShortcutWithLabel("id1", "updated");

    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    assertThat(shortcutManager.getDynamicShortcuts().get(0).getLongLabel().toString())
        .isEqualTo("updated");
  }

  @Test
  public void testRemoveAllDynamicShortcuts() {
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(createShortcut("id1"), createShortcut("id2")));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);

    shortcutManager.removeAllDynamicShortcuts();
    assertThat(shortcutManager.getDynamicShortcuts()).isEmpty();
  }

  @Test
  public void testRemoveDynamicShortcuts() {
    ShortcutInfo shortcut1 = createShortcut("id1");
    ShortcutInfo shortcut2 = createShortcut("id2");
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(shortcut1, shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);

    shortcutManager.removeDynamicShortcuts(ImmutableList.of("id1"));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut2);
  }

  @Test
  public void testSetDynamicShortcutsClearOutOldList() {
    ShortcutInfo shortcut1 = createShortcut("id1");
    ShortcutInfo shortcut2 = createShortcut("id2");
    ShortcutInfo shortcut3 = createShortcut("id3");
    ShortcutInfo shortcut4 = createShortcut("id4");

    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1, shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut1, shortcut2);
    shortcutManager.setDynamicShortcuts(ImmutableList.of(shortcut3, shortcut4));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut3, shortcut4);
  }

  @Test
  public void testUpdateShortcut_dynamic() {
    ShortcutInfo shortcut1 = createShortcutWithLabel("id1", "original");
    ShortcutInfo shortcutUpdated = createShortcutWithLabel("id1", "updated");
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut1);

    shortcutManager.updateShortcuts(ImmutableList.of(shortcutUpdated));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcutUpdated);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void testUpdateShortcut_pinned() {
    ShortcutInfo shortcut1 = createShortcutWithLabel("id1", "original");
    ShortcutInfo shortcutUpdated = createShortcutWithLabel("id1", "updated");
    shortcutManager.requestPinShortcut(
        shortcut1, null /* resultIntent */);
    assertThat(shortcutManager.getPinnedShortcuts()).containsExactly(shortcut1);

    shortcutManager.updateShortcuts(ImmutableList.of(shortcutUpdated));
    assertThat(shortcutManager.getPinnedShortcuts()).containsExactly(shortcutUpdated);
  }

  @Test
  public void testUpdateShortcutsOnlyUpdatesExistingShortcuts() {
    ShortcutInfo shortcut1 = createShortcutWithLabel("id1", "original");
    ShortcutInfo shortcutUpdated = createShortcutWithLabel("id1", "updated");
    ShortcutInfo shortcut2 = createShortcut("id2");

    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut1);
    shortcutManager.updateShortcuts(ImmutableList.of(shortcutUpdated, shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcutUpdated);
    assertThat(shortcutManager.getDynamicShortcuts().get(0).getLongLabel().toString())
        .isEqualTo("updated");
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void testPinningExistingDynamicShortcut() {
    ShortcutInfo shortcut1 = createShortcut("id1");
    ShortcutInfo shortcut2 = createShortcut("id2");
    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1, shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);

    shortcutManager.requestPinShortcut(shortcut1, null /* resultIntent */);
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut2);
    assertThat(shortcutManager.getPinnedShortcuts()).containsExactly(shortcut1);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void testPinningNewShortcut() {
    ShortcutInfo shortcut1 = createShortcut("id1");
    shortcutManager.requestPinShortcut(shortcut1, null /* resultIntent */);
    assertThat(shortcutManager.getPinnedShortcuts()).containsExactly(shortcut1);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void testSetMaxShortcutCountPerActivity() {
    ShadowShortcutManager shadowShortcutManager = Shadow.extract(shortcutManager);
    shadowShortcutManager.setMaxShortcutCountPerActivity(42);
    assertThat(shortcutManager.getMaxShortcutCountPerActivity()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void testSetManifestShortcuts() {
    ImmutableList<ShortcutInfo> manifestShortcuts = ImmutableList.of(createShortcut("id1"));
    ShadowShortcutManager shadowShortcutManager = Shadow.extract(shortcutManager);
    shadowShortcutManager.setManifestShortcuts(manifestShortcuts);
    assertThat(shortcutManager.getManifestShortcuts()).isEqualTo(manifestShortcuts);
  }

  @Config(minSdk = R)
  @Test
  public void getShortcuts_matchNone_emptyListReturned() {
    createShortCuts(
        MANIFEST_SHORTCUT_COUNT,
        DYNAMIC_SHORTCUT_COUNT,
        CACHED_DYNAMIC_SHORTCUT_COUNT,
        PINNED_SHORTCUT_COUNT);
    List<ShortcutInfo> shortcuts = shortcutManager.getShortcuts(0);

    assertThat(shortcuts).isEmpty();
  }

  @Config(minSdk = R)
  @Test
  public void getShortcuts_matchManifest_manifestShortcutsReturned() {
    createShortCuts(
        MANIFEST_SHORTCUT_COUNT,
        DYNAMIC_SHORTCUT_COUNT,
        CACHED_DYNAMIC_SHORTCUT_COUNT,
        PINNED_SHORTCUT_COUNT);
    List<ShortcutInfo> shortcuts = shortcutManager.getShortcuts(FLAG_MATCH_MANIFEST);

    assertThat(shortcuts).hasSize(MANIFEST_SHORTCUT_COUNT);
  }

  @Config(minSdk = R)
  @Test
  public void getShortcuts_matchDynamic_dynamicShortcutsReturned() {
    createShortCuts(
        MANIFEST_SHORTCUT_COUNT,
        DYNAMIC_SHORTCUT_COUNT,
        CACHED_DYNAMIC_SHORTCUT_COUNT,
        PINNED_SHORTCUT_COUNT);
    List<ShortcutInfo> shortcuts = shortcutManager.getShortcuts(FLAG_MATCH_DYNAMIC);

    assertThat(shortcuts).hasSize(DYNAMIC_SHORTCUT_COUNT + CACHED_DYNAMIC_SHORTCUT_COUNT);
  }

  @Config(minSdk = R)
  @Test
  public void getShortcuts_matchCached_cachedShortcutsReturned() {
    createShortCuts(
        MANIFEST_SHORTCUT_COUNT,
        DYNAMIC_SHORTCUT_COUNT,
        CACHED_DYNAMIC_SHORTCUT_COUNT,
        PINNED_SHORTCUT_COUNT);
    List<ShortcutInfo> shortcuts = shortcutManager.getShortcuts(FLAG_MATCH_CACHED);

    assertThat(shortcuts).hasSize(CACHED_DYNAMIC_SHORTCUT_COUNT);
  }

  @Config(minSdk = R)
  @Test
  public void getShortcuts_matchPinned_pinnedShortcutsReturned() {
    createShortCuts(
        MANIFEST_SHORTCUT_COUNT,
        DYNAMIC_SHORTCUT_COUNT,
        CACHED_DYNAMIC_SHORTCUT_COUNT,
        PINNED_SHORTCUT_COUNT);
    List<ShortcutInfo> shortcuts = shortcutManager.getShortcuts(FLAG_MATCH_PINNED);

    assertThat(shortcuts).hasSize(PINNED_SHORTCUT_COUNT);
  }

  @Config(minSdk = R)
  @Test
  public void getShortcuts_matchMultipleFlags_matchedShortcutsReturned() {
    createShortCuts(
        MANIFEST_SHORTCUT_COUNT,
        DYNAMIC_SHORTCUT_COUNT,
        CACHED_DYNAMIC_SHORTCUT_COUNT,
        PINNED_SHORTCUT_COUNT);
    List<ShortcutInfo> shortcuts =
        shortcutManager.getShortcuts(FLAG_MATCH_CACHED | FLAG_MATCH_PINNED);

    assertThat(shortcuts).hasSize(CACHED_DYNAMIC_SHORTCUT_COUNT + PINNED_SHORTCUT_COUNT);
  }

  @Config(minSdk = R)
  @Test
  public void addDynamicShortcuts_longLived_cachedShortcutsAdded() {
    createShortCuts(
        MANIFEST_SHORTCUT_COUNT,
        DYNAMIC_SHORTCUT_COUNT,
        CACHED_DYNAMIC_SHORTCUT_COUNT,
        PINNED_SHORTCUT_COUNT);
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(
            createLongLivedShortcut("id1", /* isLonglived= */ true),
            createLongLivedShortcut("id2", /* isLonglived= */ true)));

    List<ShortcutInfo> shortcuts = shortcutManager.getShortcuts(FLAG_MATCH_CACHED);

    assertThat(shortcuts).hasSize(CACHED_DYNAMIC_SHORTCUT_COUNT + 2);
  }

  @Config(minSdk = R)
  @Test
  public void pushTwoDynamicShortcuts_shortcutsAdded() {
    shortcutManager.pushDynamicShortcut(createShortcut("id1"));
    shortcutManager.pushDynamicShortcut(createShortcut("id2"));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);
  }

  @Config(minSdk = R)
  @Test
  public void pushDynamicShortcutWithSameId_duplicateGetsDeduped() {
    shortcutManager.pushDynamicShortcut(createShortcut("id1"));
    shortcutManager.pushDynamicShortcut(createShortcut("id1"));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
  }

  @Config(minSdk = R)
  @Test
  public void pushDynamicShortcutWithSameId_differentLabel_shortcutIsUpdated() {
    ShortcutInfo shortcut1 = createShortcutWithLabel("id1", "original");
    ShortcutInfo shortcut2 = createShortcutWithLabel("id1", "updated");

    shortcutManager.pushDynamicShortcut(shortcut1);
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    shortcutManager.pushDynamicShortcut(shortcut2);
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    assertThat(shortcutManager.getDynamicShortcuts().get(0).getLongLabel().toString())
        .isEqualTo("updated");
  }

  private void createShortCuts(
      int manifestShortcutCount,
      int dynamicShortcutCount,
      int cachedShortcutCount,
      int pinnedShortcutCount) {
    List<ShortcutInfo> manifestShortcuts = new ArrayList<>();
    for (int i = 0; i < manifestShortcutCount; i++) {
      manifestShortcuts.add(createShortcut("manifest_" + i));
    }
    ShadowShortcutManager shadowShortcutManager = Shadow.extract(shortcutManager);
    shadowShortcutManager.setManifestShortcuts(manifestShortcuts);

    List<ShortcutInfo> dynamicShortcuts = new ArrayList<>();
    for (int i = 0; i < dynamicShortcutCount; i++) {
      dynamicShortcuts.add(createShortcut("dynamic_" + i));
    }
    for (int i = 0; i < cachedShortcutCount; i++) {
      dynamicShortcuts.add(createLongLivedShortcut("cached_" + i, /* isLonglived= */ true));
    }
    shortcutManager.addDynamicShortcuts(dynamicShortcuts);

    for (int i = 0; i < pinnedShortcutCount; i++) {
      shortcutManager.requestPinShortcut(createShortcut("pinned_" + i), /* resultIntent= */ null);
    }
  }

  private static ShortcutInfo createShortcut(String id) {
    return new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), id).build();
  }

  private static ShortcutInfo createImmutableShortcut(String id) {
    ShortcutInfo shortcut = mock(ShortcutInfo.class);
    when(shortcut.getId()).thenReturn(id);
    when(shortcut.isImmutable()).thenReturn(true);
    return shortcut;
  }

  private static ShortcutInfo createLongLivedShortcut(String id, boolean isLonglived) {
    return new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), id)
        .setLongLived(isLonglived)
        .build();
  }

  private static ShortcutInfo createShortcutWithLabel(String id, CharSequence longLabel) {
    return new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), id)
        .setLongLabel(longLabel)
        .build();
  }
}
