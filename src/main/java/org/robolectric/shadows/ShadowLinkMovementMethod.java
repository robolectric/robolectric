package org.robolectric.shadows;

import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(LinkMovementMethod.class)
public class ShadowLinkMovementMethod {
    @Implementation
    public static MovementMethod getInstance() {
        return new LinkMovementMethod();
    }
}
