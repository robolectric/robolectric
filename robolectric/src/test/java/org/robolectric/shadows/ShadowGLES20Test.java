package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.opengl.GLES20;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link GLES20} */
@RunWith(AndroidJUnit4.class)
public final class ShadowGLES20Test {

  @Test
  public void glGenFramebuffers() {
    int[] framebuffers = new int[1];
    GLES20.glGenFramebuffers(1, framebuffers, 0);
    assertThat(framebuffers[0]).isAtLeast(1);
  }

  @Test
  public void glGenTextures() {
    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    assertThat(textures[0]).isAtLeast(1);
  }

  @Test
  public void glCreateShader_invalidEnum() {
    assertThat(GLES20.glCreateShader(-99999)).isEqualTo(GLES20.GL_INVALID_ENUM);
  }

  @Test
  public void glCreateShader_validEnum() {
    assertThat(GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)).isAtLeast(1);
  }

  @Test
  public void glCreateProgram() {
    assertThat(GLES20.glCreateProgram()).isAtLeast(1);
  }

  @Test
  public void glGetShaderiv_compileStatus() {
    int[] params = new int[1];
    GLES20.glGetShaderiv(1, GLES20.GL_COMPILE_STATUS, params, 0);
    assertThat(params[0]).isEqualTo(GLES20.GL_TRUE);
  }

  @Test
  public void glGetProgramiv_compileStatus() {
    int[] params = new int[1];
    GLES20.glGetProgramiv(1, GLES20.GL_LINK_STATUS, params, 0);
    assertThat(params[0]).isEqualTo(GLES20.GL_TRUE);
  }
}
