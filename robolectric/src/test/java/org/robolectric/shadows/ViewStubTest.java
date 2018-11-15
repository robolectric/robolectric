package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ViewStubTest {
  private Context ctxt;

  @Before public void setUp() throws Exception {
    ctxt = ApplicationProvider.getApplicationContext();
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
        Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.inflatedId, "@+id/include_id")
            .addAttribute(android.R.attr.layout, "@layout/media")
            .build());

    assertThat(viewStub.getInflatedId()).isEqualTo(R.id.include_id);
    assertThat(viewStub.getLayoutResource()).isEqualTo(R.layout.media);
  }
}
