package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.ApkAssetsCookie.K_INVALID_COOKIE;
import static org.robolectric.res.android.ResourceTypes.Res_value.DATA_NULL_EMPTY;
import static org.robolectric.res.android.ResourceTypes.Res_value.DATA_NULL_UNDEFINED;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_ATTRIBUTE;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_INT_DEC;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_NULL;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.robolectric.res.android.CppAssetManager2.ResolvedBag;
import org.robolectric.res.android.CppAssetManager2.Theme;
import org.robolectric.res.android.ResourceTypes.Res_value;

/** Exercises theme copies through style application and lookup, including absent packages. */
public class CppAssetManager2ThemeTest {
  private static final int SYSTEM_ATTRIBUTE = 0x01010004;
  private static final int APP_ATTRIBUTE = 0x7f010008;
  private static final int OTHER_APP_ATTRIBUTE = 0x7f010002;
  private static final int STYLE = 0x7f020001;
  private static final int OTHER_STYLE = 0x7f020002;
  private static final ApkAssetsCookie COOKIE = ApkAssetsCookie.forInt(2);

  @Test
  public void copyingEmptyThemeClearsDestinationAndAllowsLaterApplication() {
    BagAssetManager assets = new BagAssetManager();
    assets.addStyle(STYLE, 0x10, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 42));
    Theme destination = assets.NewTheme();
    assertThat(destination.ApplyStyle(STYLE, false)).isTrue();

    assertThat(destination.SetTo(assets.NewTheme())).isTrue();
    assertMissing(destination, APP_ATTRIBUTE);
    assertThat(destination.GetChangingConfigurations()).isEqualTo(0);

