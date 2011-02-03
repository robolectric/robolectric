package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class ViewStubTest {
    private Context ctxt;

    @Before public void setUp() throws Exception {
        ctxt = Robolectric.application;
    }

    @Test
    public void inflate_shouldReplaceOriginalWithLayout() throws Exception {
        ViewStub viewStub = new ViewStub(ctxt);
        int stubId = 12345;
        int inflatedId = 12346;

        viewStub.setId(stubId);
        viewStub.setInflatedId(inflatedId);
        viewStub.setLayoutResource(R.layout.media);

        LinearLayout root = new LinearLayout(ctxt);
        root.addView(new View(ctxt));
        root.addView(viewStub);
        root.addView(new View(ctxt));

        View inflatedView = viewStub.inflate();
        assertNotNull(inflatedView);
        assertSame(inflatedView, root.findViewById(inflatedId));

        assertNull(root.findViewById(stubId));

        assertEquals(1, root.indexOfChild(inflatedView));
        assertEquals(3, root.getChildCount());
    }

    @Test
    public void shouldApplyAttributes() throws Exception {
        ViewStub viewStub = new ViewStub(ctxt,
                new TestAttributeSet()
                        .put("android:inflatedId", "@+id/include_id")
                        .put("android:layout", "@layout/media")
        );

        assertEquals(R.id.include_id, viewStub.getInflatedId());
        assertEquals(R.layout.media, viewStub.getLayoutResource());
    }
}
