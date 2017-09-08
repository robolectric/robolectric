package org.robolectric.res.android;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.primitives.UnsignedBytes;
import java.util.Map;

/** Resource type codes. */
public enum DataType {
  /** {@code data} is either 0 (undefined) or 1 (empty). */
  NULL(0x00),
  /** {@code data} holds a {@link ResourceTableChunk} entry reference. */
  REFERENCE(0x01),
  /** {@code data} holds an attribute resource identifier. */
  ATTRIBUTE(0x02),
  /** {@code data} holds an index into the containing resource table's string pool. */
  STRING(0x03),
  /** {@code data} holds a single-precision floating point number. */
  FLOAT(0x04),
  /** {@code data} holds a complex number encoding a dimension value, such as "100in". */
  DIMENSION(0x05),
  /** {@code data} holds a complex number encoding a fraction of a container. */
  FRACTION(0x06),
  /** {@code data} holds a dynamic {@link ResourceTableChunk} entry reference. */
  DYNAMIC_REFERENCE(0x07),
  /** {@code data} holds an attribute resource identifier, which needs to be resolved
    * before it can be used like a TYPE_ATTRIBUTE.
    */
  DYNAMIC_ATTRIBUTE(0x08),
  /** {@code data} is a raw integer value of the form n..n. */
  INT_DEC(0x10),
  /** {@code data} is a raw integer value of the form 0xn..n. */
  INT_HEX(0x11),
  /** {@code data} is either 0 (false) or 1 (true). */
  INT_BOOLEAN(0x12),
  /** {@code data} is a raw integer value of the form #aarrggbb. */
  INT_COLOR_ARGB8(0x1c),
  /** {@code data} is a raw integer value of the form #rrggbb. */
  INT_COLOR_RGB8(0x1d),
  /** {@code data} is a raw integer value of the form #argb. */
  INT_COLOR_ARGB4(0x1e),
  /** {@code data} is a raw integer value of the form #rgb. */
  INT_COLOR_RGB4(0x1f);

  public static final int TYPE_FIRST_INT = INT_DEC.code();
  public static final int TYPE_LAST_INT = INT_COLOR_RGB4.code();

  private final byte code;

  private static final Map<Byte, DataType> FROM_BYTE;

  static {
    Builder<Byte, DataType> builder = ImmutableMap.builder();
    for (DataType type : values()) {
      builder.put(type.code(), type);
    }
    FROM_BYTE = builder.build();
  }

  DataType(int code) {
    this.code = UnsignedBytes.checkedCast(code);
  }

  public byte code() {
    return code;
  }

  public static DataType fromCode(int code) {
    return fromCode((byte) code);
  }

  public static DataType fromCode(byte code) {
    return Preconditions.checkNotNull(FROM_BYTE.get(code), "Unknown resource type: %s", code);
  }
}
