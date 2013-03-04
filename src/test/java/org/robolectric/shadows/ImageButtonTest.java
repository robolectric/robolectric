package org.robolectric.shadows;

import android.widget.ImageButton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.EmptyResourceLoader;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ImageButtonTest {
    @Test
    public void testBackground() throws Exception {
        RoboAttributeSet attrs = new RoboAttributeSet(new ArrayList<Attribute>(), new EmptyResourceLoader(), null);
        ImageButton button = new ImageButton(Robolectric.application, attrs);
        assertThat(button.getBackground()).isNotNull();
    }
}
