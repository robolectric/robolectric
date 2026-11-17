package org.robolectric.shadows;

import android.os.UserHandle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link StorageVolumeBuilder} */
@RunWith(AndroidJUnit4.class)
public class StorageVolumeBuilderTest {

  @Test
  public void testBuilder() {
    UserHandle userHandle = UserHandle.getUserHandleForUid(0);
    new StorageVolumeBuilder("id", new File("path"), "description", userHandle, "").build();
  }
}
