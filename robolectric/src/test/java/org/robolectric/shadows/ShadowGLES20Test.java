package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.opengl.GLES20;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Test for {@link GLES20}
 */
@RunWith(RobolectricTestRunner.class)
public final class ShadowGLES20Test {

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
}
