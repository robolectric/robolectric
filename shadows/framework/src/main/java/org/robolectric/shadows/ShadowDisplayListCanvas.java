package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;

import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.view.DisplayListCanvas} from API versions M to R */
@Implements(
    className = "android.view.DisplayListCanvas",
    isInAndroidSdk = false,
    minSdk = M,
    maxSdk = R)
public class ShadowDisplayListCanvas extends ShadowCanvas {

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nCreateDisplayListCanvas(long node, int width, int height) {
    return 1;
  }

  @Config(minSdk = N, maxSdk = N_MR1)
  protected static long nCreateDisplayListCanvas(int width, int height) {
    return 1;
  }

  @Config(maxSdk = M)
  protected static long nCreateDisplayListCanvas() {
    return 1;
  }
}
