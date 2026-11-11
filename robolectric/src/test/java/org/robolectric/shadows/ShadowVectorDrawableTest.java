package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
@GraphicsMode(Mode.LEGACY)
public class ShadowVectorDrawableTest {
  @Test
  public void inflatingAndCopying_doesNotRegisterNativeObjects() {
    Resources resources = ApplicationProvider.getApplicationContext().getResources();
    long before = registerAndRelease();

    Drawable drawable = resources.getDrawable(R.drawable.vector, null);
    drawable.getConstantState().newDrawable().mutate();
    drawable.applyTheme(resources.newTheme());

    // Native ids are handed out sequentially, so any object registered in between would leave a
    // gap.
    assertThat(registerAndRelease()).isEqualTo(before + 1);
  }

  private static long registerAndRelease() {
    long ptr = ShadowVirtualRefBasePtr.put(new Object());
    ShadowVirtualRefBasePtr.nIncStrong(ptr);
    ShadowVirtualRefBasePtr.nDecStrong(ptr);
    return ptr;
  }
}
