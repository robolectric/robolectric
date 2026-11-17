package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_PACKAGE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE_SPEC_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE_TYPE;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_ATTRIBUTE;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_DYNAMIC_ATTRIBUTE;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_DYNAMIC_REFERENCE;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_INT_DEC;
import static org.robolectric.res.android.ResourceTypes.Res_value.TYPE_REFERENCE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.robolectric.res.android.CppAssetManager2.ResolvedBag;
import org.robolectric.res.android.CppAssetManager2.Theme;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResTable_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_map;
import org.robolectric.res.android.ResourceTypes.ResTable_map_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_type;
import org.robolectric.res.android.ResourceTypes.Res_value;

/** Checks mutable entry ownership and package remapping through real resource table decoding. */
public class CppAssetManager2ResourceValueTest {
  private static final int PARENT = 0x7f010000;
  private static final int CHILD = 0x7f010001;
  private static final int ATTRIBUTE = 0x7f020002;

  @Test
  public void bagEntryCopyKeepsMutableFieldsIndependent() {
    ResolvedBag.Entry source = new ResolvedBag.Entry();
    source.key = ATTRIBUTE;
    source.cookie = ApkAssetsCookie.forInt(2);
    source.value = new Res_value((byte) TYPE_INT_DEC, 42);
    ResolvedBag.Entry copy = source.copy();

    copy.key = ATTRIBUTE + 1;
    copy.cookie = ApkAssetsCookie.forInt(3);
    copy.value = new Res_value((byte) TYPE_INT_DEC, 73);

    assertThat(source.key).isEqualTo(ATTRIBUTE);
    assertThat(source.cookie).isSameInstanceAs(ApkAssetsCookie.forInt(2));
    assertThat(source.value.data).isEqualTo(42);
    source.value = new Res_value((byte) TYPE_INT_DEC, 91);
    assertThat(copy.value.data).isEqualTo(73);
  }

  @Test
  public void bagEntryCopyPreservesMutableSubclassCopyBehavior() {
    MutableValue value = new MutableValue(42);
    ResolvedBag.Entry source = new ResolvedBag.Entry();
    source.value = value;

    ResolvedBag.Entry copy = source.copy();

    assertThat(value.copies).isEqualTo(1);
    value.state = 73;
    MutableValue copiedValue = (MutableValue) copy.value;
    assertThat(copiedValue.state).isEqualTo(42);
    copiedValue.state = 91;
    assertThat(value.state).isEqualTo(73);
  }

  @Test
  public void themeCopyPreservesMutableSubclassCopyBehavior() {
    MutableValue value = new MutableValue(42);
    ResolvedBag.Entry entry = new ResolvedBag.Entry();
    entry.key = ATTRIBUTE;
    entry.cookie = ApkAssetsCookie.forInt(0);
    entry.value = value;
    ResolvedBag bag = new ResolvedBag();
    bag.entries = new ResolvedBag.Entry[] {entry};
    bag.entry_count = 1;
    CppAssetManager2 assets =
        new CppAssetManager2() {
          @Override
          ResolvedBag GetBag(int resid, List<Integer> childResids) {
            return bag;
          }
        };
    Theme source = assets.NewTheme();
    assertThat(source.ApplyStyle(PARENT, false)).isTrue();
    Theme destination = assets.NewTheme();

    assertThat(destination.SetTo(source)).isTrue();

    assertThat(value.copies).isEqualTo(1);
    value.state = 73;
    Ref<Res_value> result = new Ref<>(null);
    assertThat(destination.GetAttribute(ATTRIBUTE, result, new Ref<>(0)))
        .isSameInstanceAs(entry.cookie);
    MutableValue copiedValue = (MutableValue) result.get();
    assertThat(copiedValue.state).isEqualTo(42);
    copiedValue.state = 91;
    assertThat(value.state).isEqualTo(73);
  }

  @Test
  public void remappingCopiedBagValueLeavesSourceAvailableForAnotherManager() {
    ResolvedBag.Entry source = new ResolvedBag.Entry();
    source.value = new Res_value((byte) TYPE_DYNAMIC_ATTRIBUTE, 0x00020002);
    ResolvedBag.Entry copy = source.copy();
    Ref<Res_value> copiedValue = new Ref<>(copy.value);
    assertThat(new DynamicRefTable((byte) 2, false).lookupResourceValue(copiedValue))
        .isEqualTo(NO_ERROR);
    copy.value = copiedValue.get();

    Ref<Res_value> sourceValue = new Ref<>(source.value);
    assertThat(new DynamicRefTable((byte) 3, false).lookupResourceValue(sourceValue))
        .isEqualTo(NO_ERROR);
    assertThat((int) source.value.dataType).isEqualTo(TYPE_DYNAMIC_ATTRIBUTE);
    assertThat(source.value.data).isEqualTo(0x00020002);
    assertThat((int) copy.value.dataType).isEqualTo(TYPE_ATTRIBUTE);
    assertThat(copy.value.data).isEqualTo(0x02020002);
    assertThat(sourceValue.get().data).isEqualTo(0x03020002);
  }

