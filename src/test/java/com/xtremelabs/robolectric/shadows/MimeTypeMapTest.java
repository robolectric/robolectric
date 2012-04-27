package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.webkit.MimeTypeMap;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class MimeTypeMapTest {

    private static final String IMAGE_EXTENSION = "jpg";
    private static final String VIDEO_EXTENSION = "mp4";
    private static final String VIDEO_MIMETYPE = "video/mp4";
    private static final String IMAGE_MIMETYPE = "image/jpeg";

    @After
    public void cleanup() throws Exception {
        Robolectric.shadowOf(MimeTypeMap.getSingleton()).clearMappings();
    }
    
    @Test
    public void getSingletonShouldAlwaysReturnSameInstance() {
        MimeTypeMap firstInstance = MimeTypeMap.getSingleton();
        MimeTypeMap secondInstance = MimeTypeMap.getSingleton();
        
        assertSame(firstInstance, secondInstance);
    }
    
    @Test
    public void byDefaultThereShouldBeNoMapping() {        
        assertFalse(MimeTypeMap.getSingleton().hasExtension(VIDEO_EXTENSION));        
        assertFalse(MimeTypeMap.getSingleton().hasExtension(IMAGE_EXTENSION));
    }
    
    @Test
    public void addingMappingShouldWorkCorrectly() {
        ShadowMimeTypeMap shadowMimeTypeMap = Robolectric.shadowOf(MimeTypeMap.getSingleton());
        shadowMimeTypeMap.addExtensionMimeTypMapping(VIDEO_EXTENSION, VIDEO_MIMETYPE);
        shadowMimeTypeMap.addExtensionMimeTypMapping(IMAGE_EXTENSION, IMAGE_MIMETYPE);
        
        assertTrue(MimeTypeMap.getSingleton().hasExtension(VIDEO_EXTENSION));        
        assertTrue(MimeTypeMap.getSingleton().hasExtension(IMAGE_EXTENSION));
        assertTrue(MimeTypeMap.getSingleton().hasMimeType(VIDEO_MIMETYPE));        
        assertTrue(MimeTypeMap.getSingleton().hasMimeType(IMAGE_MIMETYPE));

        assertEquals(IMAGE_EXTENSION, MimeTypeMap.getSingleton().getExtensionFromMimeType(IMAGE_MIMETYPE));
        assertEquals(VIDEO_EXTENSION, MimeTypeMap.getSingleton().getExtensionFromMimeType(VIDEO_MIMETYPE));
        
        assertEquals(IMAGE_MIMETYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(IMAGE_EXTENSION));
        assertEquals(VIDEO_MIMETYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(VIDEO_EXTENSION));
    }
    
    @Test
    public void clearMappingsShouldRemoveAllMappings() {
        ShadowMimeTypeMap shadowMimeTypeMap = Robolectric.shadowOf(MimeTypeMap.getSingleton());
        shadowMimeTypeMap.addExtensionMimeTypMapping(VIDEO_EXTENSION, VIDEO_MIMETYPE);
        shadowMimeTypeMap.addExtensionMimeTypMapping(IMAGE_EXTENSION, IMAGE_MIMETYPE);
    
        shadowMimeTypeMap.clearMappings();
        
        assertFalse(MimeTypeMap.getSingleton().hasExtension(VIDEO_EXTENSION));        
        assertFalse(MimeTypeMap.getSingleton().hasExtension(IMAGE_EXTENSION));
        assertFalse(MimeTypeMap.getSingleton().hasMimeType(VIDEO_MIMETYPE));        
        assertFalse(MimeTypeMap.getSingleton().hasExtension(IMAGE_MIMETYPE));
    }
    
    @Test
    public void unknownExtensionShouldProvideNothing() {
        ShadowMimeTypeMap shadowMimeTypeMap = Robolectric.shadowOf(MimeTypeMap.getSingleton());
        shadowMimeTypeMap.addExtensionMimeTypMapping(VIDEO_EXTENSION, VIDEO_MIMETYPE);
        shadowMimeTypeMap.addExtensionMimeTypMapping(IMAGE_EXTENSION, IMAGE_MIMETYPE);
        
        assertFalse(MimeTypeMap.getSingleton().hasExtension("foo"));
        assertNull(MimeTypeMap.getSingleton().getMimeTypeFromExtension("foo"));
    }
    
    @Test
    public void unknownMimeTypeShouldProvideNothing() {
        ShadowMimeTypeMap shadowMimeTypeMap = Robolectric.shadowOf(MimeTypeMap.getSingleton());
        shadowMimeTypeMap.addExtensionMimeTypMapping(VIDEO_EXTENSION, VIDEO_MIMETYPE);
        shadowMimeTypeMap.addExtensionMimeTypMapping(IMAGE_EXTENSION, IMAGE_MIMETYPE);
        
        assertFalse(MimeTypeMap.getSingleton().hasMimeType("foo/bar"));
        assertNull(MimeTypeMap.getSingleton().getExtensionFromMimeType("foo/bar"));
    }
}
