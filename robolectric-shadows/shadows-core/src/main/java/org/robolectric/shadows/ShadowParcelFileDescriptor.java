package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Shadow for {@link android.os.ParcelFileDescriptor}.
 */
@Implements(ParcelFileDescriptor.class)
public class ShadowParcelFileDescriptor {
  @RealObject
  ParcelFileDescriptor realObject;

  @Implementation
  public long getStatSize() {
    try (FileInputStream in = new FileInputStream(realObject.getFileDescriptor())) {
      return in.getChannel().size();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
