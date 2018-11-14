package org.robolectric.shadows;

import android.opengl.GLES20;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Fake implementation of {@link GLES20}
 */
@Implements(GLES20.class)
public class ShadowGLES20 {
  private static int framebufferCount = 0;
  private static int textureCount = 0;
  private static int shaderCount = 0;
  private static int programCount = 0;

  @Implementation
  protected static void glGenFramebuffers(int n, int[] framebuffers, int offset) {
    for (int i = 0; i < n; i++) {
      framebuffers[offset + i] = ++framebufferCount;
    }
  }

  @Implementation
  protected static void glGenTextures(int n, int[] textures, int offset) {
    for (int i = 0; i < n; i++) {
      textures[offset + i] = ++textureCount;
    }
  }

  @Implementation
  protected static int glCreateShader(int type) {
    if (type != GLES20.GL_VERTEX_SHADER && type != GLES20.GL_FRAGMENT_SHADER) {
      return GLES20.GL_INVALID_ENUM;
    }
    return ++shaderCount;
  }

  @Implementation
  protected static int glCreateProgram() {
    return ++programCount;
  }

  @Implementation
  protected static void glGetShaderiv(int shader, int pname, int[] params, int offset) {
    switch (pname) {
      case GLES20.GL_COMPILE_STATUS:
        params[0] = GLES20.GL_TRUE;
        break;
      default:  // no-op
    }
  }

  @Implementation
  protected static void glGetProgramiv(int program, int pname, int[] params, int offset) {
    switch (pname) {
      case GLES20.GL_LINK_STATUS:
        params[0] = GLES20.GL_TRUE;
        break;
      default:  // no-op
    }
  }
}
