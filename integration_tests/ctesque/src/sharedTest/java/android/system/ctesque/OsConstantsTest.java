package android.system.ctesque;

import static com.google.common.truth.Truth.assertThat;

import android.system.OsConstants;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests to verify {@link OsConstants} is consistent on both Robolectric and emulator. */
@RunWith(AndroidJUnit4.class)
public final class OsConstantsTest {

  // list of OsConstants field names that are expected to be 0
  private static final Set<String> zeroValFields =
      Set.of(
          "AF_UNSPEC",
          "CAP_CHOWN",
          "EXIT_SUCCESS",
          "F_DUPFD",
          "F_OK",
          "F_RDLCK",
          "ICMP_ECHOREPLY",
          "IPPROTO_IP",
          "NETLINK_ROUTE",
          "O_RDONLY",
          "PROT_NONE",
          "RT_SCOPE_UNIVERSE",
          "SEEK_SET",
          "SHUT_RD",
          "STDIN_FILENO",
          "_SC_ARG_MAX",
          "MADV_NORMAL",
          "MADV_RANDOM",
          "MADV_SEQUENTIAL",
          "MADV_WILLNEED",
          "MADV_DONTNEED",
          "MADV_REMOVE",
          "MADV_DONTFORK",
          "MADV_DOFORK",
          "MADV_HWPOISON",
          "MADV_MERGEABLE",
          "MADV_UNMERGEABLE",
          "MADV_SOFT_OFFLINE",
          "MADV_HUGEPAGE",
          "MADV_NOHUGEPAGE",
          "MADV_COLLAPSE",
          "MADV_DONTDUMP",
          "MADV_DODUMP",
          "MADV_FREE",
          "MADV_WIPEONFORK",
          "MADV_KEEPONFORK",
          "MADV_COLD",
          "MADV_PAGEOUT",
          "MADV_POPULATE_READ",
          "MADV_POPULATE_WRITE");

  @Test
  public void valuesAreDistinct() throws Exception {
    assertThat(OsConstants.errnoName(OsConstants.EAGAIN)).isEqualTo("EAGAIN");
    assertThat(OsConstants.errnoName(OsConstants.EBADF)).isEqualTo("EBADF");
  }

  // spot check a few constants
  @Test
  public void valuesAreExpected() {
    assertThat(OsConstants.S_IFMT).isEqualTo(0x000f000);
    assertThat(OsConstants.S_IFDIR).isEqualTo(0x0004000);
    assertThat(OsConstants.S_IFREG).isEqualTo(0x0008000);
    assertThat(OsConstants.S_IFLNK).isEqualTo(0x000a000);
  }

  // spot check constants are non-zero. This is to check case where a new constant
  // is added in a new SDK and there isn't support to populate it in the shadow yet
  @Test
  public void checkUninitialized() throws IllegalAccessException {
    List<String> uninitializedFields = new ArrayList<>();
    for (Field field : OsConstants.class.getDeclaredFields()) {

      if (field.getType().equals(int.class) && Modifier.isStatic(field.getModifiers())) {
        if (field.getInt(null) == 0) {
          uninitializedFields.add(field.getName());
        }
      }
    }
    uninitializedFields.removeAll(zeroValFields);
    assertThat(uninitializedFields).isEmpty();
  }
}
