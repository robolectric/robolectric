package org.robolectric.res.builder;

import org.robolectric.res.ResourceLoader;

/**
 * Returns a {@link ResourceLoader} representing the resource table that the XML was loaded with.
 */
public interface ResourceLoaderProvider {
    ResourceLoader getResourceLoader();
}
