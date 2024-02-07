package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link PaintDrawable}. */
@Implements(PaintDrawable.class)
public class ShadowPaintDrawable extends ShadowDrawable {

  @RealObject PaintDrawable realPaintDrawable;

  /** Gets the corder radii */
  public float[] getCornerRadii() {
    Object shapeState = reflector(ShapeDrawableReflector.class, realPaintDrawable).getShapeState();
    Shape shape = reflector(ShapeStateReflector.class, shapeState).getShape();
    if (!(shape instanceof RoundRectShape)) {
      return null;
    }
    RoundRectShape roundRectShape = (RoundRectShape) shape;
    return reflector(RoundRectShapeReflector.class, roundRectShape).getOuterRadii();
  }

  @ForType(ShapeDrawable.class)
  interface ShapeDrawableReflector {
    @Accessor("mShapeState")
    Object getShapeState();
  }

  @ForType(className = "android.graphics.drawable.ShapeDrawable$ShapeState")
  interface ShapeStateReflector {
    @Accessor("mShape")
    Shape getShape();
  }

  @ForType(RoundRectShape.class)
  interface RoundRectShapeReflector {
    @Accessor("mOuterRadii")
    float[] getOuterRadii();
  }
}
