package org.robolectric.annotation.processing.shadows;

import com.example.objects.AnyObject;
import org.robolectric.annotation.Implements;

@Implements(AnyObject.class)
public class ShadowNoPublicMethods {}
