package com.xtremelabs.robolectric.res;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.preference.PreferenceScreen;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;


@RunWith(WithTestDefaultsRunner.class)
public class PreferenceLoaderTest {
	private PreferenceLoader prefLoader;
	
    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
        
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
        prefLoader = new PreferenceLoader(resourceExtractor);
        new DocumentLoader(prefLoader).loadResourceXmlDir(resourceFile("res", "xml"));    
    }
    
    @Test
    public void testCreatesCorrectClasses() throws Exception {
    	PreferenceScreen screen = prefLoader.inflatePrefs(new Activity(), "xml/preferences");
    	
    }
}
