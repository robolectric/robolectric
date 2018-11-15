package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for ShadowShortcutManager. */
@Config(minSdk = Build.VERSION_CODES.N_MR1)
@RunWith(AndroidJUnit4.class)
public final class ShadowShortcutManagerTest {
  private ShortcutManager shortcutManager;

  @Before
  public void setUp() {
    shortcutManager =
        (ShortcutManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SHORTCUT_SERVICE);
  }

  @Test
  public void testDynamicShortcuts_twoAdded() throws Exception {
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(createShortcut("id1"), createShortcut("id2")));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);
  }

  @Test
  public void testDynamicShortcuts_duplicateGetsDeduped() throws Exception {
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(createShortcut("id1"), createShortcut("id1")));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
  }

  @Test
  public void testDynamicShortcuts_immutableShortcutDoesntGetUpdated() throws Exception {
    ShortcutInfo shortcut1 = createShortcut("id1", true /* isImmutable */);
    when(shortcut1.getLongLabel()).thenReturn("original");
    ShortcutInfo shortcut2 = createShortcut("id1", true /* isImmutable */);
    when(shortcut2.getLongLabel()).thenReturn("updated");

    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    assertThat(shortcutManager.getDynamicShortcuts().get(0).getLongLabel()).isEqualTo("original");
  }

  @Test
  public void testShortcutWithIdenticalIdGetsUpdated() throws Exception {
    ShortcutInfo shortcut1 = createShortcut("id1");
    when(shortcut1.getLongLabel()).thenReturn("original");
    ShortcutInfo shortcut2 = createShortcut("id1");
    when(shortcut2.getLongLabel()).thenReturn("updated");

    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(1);
    assertThat(shortcutManager.getDynamicShortcuts().get(0).getLongLabel()).isEqualTo("updated");
  }

  @Test
  public void testRemoveAllDynamicShortcuts() throws Exception {
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(createShortcut("id1"), createShortcut("id2")));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);

    shortcutManager.removeAllDynamicShortcuts();
    assertThat(shortcutManager.getDynamicShortcuts()).isEmpty();
  }

  @Test
  public void testRemoveDynamicShortcuts() throws Exception {
    ShortcutInfo shortcut1 = createShortcut("id1");
    ShortcutInfo shortcut2 = createShortcut("id2");
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(shortcut1, shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).hasSize(2);

    shortcutManager.removeDynamicShortcuts(ImmutableList.of("id1"));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut2);
  }

  @Test
  public void testSetDynamicShortcutsClearOutOldList() throws Exception {
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
  public void testUpdateShortcut_dynamic() throws Exception {
    ShortcutInfo shortcut1 = createShortcut("id1");
    when(shortcut1.getLongLabel()).thenReturn("original");
    ShortcutInfo shortcutUpdated = createShortcut("id1");
    when(shortcutUpdated.getLongLabel()).thenReturn("updated");
    shortcutManager.addDynamicShortcuts(
        ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut1);

    shortcutManager.updateShortcuts(ImmutableList.of(shortcutUpdated));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcutUpdated);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void testUpdateShortcut_pinned() throws Exception {
    ShortcutInfo shortcut1 = createShortcut("id1");
    when(shortcut1.getLongLabel()).thenReturn("original");
    ShortcutInfo shortcutUpdated = createShortcut("id1");
    when(shortcutUpdated.getLongLabel()).thenReturn("updated");
    shortcutManager.requestPinShortcut(
        shortcut1, null /* resultIntent */);
    assertThat(shortcutManager.getPinnedShortcuts()).containsExactly(shortcut1);

    shortcutManager.updateShortcuts(ImmutableList.of(shortcutUpdated));
    assertThat(shortcutManager.getPinnedShortcuts()).containsExactly(shortcutUpdated);
  }

  @Test
  public void testUpdateShortcutsOnlyUpdatesExistingShortcuts() throws Exception {
    ShortcutInfo shortcut1 = createShortcut("id1");
    when(shortcut1.getLongLabel()).thenReturn("original");
    ShortcutInfo shortcutUpdated = createShortcut("id1");
    when(shortcutUpdated.getLongLabel()).thenReturn("updated");
    ShortcutInfo shortcut2 = createShortcut("id2");

    shortcutManager.addDynamicShortcuts(ImmutableList.of(shortcut1));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcut1);
    shortcutManager.updateShortcuts(ImmutableList.of(shortcutUpdated, shortcut2));
    assertThat(shortcutManager.getDynamicShortcuts()).containsExactly(shortcutUpdated);
    assertThat(shortcutManager.getDynamicShortcuts().get(0).getLongLabel()).isEqualTo("updated");
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void testPinningExistingDynamicShortcut() throws Exception {
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
  public void testPinningNewShortcut() throws Exception {
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


  private static ShortcutInfo createShortcut(String id) {
    return createShortcut(id, false /* isImmutable */);
  }

  private static ShortcutInfo createShortcut(String id, boolean isImmutable) {
    ShortcutInfo shortcut = mock(ShortcutInfo.class);
    when(shortcut.getId()).thenReturn(id);
    when(shortcut.isImmutable()).thenReturn(isImmutable);
    return shortcut;
  }
}
