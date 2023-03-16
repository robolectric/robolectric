package org.robolectric.res.android;

import static org.junit.Assert.assertEquals;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_PACKAGE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_STAGED_ALIAS_TYPE;

import java.nio.ByteBuffer;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;

@RunWith(JUnit4.class)
public class LoadedArscTest {

  @Test
  public void testResTableStagedAliasType() {
    final int stagedResId = 0x123;
    final int finalizedResId = 0x456;

    final ByteBuffer buf = ByteBuffer.allocate(1024);

    ResChunk_header.write(
        buf,
        (short) RES_TABLE_PACKAGE_TYPE,
        () -> {
          // header
          buf.putInt(0x01); // ResTable_package.id
          // ResTable_package.name
          for (int i = 0; i < 128; i++) {
            buf.putChar('\0');
          }
          buf.putInt(0); // ResTable_package.typeStrings
          buf.putInt(0); // ResTable_package.lastPublicType
          buf.putInt(0); // ResTable_package.keyStrings
          buf.putInt(0); // ResTable_package.lastPublicKey
          buf.putInt(0); // ResTable_package.typeIdOffset
        },
        () -> {
          // contents
          ResChunk_header.write(
              buf,
              (short) RES_TABLE_STAGED_ALIAS_TYPE,
              () -> {
                // header
                buf.putInt(1); // ResTableStagedAliasHeader.count
              },
              () -> {
                // contents
                buf.putInt(stagedResId); // ResTableStagedAliasEntry.stagedResId
                buf.putInt(finalizedResId); // ResTableStagedAliasEntry.finalizedResId
              });
        });
    final Chunk chunk = new Chunk(new ResChunk_header(buf, 0));
    final LoadedArsc.LoadedPackage loadedPackage =
        LoadedArsc.LoadedPackage.Load(
            chunk, null /* loaded_idmap */, true /* system */, false /* load_as_shared_library */);

    final Map<Integer, Integer> aliasIdMap = loadedPackage.getAliasResourceIdMap();
    assertEquals(finalizedResId, (int) aliasIdMap.get(stagedResId));
  }
}
