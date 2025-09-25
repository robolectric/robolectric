package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.RenderScript;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link RenderScript}. The main purpose of this shadow is to allow creation of
 * RenderScript objects without crashing. Note that RenderScript will never run on Robolectric,
 * because it requires a GPU, and is deprecated.
 */
@Implements(RenderScript.class)
public class ShadowRenderScript {

  @RealObject RenderScript realRenderScript;
  private boolean isDestroyed;

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

    // Initialize the `mMessageThread` field to ensure that the RenderScript.destroy doesn't throw
    // an NPE.
    Class<?> messageThreadClass =
        ReflectionHelpers.loadClass(
            Shadow.class.getClassLoader(), "android.renderscript.RenderScript$MessageThread");
    ReflectionHelpers.setField(
        rs,
        "mMessageThread",
        ReflectionHelpers.callConstructor(
            messageThreadClass, ClassParameter.from(RenderScript.class, rs)));
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

  @Implementation
  protected long rsnAllocationCreateTyped(long con, long type, int mip, int usage, long pointer) {
    return 1;
  }

  @Implementation
  protected void destroy() {
    isDestroyed = true;

    reflector(RenderScriptReflector.class, realRenderScript).destroy();
  }

  public boolean isDestroyed() {
    return isDestroyed;
  }

  @ForType(RenderScript.class)
  interface RenderScriptReflector {
    @Direct
    void destroy();
  }
}
