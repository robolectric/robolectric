package org.robolectric.res.android;

import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;

import java.nio.ByteBuffer;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header;
import org.robolectric.res.android.ResourceTypes.ResTable_header;
import org.robolectric.res.android.ResourceTypes.ResTable_lib_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_lib_header;
import org.robolectric.res.android.ResourceTypes.ResTable_package;
import org.robolectric.res.android.ResourceTypes.ResTable_type;
import org.robolectric.res.android.ResourceTypes.WithOffset;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ChunkIterator.cpp and
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/Chunk.h

// Helpful wrapper around a ResChunk_header that provides getter methods
// that handle endianness conversions and provide access to the data portion
// of the chunk.
class Chunk {

  // public:
  Chunk(ResChunk_header chunk) {
    this.device_chunk_ = chunk;
  }

  // Returns the type of the chunk. Caller need not worry about endianness.
  int type() {
    return dtohs(device_chunk_.type);
  }

  // Returns the size of the entire chunk. This can be useful for skipping
  // over the entire chunk. Caller need not worry about endianness.
  int size() {
    return dtohl(device_chunk_.size);
  }

  // Returns the size of the header. Caller need not worry about endianness.
  int header_size() {
    return dtohs(device_chunk_.headerSize);
  }

  // template <typename T, int MinSize = sizeof(T)>
  // T* header() {
  //   if (header_size() >= MinSize) {
  //     return reinterpret_cast<T*>(device_chunk_);
  //   }
  //   return nullptr;
  // }

  ByteBuffer myBuf() {
    return device_chunk_.myBuf();
  }

  int myOffset() {
    return device_chunk_.myOffset();
  }

  public WithOffset data_ptr() {
    return new WithOffset(device_chunk_.myBuf(), device_chunk_.myOffset() + header_size());
  }

  int data_size() {
    return size() - header_size();
  }

  // private:
  private ResChunk_header device_chunk_;

  public ResTable_header asResTable_header() {
    if (header_size() >= ResTable_header.SIZEOF) {
      return new ResTable_header(device_chunk_.myBuf(), device_chunk_.myOffset());
    } else {
      return null;
    }
  }

  public ResStringPool_header asResStringPool_header() {
    if (header_size() >= ResStringPool_header.SIZEOF) {
      return new ResStringPool_header(device_chunk_.myBuf(), device_chunk_.myOffset());
    } else {
      return null;
    }
  }

  public ResTable_package asResTable_package(int size) {
    if (header_size() >= size) {
      return new ResTable_package(device_chunk_.myBuf(), device_chunk_.myOffset());
    } else {
      return null;
    }
  }

  public ResTable_type asResTable_type(int size) {
    if (header_size() >= size) {
      return new ResTable_type(device_chunk_.myBuf(), device_chunk_.myOffset());
    } else {
      return null;
    }
  }

  public ResTable_lib_header asResTable_lib_header() {
    if (header_size() >= ResTable_lib_header.SIZEOF) {
      return new ResTable_lib_header(device_chunk_.myBuf(), device_chunk_.myOffset());
    } else {
      return null;
    }
  }

  public ResTable_lib_entry asResTable_lib_entry() {
    if (header_size() >= ResTable_lib_entry.SIZEOF) {
      return new ResTable_lib_entry(device_chunk_.myBuf(), device_chunk_.myOffset());
    } else {
      return null;
    }
  }

  static class Iterator {
    private ResChunk_header next_chunk_;
    private int len_;
    private String last_error_;
    private boolean last_error_was_fatal_ = true;

    public Iterator(WithOffset buf, int itemSize) {
      this.next_chunk_ = new ResChunk_header(buf.myBuf(), buf.myOffset());
      this.len_ = itemSize;
    }

    boolean HasNext() { return !HadError() && len_ != 0; };
    // Returns whether there was an error and processing should stop
    boolean HadError() { return last_error_ != null; }
    String GetLastError() { return last_error_; }
    // Returns whether there was an error and processing should stop. For legacy purposes,
    // some errors are considered "non fatal". Fatal errors stop processing new chunks and
    // throw away any chunks already processed. Non fatal errors also stop processing new
    // chunks, but, will retain and use any valid chunks already processed.
    boolean HadFatalError() { return HadError() && last_error_was_fatal_; }

    Chunk Next() {
      assert (len_ != 0) : "called Next() after last chunk";

      ResChunk_header this_chunk = next_chunk_;

      // We've already checked the values of this_chunk, so safely increment.
      // next_chunk_ = reinterpret_cast<const ResChunk_header*>(
      //     reinterpret_cast<const uint8_t*>(this_chunk) + dtohl(this_chunk->size));
      int remaining = len_ - dtohl(this_chunk.size);
      if (remaining <= 0) {
        next_chunk_ = null;
      } else {
        next_chunk_ = new ResChunk_header(
            this_chunk.myBuf(), this_chunk.myOffset() + dtohl(this_chunk.size));
      }
      len_ -= dtohl(this_chunk.size);

      if (len_ != 0) {
        // Prepare the next chunk.
        if (VerifyNextChunkNonFatal()) {
          VerifyNextChunk();
        }
      }
      return new Chunk(this_chunk);
    }

    // TODO(b/111401637) remove this and have full resource file verification
    // Returns false if there was an error. For legacy purposes.
    boolean VerifyNextChunkNonFatal() {
      if (len_ < ResChunk_header.SIZEOF) {
        last_error_ = "not enough space for header";
        last_error_was_fatal_ = false;
        return false;
      }
      int size = dtohl(next_chunk_.size);
      if (size > len_) {
        last_error_ = "chunk size is bigger than given data";
        last_error_was_fatal_ = false;
        return false;
      }
      return true;
    }

    // Returns false if there was an error.
    boolean VerifyNextChunk() {
      // uintptr_t header_start = reinterpret_cast<uintptr_t>(next_chunk_);
      int header_start = next_chunk_.myOffset();

      // This data must be 4-byte aligned, since we directly
      // access 32-bit words, which must be aligned on
      // certain architectures.
      if (isTruthy(header_start & 0x03)) {
        last_error_ = "header not aligned on 4-byte boundary";
        return false;
      }

      if (len_ < ResChunk_header.SIZEOF) {
        last_error_ = "not enough space for header";
        return false;
      }

      int header_size = dtohs(next_chunk_.headerSize);
      int size = dtohl(next_chunk_.size);
      if (header_size < ResChunk_header.SIZEOF) {
        last_error_ = "header size too small";
        return false;
      }

      if (header_size > size) {
        last_error_ = "header size is larger than entire chunk";
        return false;
      }

      if (size > len_) {
        last_error_ = "chunk size is bigger than given data";
        return false;
      }

      if (isTruthy((size | header_size) & 0x03)) {
        last_error_ = "header sizes are not aligned on 4-byte boundary";
        return false;
      }
      return true;
    }
  }
}
