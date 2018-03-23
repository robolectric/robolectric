/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package org.robolectric.res.android;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.UnsignedBytes;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/** Provides utilities to decode/encode a String packed in an arsc resource file. */
public final class ResourceString {

  /** Type of {@link ResourceString} to encode / decode. */
  public enum Type {
    UTF8(UTF_8),
    UTF16(UTF_16LE);

    private final Charset charset;

    Type(Charset charset) {
      this.charset = charset;
    }

    public Charset charset() {
      return charset;
    }
  }

  private ResourceString() {} // Private constructor

  /**
   * Given a buffer and an offset into the buffer, returns a String. The {@code offset} is the
   * 0-based byte offset from the start of the buffer where the string resides. This should be the
   * location in memory where the string's character count, followed by its byte count, and then
   * followed by the actual string is located.
   *
   * <p>Here's an example UTF-8-encoded string of ab©:
   * <pre>
   * 03 04 61 62 C2 A9 00
   * ^ Offset should be here
   * </pre>
   *
   * @param buffer The buffer containing the string to decode.
   * @param offset Offset into the buffer where the string resides.
   * @param type The encoding type that the {@link ResourceString} is encoded in.
   * @return The decoded string.
   */
  public static String decodeString(ByteBuffer buffer, int offset, Type type) {
    int length;
    int characterCount = decodeLength(buffer, offset, type);
    offset += computeLengthOffset(characterCount, type);
    // UTF-8 strings have 2 lengths: the number of characters, and then the encoding length.
    // UTF-16 strings, however, only have 1 length: the number of characters.
    if (type == Type.UTF8) {
      length = decodeLength(buffer, offset, type);
      offset += computeLengthOffset(length, type);
    } else {
      length = characterCount * 2;
    }
    return new String(buffer.array(), offset, length, type.charset());
  }

  /**
   * Encodes a string in either UTF-8 or UTF-16 and returns the bytes of the encoded string.
   * Strings are prefixed by 2 values. The first is the number of characters in the string.
   * The second is the encoding length (number of bytes in the string).
   *
   * <p>Here's an example UTF-8-encoded string of ab©:
   * <pre>03 04 61 62 C2 A9 00</pre>
   *
   * @param str The string to be encoded.
   * @param type The encoding type that the {@link ResourceString} should be encoded in.
   * @return The encoded string.
   */
  public static byte[] encodeString(String str, Type type) {
    byte[] bytes = str.getBytes(type.charset());
    // The extra 5 bytes is for metadata (character count + byte count) and the NULL terminator.
    ByteArrayDataOutput output = ByteStreams.newDataOutput(bytes.length + 5);
    encodeLength(output, str.length(), type);
    if (type == Type.UTF8) {  // Only UTF-8 strings have the encoding length.
      encodeLength(output, bytes.length, type);
    }
    output.write(bytes);
    // NULL-terminate the string
    if (type == Type.UTF8) {
      output.write(0);
    } else {
      output.writeShort(0);
    }
    return output.toByteArray();
  }

  /**
   * Builds a string from a null-terminated char data.
   */
  public static String buildString(char[] data) {
    int count = 0;
    for (count=0; count < data.length; count++) {
      if (data[count] == 0) {
        break;
      }
    }
    return new String(data, 0, count);
  }

  private static void encodeLength(ByteArrayDataOutput output, int length, Type type) {
    if (length < 0) {
      output.write(0);
      return;
    }
    if (type == Type.UTF8) {
      if (length > 0x7F) {
        output.write(((length & 0x7F00) >> 8) | 0x80);
      }
      output.write(length & 0xFF);
    } else {  // UTF-16
      // TODO(acornwall): Replace output with a little-endian output.
      if (length > 0x7FFF) {
        int highBytes = ((length & 0x7FFF0000) >> 16) | 0x8000;
        output.write(highBytes & 0xFF);
        output.write((highBytes & 0xFF00) >> 8);
      }
      int lowBytes = length & 0xFFFF;
      output.write(lowBytes & 0xFF);
      output.write((lowBytes & 0xFF00) >> 8);
    }
  }

  static int computeLengthOffset(int length, Type type) {
    return (type == Type.UTF8 ? 1 : 2) * (length >= (type == Type.UTF8 ? 0x80 : 0x8000) ? 2 : 1);
  }

  static int decodeLength(ByteBuffer buffer, int offset, Type type) {
    return type == Type.UTF8 ? decodeLengthUTF8(buffer, offset) : decodeLengthUTF16(buffer, offset);
  }

  static int decodeLengthUTF8(ByteBuffer buffer, int offset) {
    // UTF-8 strings use a clever variant of the 7-bit integer for packing the string length.
    // If the first byte is >= 0x80, then a second byte follows. For these values, the length
    // is WORD-length in big-endian & 0x7FFF.
    int length = UnsignedBytes.toInt(buffer.get(offset));
    if ((length & 0x80) != 0) {
      length = ((length & 0x7F) << 8) | UnsignedBytes.toInt(buffer.get(offset + 1));
    }
    return length;
  }

  static int decodeLengthUTF16(ByteBuffer buffer, int offset) {
    // UTF-16 strings use a clever variant of the 7-bit integer for packing the string length.
    // If the first word is >= 0x8000, then a second word follows. For these values, the length
    // is DWORD-length in big-endian & 0x7FFFFFFF.
    int length = (buffer.getShort(offset) & 0xFFFF);
    if ((length & 0x8000) != 0) {
      length = ((length & 0x7FFF) << 16) | (buffer.getShort(offset + 2) & 0xFFFF);
    }
    return length;
  }
}