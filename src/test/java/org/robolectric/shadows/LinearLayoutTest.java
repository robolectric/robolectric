package org.robolectric.shadows;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class LinearLayoutTest {
    private LinearLayout linearLayout;
    private LinearLayout contextFreeLinearLayout;

    @Before
    public void setup() throws Exception {
        linearLayout = new LinearLayout(Robolectric.application);
        contextFreeLinearLayout = new LinearLayout(null);
    }

    @Test
    public void getLayoutParams_shouldReturnLinearLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams =contextFreeLinearLayout.getLayoutParams();

        assertThat(layoutParams, instanceOf(LinearLayout.LayoutParams.class));
    }

    @Test
    public void getLayoutParams_shouldReturnTheSameLinearLayoutParamsFromTheSetter() throws Exception {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 2);

        contextFreeLinearLayout.setLayoutParams(params);

        assertTrue(contextFreeLinearLayout.getLayoutParams() == params);
    }

    @Test
    public void canAnswerOrientation() throws Exception {
        assertThat(linearLayout.getOrientation(), equalTo(LinearLayout.HORIZONTAL));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        assertThat(linearLayout.getOrientation(), equalTo(LinearLayout.VERTICAL));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        assertThat(linearLayout.getOrientation(), equalTo(LinearLayout.HORIZONTAL));
    }

    @Test
    public void canAnswerGravity() throws Exception {
        assertThat(shadowOf(linearLayout).getGravity(), equalTo(Gravity.TOP | Gravity.START));
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        assertThat(shadowOf(linearLayout).getGravity(), equalTo(Gravity.CENTER_VERTICAL));
    }
}
