package com.xtremelabs.robolectric.fakes;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.test.MoreAsserts.assertNotEqual;
import static com.xtremelabs.robolectric.RobolectricAndroidTestRunner.proxyFor;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricAndroidTestRunner.class)
public class BitmapDrawableTest {
    private Resources resources;

    @Before
    public void setUp() throws Exception {
        RobolectricAndroidTestRunner.addGenericProxies();

        Application application = new Application();
        resources = application.getResources();
    }

    @Test
    public void equals_shouldTestResourceId() throws Exception {
        Drawable drawable1a = resources.getDrawable(R.drawable.an_image);
        Drawable drawable1b = resources.getDrawable(R.drawable.an_image);
        Drawable drawable2 = resources.getDrawable(R.drawable.an_other_image);

        assertEquals(drawable1a, drawable1b);
        assertNotEqual(drawable1a, drawable2);
    }

    @Test
    public void equals_shouldTestBounds() throws Exception {
        Drawable drawable1a = resources.getDrawable(R.drawable.an_image);
        Drawable drawable1b = resources.getDrawable(R.drawable.an_image);

        drawable1a.setBounds(1, 2, 3, 4);
        drawable1b.setBounds(1, 2, 3, 4);

        assertEquals(drawable1a, drawable1b);

        drawable1b.setBounds(1, 2, 3, 5);
        assertNotEqual(drawable1a, drawable1b);
    }

    @Test
    public void shouldStillHaveProxy() throws Exception {
        Drawable drawable = resources.getDrawable(R.drawable.an_image);
        assertEquals(R.drawable.an_image, ((FakeBitmapDrawable) proxyFor(drawable)).loadedFromResourceId);
    }
}