  @Test
  public void publicValueCopyRetainsDistinctIdentityAndDecodedSize() {
    Res_value original = new Res_value();
    Res_value copy = original.copy();
    assertThat(copy).isNotSameInstanceAs(original);
    assertThat(copy.size).isEqualTo(original.size);
    assertThat(copy.dataType).isEqualTo(original.dataType);
    assertThat(copy.data).isEqualTo(original.data);
  }

  @Test
  public void resourceLookupRemapsReferencesAndKeepsRepeatedOutputsIndependent() {
    CppAssetManager2 assets =
        assets(true, simple(TYPE_DYNAMIC_REFERENCE, PARENT), simple(TYPE_INT_DEC, 42));
    Ref<Res_value> first = new Ref<>(null);
    Ref<ResTable_config> config = new Ref<>(null);
    Ref<Integer> flags = new Ref<>(0);
    assertThat(assets.GetResource(0x02010000, false, (short) 0, first, config, flags))
        .isSameInstanceAs(ApkAssetsCookie.forInt(0));
    Res_value saved = first.get();
    assertThat((int) saved.dataType).isEqualTo(TYPE_REFERENCE);
    assertThat(saved.data).isEqualTo(0x02010000);
    assertThat(flags.get()).isEqualTo(0x10);

    assertThat(assets.GetResource(0x02010001, false, (short) 0, first, config, flags))
        .isSameInstanceAs(ApkAssetsCookie.forInt(0));
    assertThat(first.get().data).isEqualTo(42);
    assertThat(saved.data).isEqualTo(0x02010000);
    Ref<Res_value> repeated = new Ref<>(null);
    assets.GetResource(0x02010000, false, (short) 0, repeated, config, flags);
    assertThat(repeated.get().data).isEqualTo(saved.data);
  }

  @Test
  public void inheritedBagMergesOverrideAndTrailingChildWithIndependentEntries() {
    CppAssetManager2 assets =
        assets(
            false,
            bag(0, item(2, 20), item(4, 40), item(6, 60)),
            bag(PARENT, item(1, 10), item(4, 41), item(7, 70)));
    ResolvedBag parent = assets.GetBag(PARENT);
    ResolvedBag child = assets.GetBag(CHILD);
    assertThat(child.entry_count).isEqualTo(5);
    assertThat(values(child)).asList().containsExactly(10, 20, 41, 60, 70).inOrder();
    assertThat(child.type_spec_flags).isEqualTo(0x30);

    child.entries[1].value = new Res_value((byte) TYPE_INT_DEC, 99);
    child.entries[1].key++;
    assertThat(parent.entries[0].value.data).isEqualTo(20);
    assertThat(parent.entries[0].key).isEqualTo(ATTRIBUTE);
    parent.entries[2].value = new Res_value((byte) TYPE_INT_DEC, 88);
    assertThat(child.entries[3].value.data).isEqualTo(60);
  }

  @Test
  public void inheritedBagRemapsKeysAndValuesIncludingRemainingParentEntries() {
    CppAssetManager2 assets =
        assets(
            true,
            bag(0, item(2, TYPE_DYNAMIC_REFERENCE, PARENT), item(6, 60)),
            bag(PARENT, item(1, TYPE_DYNAMIC_ATTRIBUTE, ATTRIBUTE)));
    ResolvedBag child = assets.GetBag(0x02010001);
    assertThat(child.entry_count).isEqualTo(3);
    assertThat(child.entries[0].key).isEqualTo(0x02020001);
    assertThat((int) child.entries[0].value.dataType).isEqualTo(TYPE_ATTRIBUTE);
    assertThat(child.entries[0].value.data).isEqualTo(0x02020002);
    assertThat(child.entries[1].key).isEqualTo(0x02020002);
    assertThat((int) child.entries[1].value.dataType).isEqualTo(TYPE_REFERENCE);
    assertThat(child.entries[1].value.data).isEqualTo(0x02010000);
    assertThat(child.entries[2].key).isEqualTo(0x02020006);
    assertThat(child.entries[2].value.data).isEqualTo(60);
  }

