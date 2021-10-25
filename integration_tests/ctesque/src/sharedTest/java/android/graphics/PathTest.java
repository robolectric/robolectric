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

  // Test constants
  private static final float LEFT = 10.0f;
  private static final float RIGHT = 50.0f;
  private static final float TOP = 10.0f;
  private static final float BOTTOM = 50.0f;
  private static final float XCOORD = 40.0f;
  private static final float YCOORD = 40.0f;

  @Test
  public void moveTo() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();

    path.moveTo(0, 0);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void lineTo() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    path.lineTo(XCOORD, YCOORD);
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
  public void addRect1() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    RectF rect = new RectF(LEFT, TOP, RIGHT, BOTTOM);
    path.addRect(rect, Path.Direction.CW);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void addRect2() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    path.addRect(LEFT, TOP, RIGHT, BOTTOM, Path.Direction.CW);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void getFillType() {
    Path path = new Path();
    path.setFillType(Path.FillType.EVEN_ODD);
    assertThat(path.getFillType()).isEqualTo(Path.FillType.EVEN_ODD);
  }

  @Test
  public void transform() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();

    Path dst = new Path();
    path.addRect(new RectF(LEFT, TOP, RIGHT, BOTTOM), Path.Direction.CW);
    path.transform(new Matrix(), dst);

    assertThat(dst.isEmpty()).isFalse();
  }

  @Test
  public void testAddCircle() {
    // new the Path instance
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    path.addCircle(XCOORD, YCOORD, 10.0f, Path.Direction.CW);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void arcTo1() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    RectF oval = new RectF(LEFT, TOP, RIGHT, BOTTOM);
    path.arcTo(oval, 0.0f, 30.0f, true);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void arcTo2() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    RectF oval = new RectF(LEFT, TOP, RIGHT, BOTTOM);
    path.arcTo(oval, 0.0f, 30.0f);
    assertThat(path.isEmpty()).isFalse();
  }

  @Test
  public void close() {
    Path path = new Path();
    assertThat(path.isEmpty()).isTrue();
    path.close();
  }

  @Test
  public void invalidArc_doesNotNPE() {
    Path path = new Path();
    // This arc is invalid because the bounding rectangle has left > right and top > bottom.
    path.arcTo(new RectF(1, 1, 0, 0), 0, 30);
    assertThat(path.isEmpty()).isTrue();
  }
}
