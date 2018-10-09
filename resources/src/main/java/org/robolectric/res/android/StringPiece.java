package org.robolectric.res.android;

import java.nio.ByteBuffer;
import org.robolectric.res.android.ResourceTypes.Idmap_header;
import org.robolectric.res.android.ResourceTypes.WithOffset;

public class StringPiece extends WithOffset {

  StringPiece(ByteBuffer buf, int offset) {
    super(buf, offset);
  }

  public int size() {
    return myBuf().capacity() - myOffset();
  }

  public Idmap_header asIdmap_header() {
    return new Idmap_header(myBuf(), myOffset());
  }
}
