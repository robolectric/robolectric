package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/**
 * Created by jongerrish on 2/10/18.
 */
public class ShadowSystemServiceRegistry {

    @Implements
    static class ShadowStaticApplicationContextServiceFetcher  {
        @Resetter
        public static void reset() {
            //ReflectionHelpers.setStaticField();mCachedInstance;
        }
    }

}
