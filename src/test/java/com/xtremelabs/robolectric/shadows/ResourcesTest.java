package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestR;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


@RunWith(WithTestDefaultsRunner.class)
public class ResourcesTest {

	private Resources resources;
	private ShadowContextWrapper shadowApp;

	@Before
	public void setup() {
		resources = new Activity().getResources();
		shadowApp = shadowOf( Robolectric.application );		
	}
	
    @Test(expected = Resources.NotFoundException.class)
    public void getStringArray_shouldThrowExceptionIfNotFound() throws Exception {
        resources.getStringArray(-1);
    }

    @Test
    public void testConfiguration() {
        Configuration configuration = resources.getConfiguration();
        assertThat(configuration, notNullValue());
        assertThat(configuration.locale, notNullValue());
    }

    @Test
    public void testConfigurationReturnsTheSameInstance() {
        assertThat(resources.getConfiguration(), is(resources.getConfiguration()));
    }

    @Test
    public void testNewTheme() {
        assertThat(resources.newTheme(), notNullValue());
    }

    /**
     * a missing R.class will result in an BitmapDrawable getting returned
     * by default
     */
    @Test
    public void testGetDrawableNullRClass() {
    	shadowApp.getResourceLoader().setLocalRClass( null );
    	assertThat( resources.getDrawable( TestR.anim.test_anim_1 ), instanceOf( BitmapDrawable.class ) );
    }
    
    /**
     * given an R.anim.id value, will return an AnimationDrawable
     */
    @Test
    public void testGetAnimationDrawable() {
    	shadowApp.getResourceLoader().setLocalRClass( TestR.class );
    	assertThat( resources.getDrawable( TestR.anim.test_anim_1 ), instanceOf( AnimationDrawable.class ) );
    }
    
    /**
     * given an R.color.id value, will return a ColorDrawable
     */
    @Test
    public void testGetColorDrawable() {
    	shadowApp.getResourceLoader().setLocalRClass( TestR.class );
    	assertThat( resources.getDrawable( TestR.color.test_color_1 ), instanceOf( ColorDrawable.class ) );    	
    }

    /**
     * given an R.drawable.id value, will return a BitmapDrawable
     */
    @Test
    public void testGetBitmapDrawable() {
    	shadowApp.getResourceLoader().setLocalRClass( TestR.class );
    	assertThat( resources.getDrawable( TestR.drawable.test_drawable_1 ), instanceOf( BitmapDrawable.class ) );    	    	    	
    }
    
    /**
     * given a value that doesn't in one of R's inner classes, will return a BitmapDrawable 
     */
    @Test
    public void testGetBitmapDrawableForUnknownId() {
    	shadowApp.getResourceLoader().setLocalRClass( TestR.class );
    	assertThat( resources.getDrawable( Integer.MAX_VALUE ), instanceOf( BitmapDrawable.class ) );    	    	
    }
    @Test
    public void testDensity() {
        Activity activity = new Activity();
        assertThat(activity.getResources().getDisplayMetrics().density, equalTo(1f));

        shadowOf(activity.getResources()).setDensity(1.5f);
        assertThat(activity.getResources().getDisplayMetrics().density, equalTo(1.5f));

        Activity anotherActivity = new Activity();
        assertThat(anotherActivity.getResources().getDisplayMetrics().density, equalTo(1.5f));
    }
}
