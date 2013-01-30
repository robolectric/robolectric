package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.TEST_PACKAGE;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
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
                new TestAttributeSet(asList(
                        new Attribute("android:attr/inflatedId", "@+id/include_id", TEST_PACKAGE),
                        new Attribute("android:attr/layout", "@layout/media", TEST_PACKAGE)
                ), Robolectric.getShadowApplication().getResourceLoader(), null)
        );

        assertEquals(R.id.include_id, viewStub.getInflatedId());
        assertEquals(R.layout.media, viewStub.getLayoutResource());
    }
}
