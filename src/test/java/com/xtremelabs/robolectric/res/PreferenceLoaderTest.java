package com.xtremelabs.robolectric.res;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.I18nException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

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
    public void shouldCreateCorrectClasses() {
    	PreferenceScreen screen = prefLoader.inflatePreferences(new Activity(), "xml/preferences"); 	
    	assertThatScreenMatchesExpected(screen);
    }
    
    @Test
    public void shouldLoadByResourceId() {
       	PreferenceScreen screen = prefLoader.inflatePreferences(new Activity(), R.xml.preferences); 	
       	assertThatScreenMatchesExpected(screen); 	
    }
    
    @Test(expected=I18nException.class) 
    public void shouldThrowI18nExceptionOnPrefsWithBareStrings() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
        prefLoader = new PreferenceLoader(resourceExtractor);
        prefLoader.setStrictI18n(true);
        new DocumentLoader(prefLoader).loadResourceXmlDir(resourceFile("res", "xml"));   

        prefLoader.inflatePreferences(Robolectric.application, R.xml.preferences);
    }
    
    protected void assertThatScreenMatchesExpected(PreferenceScreen screen) {
    	assertThat(screen.getPreferenceCount(), equalTo(6));
    	
    	assertThat(screen.getPreference(0), instanceOf(PreferenceCategory.class));
    	assertThat(((PreferenceCategory)screen.getPreference(0)).getPreference(0), instanceOf(Preference.class));
   	  	
    	assertThat(screen.getPreference(1), instanceOf(CheckBoxPreference.class));
    	assertThat(screen.getPreference(2), instanceOf(EditTextPreference.class));
    	assertThat(screen.getPreference(3), instanceOf(ListPreference.class));
    	assertThat(screen.getPreference(4), instanceOf(Preference.class));
    	assertThat(screen.getPreference(5), instanceOf(RingtonePreference.class));   	
    }
}
