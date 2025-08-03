package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.view.TextureView;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Logger;

/** Shadow for {@link TextureView}. */
@Implements(value = TextureView.class)
public class ShadowTextureView extends ShadowView {
  @Implementation(minSdk = P, maxSdk = R)
  protected @ClassName("android.view.TextureLayer") Object getTextureLayer() {
    // TextureView uses OpenGL, which is not supported by Robolectric. It will try to use
    // nCreateTextureLayer, but that will always return 0 since OpenGL is not supported.
    // NoOp this code so it doesn't crash trying to use the null TextureLayer from OpenGL.
    Logger.error("ShadowTextureView", "TextureView is not supported in Robolectric.");
    return null;
  }

  @Implementation(methodName = "getTextureLayer", minSdk = S)
  protected @ClassName("android.graphics.TextureLayer") Object getTextureLayerRPlus() {
    // TextureView uses OpenGL, which is not supported by Robolectric. It will try to use
    // nCreateTextureLayer, but that will always return 0 since OpenGL is not supported.
    // NoOp this code so it doesn't crash trying to use the null TextureLayer from OpenGL.
    Logger.error("ShadowTextureView", "TextureView is not supported in Robolectric.");
    return null;
  }
}
