package android.graphics;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link Path} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class PathTest {

  @Test
  public void moveTo() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();

    path.moveTo(0, 0);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void quadTo() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    path.quadTo(20.0f, 20.0f, 40.0f, 40.0f);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void close() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    path.close();
  }
}
