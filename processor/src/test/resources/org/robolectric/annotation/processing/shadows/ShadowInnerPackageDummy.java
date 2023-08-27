package org.robolectric.annotation.processing.shadows;

import com.example.objects.innerpackage.InnerPackageDummy;
import org.robolectric.annotation.Implements;

@Implements(InnerPackageDummy.class)
public class ShadowInnerPackageDummy {}
