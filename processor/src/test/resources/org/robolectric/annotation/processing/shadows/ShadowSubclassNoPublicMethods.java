package org.robolectric.annotation.processing.shadows;

import com.example.objects.OuterDummy;
import org.robolectric.annotation.Implements;

@Implements(OuterDummy.class)
public class ShadowSubclassNoPublicMethods extends ShadowWithPublicMethods {}
