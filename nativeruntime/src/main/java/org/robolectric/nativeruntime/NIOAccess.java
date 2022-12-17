/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.nativeruntime;

import static org.robolectric.util.reflector.Reflector.reflector;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Analogue to libcore's <a
 * href="https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:libcore/luni/src/main/java/java/nio/NIOAccess.java">NIOAccess</a>,
 * which provides access to some internal methods and properties of {@link Buffer}. These methods
 * are designed to work on the JVM and get called from native code such as libnativehelper.
 */
public final class NIOAccess {

  private NIOAccess() {}

  /**
   * Returns the underlying native pointer to the data of the given Buffer starting at the Buffer's
   * current position, or 0 if the Buffer is not backed by native heap storage.
   */
  public static long getBasePointer(Buffer b) {
    long address = reflector(BufferReflector.class, b).getAddress();

    if (address == 0L || !b.isDirect()) {
      return 0L;
    }
    return address + ((long) b.position() << elementSizeShift(b));
  }

  /**
   * Returns the underlying Java array containing the data of the given Buffer, or null if the
   * Buffer is not backed by a Java array.
   */
  static Object getBaseArray(Buffer b) {
    return b.hasArray() ? b.array() : null;
  }

  /**
   * Returns the offset in bytes from the start of the underlying Java array object containing the
   * data of the given Buffer to the actual start of the data. The start of the data takes into
   * account the Buffer's current position. This method is only meaningful if getBaseArray() returns
   * non-null.
   */
  static int getBaseArrayOffset(Buffer b) {
    return b.hasArray() ? ((b.arrayOffset() + b.position()) << elementSizeShift(b)) : 0;
  }

  /**
   * The Android version of java.nio.Buffer has an extra final field called _elementSizeShift that
   * only depend on the implementation of the buffer. This method can be called instead when wanting
   * to access the value of that field on the JVM.
   */
  public static int elementSizeShift(Buffer buffer) {
    if (buffer instanceof ByteBuffer) {
      return 0;
    }
    if (buffer instanceof ShortBuffer || buffer instanceof CharBuffer) {
      return 1;
    }
    if (buffer instanceof IntBuffer || buffer instanceof FloatBuffer) {
      return 2;
    }
    if (buffer instanceof LongBuffer || buffer instanceof DoubleBuffer) {
      return 3;
    }
    return 0;
  }

  @ForType(Buffer.class)
  interface BufferReflector {

    @Accessor("address")
    long getAddress();
  }
}