  /** Models a public resource-value subtype whose copy contract owns mutable extension state. */
  private static final class MutableValue extends Res_value {
    int state;
    int copies;

    MutableValue(int state) {
      super((byte) TYPE_INT_DEC, 42);
      this.state = state;
    }

    @Override
    public Res_value copy() {
      copies++;
      return new MutableValue(state);
    }
  }

  private static int[] values(ResolvedBag bag) {
    return Arrays.stream(bag.entries).mapToInt(entry -> entry.value.data).toArray();
  }

  private static int[] item(int entry, int data) {
    return item(entry, TYPE_INT_DEC, data);
  }

  private static int[] item(int entry, int type, int data) {
    return new int[] {0x7f020000 | entry, type, data};
  }

  /** Encodes the same entry headers and immutable values used by a compiled resource table. */
  private static byte[] simple(int type, int data) {
    ByteBuffer buffer = ByteBuffer.allocate(ResTable_entry.SIZEOF + Res_value.SIZEOF);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.putShort((short) ResTable_entry.SIZEOF).putShort((short) 0).putInt(0);
    Res_value.write(buffer, type, data);
    return buffer.array();
  }

  private static byte[] bag(int parent, int[]... items) {
    ByteBuffer buffer =
        ByteBuffer.allocate(ResTable_map_entry.BASE_SIZEOF + items.length * ResTable_map.SIZEOF)
            .order(ByteOrder.LITTLE_ENDIAN);
    buffer.putShort((short) ResTable_map_entry.BASE_SIZEOF);
    buffer.putShort((short) ResTable_entry.FLAG_COMPLEX).putInt(0);
    buffer.putInt(parent).putInt(items.length);
    for (int[] item : items) {
      buffer.putInt(item[0]);
      Res_value.write(buffer, item[1], item[2]);
    }
    return buffer.array();
  }

  /**
   * Loads one in-memory package so GetResource and GetBag exercise normal decoding and remapping.
   */
  private static CppAssetManager2 assets(boolean shared, byte[]... entries) {
    ByteBuffer buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
    ResChunk_header.write(
        buffer,
        (short) RES_TABLE_TYPE,
        () -> buffer.putInt(1),
        () ->
            ResChunk_header.write(
                buffer,
                (short) RES_TABLE_PACKAGE_TYPE,
                () -> {
                  buffer.putInt(0x7f);
                  buffer.put(new byte[256]); // Empty package name.
                  buffer.put(new byte[20]); // String pool offsets and type ID offset.
                },
                () -> writeType(buffer, entries)));
    ByteBuffer encoded =
        ByteBuffer.wrap(Arrays.copyOf(buffer.array(), buffer.position()))
            .order(ByteOrder.LITTLE_ENDIAN);
    LoadedArsc table = LoadedArsc.Load(new StringPiece(encoded, 0), null, false, shared);
    assertThat(table.GetPackages()).hasSize(1);
    CppApkAssets apk =
        new CppApkAssets() {
          @Override
          public LoadedArsc GetLoadedArsc() {
            return table;
          }
        };
    CppAssetManager2 assets = new CppAssetManager2();
    assertThat(assets.SetApkAssets(Collections.singletonList(apk), true)).isTrue();
    return assets;
  }

  /** Supplies one unqualified type with separate flags for each entry and dense entry offsets. */
  private static void writeType(ByteBuffer buffer, byte[][] entries) {
    ResChunk_header.write(
        buffer,
        (short) RES_TABLE_TYPE_SPEC_TYPE,
        () -> buffer.putInt(1).putInt(entries.length),
        () -> {
          for (int i = 0; i < entries.length; i++) {
            buffer.putInt(0x10 << i);
          }
        });
    ResChunk_header.write(
        buffer,
        (short) RES_TABLE_TYPE_TYPE,
        () -> {
          buffer.putInt(1).putInt(entries.length);
          buffer.putInt(
              ResTable_type.SIZEOF_WITHOUT_CONFIG + ResTable_config.SIZEOF + 4 * entries.length);
          buffer.putInt(ResTable_config.SIZEOF).put(new byte[ResTable_config.SIZEOF - 4]);
        },
        () -> {
          int offset = 0;
          for (byte[] entry : entries) {
            buffer.putInt(offset);
            offset += entry.length;
          }
          for (byte[] entry : entries) {
            buffer.put(entry);
          }
        });
  }
}
