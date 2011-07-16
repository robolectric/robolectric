package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TabHost;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class TabSpecTest {
	Drawable icon1;
	Drawable icon2;
	@Before
	public void init() {
		 icon1 = new Drawable() {

				@Override
				public void draw(Canvas canvas) {
				}

				@Override
				public void setAlpha(int alpha) {
				}

				@Override
				public void setColorFilter(ColorFilter cf) {
				}

				@Override
				public int getOpacity() {
					return 1;
				}
	        
	        };
		 icon2 = new Drawable() {

			@Override
			public void draw(Canvas canvas) {
			}

			@Override
			public void setAlpha(int alpha) {
			}

			@Override
			public void setColorFilter(ColorFilter cf) {
			}

			@Override
			public int getOpacity() {
				return 2;
			}
		 
		 };
	}
	
    @Test
    public void shouldGetAndSetTheIndicator() throws Exception {
        TabHost.TabSpec spec = new TabHost(null).newTabSpec("foo");
        View view = new View(null);
        TabHost.TabSpec self = spec.setIndicator(view);
        assertThat(self, is(spec));
        assertThat(shadowOf(spec).getIndicatorAsView(), is(view));
    }

    @Test
    public void shouldGetAndSetTheIntentContent() throws Exception {
        TabHost.TabSpec spec = new TabHost(null).newTabSpec("foo");
        Intent intent = new Intent();
        TabHost.TabSpec self = spec.setContent(intent);
        assertThat(self, is(spec));
        assertThat(shadowOf(spec).getContentAsIntent(), is(intent));
    }
    
    @Test
    public void shouldGetAndSetTheIndicatorLabel() throws Exception {
        TabHost.TabSpec spec = new TabHost(null).newTabSpec("foo")
        .setContent(R.layout.main).setIndicator("labelText");

        assertThat(shadowOf(spec).getIndicatorLabel(), is("labelText"));
        assertThat(shadowOf(spec).getText(), is("labelText"));
    }
    @Test
    public void shouldGetAndSetTheIndicatorLabelAndIcon() throws Exception {
        TabHost.TabSpec spec = new TabHost(null).newTabSpec("foo")
        .setContent(R.layout.main).setIndicator("labelText",icon1);

        assertThat(shadowOf(spec).getIndicatorLabel(), is("labelText"));
        assertThat(shadowOf(spec).getText(), is("labelText"));
        assertThat(shadowOf(spec).getIndicatorIcon(), is(icon1));
        assertThat(shadowOf(spec).getIndicatorIcon(), is(not(icon2)));
    }
}
