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

  // Gets the value associated with a particular bit index.
  static long valueForBit(int n) {
    return 0x8000000000000000L >>> n;
  }

  /** Clears the bit set. */
  void clear() {
    value = 0;
  }

  //
  // static inline void clear(uint64_t& value) { value = 0ULL; }
  //
  // // Returns the number of marked bits in the set.
  int count() {
    int count = 0;
    for (int n = 0; n < 64; n++) {
      if (hasBit(n)) {
        count++;
      }
    }
    return count;
  }
  //
  // static inline uint32_t count(uint64_t value) { return __builtin_popcountll(value); }
  //
  // // Returns true if the bit set does not contain any marked bits.
  // inline bool isEmpty() const { return isEmpty(value); }
  //
  boolean isEmpty() {
    return value == 0;
  }
  //
  // // Returns true if the bit set does not contain any unmarked bits.
  // inline bool isFull() const { return isFull(value); }
  //
  // static inline bool isFull(uint64_t value) { return value == 0xffffffffffffffffULL; }
  //
  // Returns true if the specified bit is marked.
  boolean hasBit(int n) {
    return (value & valueForBit(n)) != 0;
  }

  // Marks the specified bit.
  void markBit(int n) {
    value |= valueForBit(n);
  }

  // Clears the specified bit.
  void clearBit(int n) {
    value &= ~valueForBit(n);
  }

  // Finds the first marked bit in the set.
  // Result is undefined if all bits are unmarked.
  int firstMarkedBit() {
    for (int n = 0; n < 64; n++) {
      if (hasBit(n)) {
        return n;
      }
    }
    return 0;
  }
  //
  // // Finds the first unmarked bit in the set.
  // // Result is undefined if all bits are marked.
  // inline uint32_t firstUnmarkedBit() const { return firstUnmarkedBit(value); }
  //
  // static inline uint32_t firstUnmarkedBit(uint64_t value) { return __builtin_clzll(~ value); }
  //
  // // Finds the last marked bit in the set.
  // // Result is undefined if all bits are unmarked.
  // inline uint32_t lastMarkedBit() const { return lastMarkedBit(value); }
  //
  // static inline uint32_t lastMarkedBit(uint64_t value) { return 63 - __builtin_ctzll(value); }
  //
  // // Finds the first marked bit in the set and clears it.  Returns the bit index.
  // // Result is undefined if all bits are unmarked.
  int clearFirstMarkedBit() {
    int n = firstMarkedBit();
    clearBit(n);
    return n;
  }
  //
  // static inline uint32_t clearFirstMarkedBit(uint64_t& value) {
  //   uint64_t n = firstMarkedBit(value);
  //   clearBit(value, n);
  //   return n;
  // }
  //
  // // Finds the first unmarked bit in the set and marks it.  Returns the bit index.
  // // Result is undefined if all bits are marked.
  // inline uint32_t markFirstUnmarkedBit() { return markFirstUnmarkedBit(value); }
  //
  // static inline uint32_t markFirstUnmarkedBit(uint64_t& value) {
  //   uint64_t n = firstUnmarkedBit(value);
  //   markBit(value, n);
  //   return n;
  // }
  //
  // // Finds the last marked bit in the set and clears it.  Returns the bit index.
  // // Result is undefined if all bits are unmarked.
  // inline uint32_t clearLastMarkedBit() { return clearLastMarkedBit(value); }
  //
  // static inline uint32_t clearLastMarkedBit(uint64_t& value) {
  //   uint64_t n = lastMarkedBit(value);
  //   clearBit(value, n);
  //   return n;
  // }

  // Gets the index of the specified bit in the set, which is the number of
  // marked bits that appear before the specified bit.
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

  //
  // inline bool operator== (const BitSet64& other) const { return value == other.value; }
  // inline bool operator!= (const BitSet64& other) const { return value != other.value; }
  // inline BitSet64 operator& (const BitSet64& other) const {
  //   return BitSet64(value & other.value);
  // }
  // inline BitSet64& operator&= (const BitSet64& other) {
  //   value &= other.value;
  //   return *this;
  // }
  // inline BitSet64 operator| (const BitSet64& other) const {
  //   return BitSet64(value | other.value);
  // }
  // inline BitSet64& operator|= (const BitSet64& other) {
  //   value |= other.value;
  //   return *this;
  // }
}
