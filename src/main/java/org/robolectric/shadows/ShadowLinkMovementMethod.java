package com.xtremelabs.robolectric.shadows;

import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(LinkMovementMethod.class)
public class ShadowLinkMovementMethod {
    @Implementation
    public static MovementMethod getInstance() {
        return new LinkMovementMethod();
    }
}
