package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link StorageVolumeBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.N)
public class StorageVolumeBuilderTest {

  @Test
  public void testBuilder() {
    UserHandle userHandle = UserHandle.getUserHandleForUid(0);
    new StorageVolumeBuilder("id", new File("path"), "description", userHandle, "").build();
  }
}
