package org.robolectric.shadows;

/**
 * Transliteration of native BitSet64.
 *
 * <p>Unlike the native code stores value inline as opposed to a manipulating data via series of
 * static methods passed values by reference.
 *
 * @see system/core/libutils/include/utils/BitSet.h
 */
public class NativeBitSet64 {

  private long value;

  NativeBitSet64(long value) {
    this.value = value;
  }

  NativeBitSet64(NativeBitSet64 other) {
    this.value = other.value;
  }

  NativeBitSet64() {
    this(0);
  }

  /** Gets the value associated with a particular bit index. */
  static long valueForBit(int n) {
    return 0x8000000000000000L >>> n;
  }

  /** Clears the bit set. */
  void clear() {
    value = 0;
  }

  /** Returns the number of marked bits in the set. */
  int count() {
    int count = 0;
    for (int n = 0; n < 64; n++) {
      if (hasBit(n)) {
        count++;
      }
    }
    return count;
  }

  /** Returns true if the bit set does not contain any marked bits. */
  boolean isEmpty() {
    return value == 0;
  }

  /** Returns true if the specified bit is marked. */
  boolean hasBit(int n) {
    return (value & valueForBit(n)) != 0;
  }

  /** Marks the specified bit. */
  void markBit(int n) {
    value |= valueForBit(n);
  }

  /** Clears the specified bit. */
  void clearBit(int n) {
    value &= ~valueForBit(n);
  }

  /** Finds the first marked bit in the set. Result is undefined if all bits are unmarked. */
  int firstMarkedBit() {
    for (int n = 0; n < 64; n++) {
      if (hasBit(n)) {
        return n;
      }
    }
    return 0;
  }

  /**
   * Finds the first marked bit in the set and clears it. Returns the bit index. Result is undefined
   * if all bits are unmarked.
   */
  int clearFirstMarkedBit() {
    int n = firstMarkedBit();
    clearBit(n);
    return n;
  }

  /**
   * Gets the index of the specified bit in the set, which is the number of marked bits that appear
   * before the specified bit.
   */
  int getIndexOfBit(int n) {
    // return __builtin_popcountll(value & ~(0xffffffffffffffffULL >> n));
    int numMarkedBits = 0;
    for (int i = 0; i < n; i++) {
      if (hasBit(i)) {
        numMarkedBits++;
      }
    }
    return numMarkedBits;
  }

  public void setValue(long l) {
    value = l;
  }

  public long getValue() {
    return value;
  }
}
