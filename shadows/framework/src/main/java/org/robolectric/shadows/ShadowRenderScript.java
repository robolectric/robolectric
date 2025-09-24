package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.RenderScript;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow for {@link RenderScript}. The main purpose of this shadow is to allow creation of
 * RenderScript objects without crashing. Note that RenderScript will never run on Robolectric,
 * because it requires a GPU, and is deprecated.
 */
@Implements(RenderScript.class)
public class ShadowRenderScript {

  /**
   * The real implementation of this method spawns a new background thread, so we shadow at this
   * level to prevent this from happening.
   */
  @Implementation
  protected static RenderScript internalCreate(
      Context ctx, int sdkVersion, RenderScript.ContextType ct, int flags) {
    RenderScript rs =
        ReflectionHelpers.callConstructor(
            RenderScript.class, ClassParameter.from(Context.class, ctx));
    ReflectionHelpers.setField(rs, "mContext", 1L);
    return rs;
  }

  @Implementation
  protected long rsnElementCreate(long con, long type, int kind, boolean norm, int vecSize) {
    return 1;
  }

  @Implementation
  protected long rsnScriptIntrinsicCreate(long con, int id, long eid) {
    return 1;
  }

  @Implementation(minSdk = R)
  protected long rsnAllocationCreateFromBitmap(
      long con, long type, int mip, Bitmap bmp, int usage) {
    return 1;
  }

  @Implementation(minSdk = Q, maxSdk = Q, methodName = "rsnAllocationCreateFromBitmap")
  protected long rsnAllocationCreateFromBitmapQ(
      long con, long type, int mip, long bitmapHandle, int usage) {
    return 1;
  }

  @Implementation(maxSdk = P, methodName = "rsnAllocationCreateFromBitmap")
  protected long rsnAllocationCreateFromBitmapPreQ(
      long con, long type, int mip, Bitmap bmp, int usage) {
    return 1;
  }

  @Implementation(minSdk = R)
  protected long rsnAllocationCreateBitmapBackedAllocation(
      long con, long type, int mip, Bitmap bmp, int usage) {
    return 1;
  }

  @Implementation(minSdk = Q, maxSdk = Q, methodName = "rsnAllocationCreateBitmapBackedAllocation")
  protected long rsnAllocationCreateBitmapBackedAllocationQ(
      long con, long type, int mip, long bitmapHandle, int usage) {
    return 1;
  }

  @Implementation(maxSdk = P, methodName = "rsnAllocationCreateBitmapBackedAllocation")
  protected long rsnAllocationCreateBitmapBackedAllocationPreQ(
      long con, long type, int mip, Bitmap bmp, int usage) {
    return 1;
  }

  @Implementation
  protected long rsnTypeCreate(
      long con, long eid, int x, int y, int z, boolean mips, boolean faces, int yuv) {
    return 1;
  }
}
