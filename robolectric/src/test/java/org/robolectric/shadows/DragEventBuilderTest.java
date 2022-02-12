package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.ClipData;
import android.view.DragEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link DragEventBuilder}. */
@RunWith(AndroidJUnit4.class)
public final class DragEventBuilderTest {
  @Test
  public void obtain() {
    Object localState = new Object();
    ClipData clipData = ClipData.newPlainText("label", "text");

    DragEvent event =
        DragEventBuilder.newBuilder()
            .setAction(DragEvent.ACTION_DRAG_STARTED)
            .setX(1)
            .setY(2)
            .setLocalState(localState)
            .setClipDescription(clipData.getDescription())
            .setClipData(clipData)
            .setResult(true)
            .build();

    assertThat(event.getAction()).isEqualTo(event.getAction());
    assertThat(event.getX()).isEqualTo(1);
    assertThat(event.getY()).isEqualTo(2);
    assertThat(event.getLocalState()).isEqualTo(localState);
    assertThat(event.getClipDescription()).isEqualTo(clipData.getDescription());
    assertThat(event.getClipData()).isEqualTo(clipData);
    assertThat(event.getResult()).isTrue();
  }
}
