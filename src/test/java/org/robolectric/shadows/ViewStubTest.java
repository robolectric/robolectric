package org.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;

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
        new RoboAttributeSet(asList(
            new Attribute("android:attr/inflatedId", "@+id/include_id", TEST_PACKAGE),
            new Attribute("android:attr/layout", "@layout/media", TEST_PACKAGE)
        ), Robolectric.application.getResources(), null)
    );

    assertThat(viewStub.getInflatedId()).isEqualTo(R.id.include_id);
    assertThat(viewStub.getLayoutResource()).isEqualTo(R.layout.media);
  }
}
