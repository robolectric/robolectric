package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class BitmapDrawableTest {
    private Resources resources;

    @Before
    public void setUp() throws Exception {
        resources = Robolectric.application.getResources();
    }

    @Test
    public void constructors_shouldSetBitmap() throws Exception {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        assertEquals(bitmap, drawable.getBitmap());

        drawable = new BitmapDrawable(resources, bitmap);
        assertEquals(bitmap, drawable.getBitmap());
    }

    @Test
    public void getBitmap_shouldReturnBitmapUsedToDraw() throws Exception {
        BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
        assertEquals("Bitmap for resource:com.xtremelabs.robolectric:drawable/an_image", shadowOf(drawable.getBitmap()).getDescription());
    }

    @Test
    public void mutate_createsDeepCopy() throws Exception {
        BitmapDrawable original = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
        Drawable mutated = original.mutate();
        assertNotSame(original, mutated);
        assertTrue(mutated instanceof BitmapDrawable);
        assertEquals(original, mutated);
    }

    @Test
    public void draw_shouldCopyDescriptionToCanvas() throws Exception {
        BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
        Canvas canvas = new Canvas();
        drawable.draw(canvas);

        assertEquals("Bitmap for resource:com.xtremelabs.robolectric:drawable/an_image", shadowOf(canvas).getDescription());
    }

    @Test
    public void shouldInheritSourceStringFromDrawableDotCreateFromStream() throws Exception {
        InputStream emptyInputStream = new ByteArrayInputStream("".getBytes());
        BitmapDrawable drawable = (BitmapDrawable) Drawable.createFromStream(emptyInputStream, "source string value");
        assertEquals("source string value", shadowOf(drawable).getSource());
    }

    @Test
    public void withColorFilterSet_draw_shouldCopyDescriptionToCanvas() throws Exception {
        BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
        drawable.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
        Canvas canvas = new Canvas();
        drawable.draw(canvas);

        assertEquals("Bitmap for resource:com.xtremelabs.robolectric:drawable/an_image with ColorMatrixColorFilter<1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0>",
                shadowOf(canvas).getDescription());
    }

    @Test
    public void equals_shouldTestResourceId() throws Exception {
        Drawable drawable1a = resources.getDrawable(R.drawable.an_image);
        Drawable drawable1b = resources.getDrawable(R.drawable.an_image);
        Drawable drawable2 = resources.getDrawable(R.drawable.an_other_image);

        assertEquals(drawable1a, drawable1b);
        assertFalse(drawable1a.equals(drawable2));
    }

    @Test
    public void equals_shouldTestBounds() throws Exception {
        Drawable drawable1a = resources.getDrawable(R.drawable.an_image);
        Drawable drawable1b = resources.getDrawable(R.drawable.an_image);

        drawable1a.setBounds(1, 2, 3, 4);
        drawable1b.setBounds(1, 2, 3, 4);

        assertEquals(drawable1a, drawable1b);

        drawable1b.setBounds(1, 2, 3, 5);
        assertFalse(drawable1a.equals(drawable1b));
    }

    @Test
    public void shouldStillHaveShadow() throws Exception {
        Drawable drawable = resources.getDrawable(R.drawable.an_image);
        assertEquals(R.drawable.an_image, ((ShadowBitmapDrawable) Robolectric.shadowOf(drawable)).getLoadedFromResourceId());
    }

    @Test
    public void shouldSetTileModeXY() throws Exception {
        BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
        drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.MIRROR);
        assertEquals(Shader.TileMode.REPEAT, drawable.getTileModeX());
        assertEquals(Shader.TileMode.MIRROR, drawable.getTileModeY());
    }
}