    assertThat(destination.ApplyStyle(STYLE, false)).isTrue();
    assertAttribute(destination, APP_ATTRIBUTE, TYPE_INT_DEC, 42, 0x10);
  }

  @Test
  public void copiesKeepAttributesIndependentAfterSourceAndDestinationChange() {
    BagAssetManager assets = new BagAssetManager();
    assets.addStyle(STYLE, 0x10, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 42));
    assets.addStyle(OTHER_STYLE, 0x20, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 73));
    Theme source = assets.NewTheme();
    assertThat(source.ApplyStyle(STYLE, false)).isTrue();
    Theme destination = assets.NewTheme();
    assertThat(destination.SetTo(source)).isTrue();

    assertThat(destination.ApplyStyle(OTHER_STYLE, true)).isTrue();
    assertAttribute(source, APP_ATTRIBUTE, TYPE_INT_DEC, 42, 0x10);
    assertAttribute(destination, APP_ATTRIBUTE, TYPE_INT_DEC, 73, 0x30);
    source.Clear();
    assertMissing(source, APP_ATTRIBUTE);
    assertAttribute(destination, APP_ATTRIBUTE, TYPE_INT_DEC, 73, 0x30);
  }

  @Test
  public void copyingAcrossAssetManagersRetainsSystemAttributesAndClearsAppAttributes() {
    BagAssetManager sourceAssets = new BagAssetManager();
    sourceAssets.addStyle(STYLE, 0x10,
        entry(SYSTEM_ATTRIBUTE, TYPE_INT_DEC, 12), entry(APP_ATTRIBUTE, TYPE_INT_DEC, 42));
    Theme source = sourceAssets.NewTheme();
    assertThat(source.ApplyStyle(STYLE, false)).isTrue();
    BagAssetManager destinationAssets = new BagAssetManager();
    destinationAssets.addStyle(OTHER_STYLE, 0x20, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 73));
    Theme destination = destinationAssets.NewTheme();
    assertThat(destination.ApplyStyle(OTHER_STYLE, false)).isTrue();

    assertThat(destination.SetTo(source)).isTrue();
    assertAttribute(destination, SYSTEM_ATTRIBUTE, TYPE_INT_DEC, 12, 0x10);
    assertMissing(destination, APP_ATTRIBUTE);
    assertAttribute(source, APP_ATTRIBUTE, TYPE_INT_DEC, 42, 0x10);
    assertThat(destination.ApplyStyle(OTHER_STYLE, false)).isTrue();
    assertAttribute(destination, APP_ATTRIBUTE, TYPE_INT_DEC, 73, 0x20);
  }

  @Test
  public void copyingThemeToItselfRetainsValuesAndConfigurationFlags() {
    BagAssetManager assets = new BagAssetManager();
    assets.addStyle(STYLE, 0x10, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 42));
    Theme theme = assets.NewTheme();
    assertThat(theme.ApplyStyle(STYLE, false)).isTrue();

    assertThat(theme.SetTo(theme)).isTrue();
    assertAttribute(theme, APP_ATTRIBUTE, TYPE_INT_DEC, 42, 0x10);
    assertThat(theme.GetChangingConfigurations()).isEqualTo(0x10);
  }

  @Test
  public void missingSlotsRemainMissingAfterCopyAndCanBeFilledIndependently() {
    BagAssetManager assets = new BagAssetManager();
    assets.addStyle(STYLE, 0x10, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 42));
    assets.addStyle(OTHER_STYLE, 0x20, entry(OTHER_APP_ATTRIBUTE, TYPE_INT_DEC, 73));
    Theme source = assets.NewTheme();
    assertThat(source.ApplyStyle(STYLE, false)).isTrue();
    Theme destination = assets.NewTheme();

    assertThat(destination.SetTo(source)).isTrue();
    assertMissing(destination, OTHER_APP_ATTRIBUTE);
    assertThat(destination.ApplyStyle(OTHER_STYLE, false)).isTrue();
    assertAttribute(destination, OTHER_APP_ATTRIBUTE, TYPE_INT_DEC, 73, 0x20);
    assertMissing(source, OTHER_APP_ATTRIBUTE);
    assertAttribute(source, APP_ATTRIBUTE, TYPE_INT_DEC, 42, 0x10);
  }

  @Test
  public void populatedSourceSlotsCanChangeWithoutChangingTheCopy() {
    BagAssetManager assets = new BagAssetManager();
    assets.addStyle(STYLE, 0x10, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 42));
    assets.addStyle(OTHER_STYLE, 0x20, entry(APP_ATTRIBUTE, TYPE_INT_DEC, 73));
    Theme source = assets.NewTheme();
    assertThat(source.ApplyStyle(STYLE, false)).isTrue();
    Theme destination = assets.NewTheme();
    assertThat(destination.SetTo(source)).isTrue();

    assertThat(source.ApplyStyle(OTHER_STYLE, true)).isTrue();
    assertAttribute(source, APP_ATTRIBUTE, TYPE_INT_DEC, 73, 0x30);
    assertAttribute(destination, APP_ATTRIBUTE, TYPE_INT_DEC, 42, 0x10);
  }

  @Test
  public void explicitEmptyAndNullAttributesKeepTheirNonForcedStyleBehaviorAfterCopy() {
    BagAssetManager assets = new BagAssetManager();
    assets.addStyle(STYLE, 0x10,
        entry(APP_ATTRIBUTE, TYPE_NULL, DATA_NULL_EMPTY),
        entry(OTHER_APP_ATTRIBUTE, TYPE_NULL, DATA_NULL_UNDEFINED));
    assets.addStyle(OTHER_STYLE, 0x20,
        entry(APP_ATTRIBUTE, TYPE_INT_DEC, 42), entry(OTHER_APP_ATTRIBUTE, TYPE_INT_DEC, 73));
    Theme source = assets.NewTheme();
    assertThat(source.ApplyStyle(STYLE, false)).isTrue();
    Theme destination = assets.NewTheme();
    assertThat(destination.SetTo(source)).isTrue();

    assertAttribute(destination, APP_ATTRIBUTE, TYPE_NULL, DATA_NULL_EMPTY, 0x10);
    assertMissing(destination, OTHER_APP_ATTRIBUTE);
    assertThat(destination.ApplyStyle(OTHER_STYLE, false)).isTrue();
    assertAttribute(destination, APP_ATTRIBUTE, TYPE_NULL, DATA_NULL_EMPTY, 0x10);
    assertAttribute(destination, OTHER_APP_ATTRIBUTE, TYPE_INT_DEC, 73, 0x30);
    assertMissing(source, OTHER_APP_ATTRIBUTE);
  }

  @Test
  public void attributeReferenceToMissingSlotLeavesOutputUnchangedAndResolvesAfterApplication() {
    BagAssetManager assets = new BagAssetManager();
    assets.addStyle(STYLE, 0x10, entry(APP_ATTRIBUTE, TYPE_ATTRIBUTE, OTHER_APP_ATTRIBUTE));
    assets.addStyle(OTHER_STYLE, 0x20, entry(OTHER_APP_ATTRIBUTE, TYPE_INT_DEC, 73));
    Theme source = assets.NewTheme();
    assertThat(source.ApplyStyle(STYLE, false)).isTrue();
    Theme destination = assets.NewTheme();
    assertThat(destination.SetTo(source)).isTrue();

    assertMissing(destination, APP_ATTRIBUTE);
    assertThat(destination.ApplyStyle(OTHER_STYLE, false)).isTrue();
    assertAttribute(destination, APP_ATTRIBUTE, TYPE_INT_DEC, 73, 0x30);
    assertMissing(source, APP_ATTRIBUTE);
  }

  private static ResolvedBag.Entry entry(int attribute, int dataType, int data) {
    ResolvedBag.Entry entry = new ResolvedBag.Entry();
    entry.key = attribute;
    entry.value = new Res_value((byte) dataType, data);
    entry.cookie = COOKIE;
    return entry;
  }

  private static void assertAttribute(Theme theme, int attribute, int type, int data, int flags) {
    Ref<Res_value> value = new Ref<>(null);
    Ref<Integer> actualFlags = new Ref<>(0);
    assertThat(theme.GetAttribute(attribute, value, actualFlags)).isSameInstanceAs(COOKIE);
    assertThat((int) value.get().dataType).isEqualTo(type);
    assertThat(value.get().data).isEqualTo(data);
    assertThat(actualFlags.get()).isEqualTo(flags);
  }

  /** Failed lookups leave caller-owned output values unchanged. */
  private static void assertMissing(Theme theme, int attribute) {
    Res_value sentinel = new Res_value((byte) TYPE_INT_DEC, 91);
    Ref<Res_value> value = new Ref<>(sentinel);
    Ref<Integer> flags = new Ref<>(0x80);
    assertThat(theme.GetAttribute(attribute, value, flags)).isSameInstanceAs(K_INVALID_COOKIE);
    assertThat(value.get()).isSameInstanceAs(sentinel);
    assertThat(flags.get()).isEqualTo(0x80);
  }

  /** Supplies sorted style bags without Android framework or APK dependencies. */
  private static final class BagAssetManager extends CppAssetManager2 {
    private final Map<Integer, ResolvedBag> styles = new HashMap<>();

    void addStyle(int style, int flags, ResolvedBag.Entry... entries) {
      Arrays.sort(entries, Comparator.comparingInt(entry -> entry.key));
      ResolvedBag bag = new ResolvedBag();
      bag.entries = entries;
      bag.entry_count = entries.length;
      bag.type_spec_flags = flags;
      styles.put(style, bag);
    }

    @Override
    ResolvedBag GetBag(int resid, List<Integer> childResids) {
      return styles.get(resid);
    }
  }
}
