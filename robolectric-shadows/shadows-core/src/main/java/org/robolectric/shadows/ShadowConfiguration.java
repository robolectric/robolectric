package org.robolectric.shadows;

import android.content.res.Configuration;
import java.util.Locale;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.shadow.api.Shadow.directlyOn;

/**
 * Shadow for {@link android.content.res.Configuration}.
 */
@Implements(Configuration.class)
public class ShadowConfiguration {


}
