package org.robolectric.res.android;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.res.android.Asset.toIntExact;
import static org.robolectric.res.android.Util.ALOGV;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class FileMap {

  /** ZIP archive central directory end header signature. */
  private static final int ENDSIG = 0x6054b50;

  private static final int EOCD_SIZE = 22;

  private static final int ZIP64_EOCD_SIZE = 56;

  private static final int ZIP64_EOCD_LOCATOR_SIZE = 20;

  /** ZIP64 archive central directory end header signature. */
  private static final int ENDSIG64 = 0x6064b50;

  private static final int MAX_COMMENT_SIZE = 64 * 1024; // 64k

  /** the maximum size of the end of central directory sections in bytes */
  private static final int MAXIMUM_ZIP_EOCD_SIZE =
      MAX_COMMENT_SIZE + EOCD_SIZE + ZIP64_EOCD_SIZE + ZIP64_EOCD_LOCATOR_SIZE;

  private ZipFile zipFile;
  private ZipEntry zipEntry;

  @SuppressWarnings("unused")
  private boolean readOnly;

  private int fd;
  private boolean isFromZip;

  // Create a new mapping on an open file.
//
// Closing the file descriptor does not unmap the pages, so we don't
// claim ownership of the fd.
//
// Returns "false" on failure.
  boolean create(String origFileName, int fd, long offset, int length,
      boolean readOnly)
  {
    this.mFileName = origFileName;
    this.fd = fd;
    this.mDataOffset = offset;
    this.readOnly = readOnly;
    return true;
  }

// #if defined(__MINGW32__)
//     int     adjust;
//     off64_t adjOffset;
//     size_t  adjLength;
//
//     if (mPageSize == -1) {
//       SYSTEM_INFO  si;
//
//       GetSystemInfo( &si );
//       mPageSize = si.dwAllocationGranularity;
//     }
//
//     DWORD  protect = readOnly ? PAGE_READONLY : PAGE_READWRITE;
//
//     mFileHandle  = (HANDLE) _get_osfhandle(fd);
//     mFileMapping = CreateFileMapping( mFileHandle, NULL, protect, 0, 0, NULL);
//     if (mFileMapping == NULL) {
//       ALOGE("CreateFileMapping(%s, %" PRIx32 ") failed with error %" PRId32 "\n",
//           mFileHandle, protect, GetLastError() );
//       return false;
//     }
//
//     adjust    = offset % mPageSize;
//     adjOffset = offset - adjust;
//     adjLength = length + adjust;
//
//     mBasePtr = MapViewOfFile( mFileMapping,
//         readOnly ? FILE_MAP_READ : FILE_MAP_ALL_ACCESS,
//         0,
//         (DWORD)(adjOffset),
//         adjLength );
//     if (mBasePtr == NULL) {
//       ALOGE("MapViewOfFile(%" PRId64 ", 0x%x) failed with error %" PRId32 "\n",
//           adjOffset, adjLength, GetLastError() );
//       CloseHandle(mFileMapping);
//       mFileMapping = INVALID_HANDLE_VALUE;
//       return false;
//     }
// #else // !defined(__MINGW32__)
//     int     prot, flags, adjust;
//     off64_t adjOffset;
//     size_t  adjLength;
//
//     void* ptr;
//
//     assert(fd >= 0);
//     assert(offset >= 0);
//     assert(length > 0);
//
//     // init on first use
//     if (mPageSize == -1) {
//       mPageSize = sysconf(_SC_PAGESIZE);
//       if (mPageSize == -1) {
//         ALOGE("could not get _SC_PAGESIZE\n");
//         return false;
//       }
//     }
//
//     adjust = offset % mPageSize;
//     adjOffset = offset - adjust;
//     adjLength = length + adjust;
//
//     flags = MAP_SHARED;
//     prot = PROT_READ;
//     if (!readOnly)
//       prot |= PROT_WRITE;
//
//     ptr = mmap(NULL, adjLength, prot, flags, fd, adjOffset);
//     if (ptr == MAP_FAILED) {
//       ALOGE("mmap(%lld,0x%x) failed: %s\n",
//           (long long)adjOffset, adjLength, strerror(errno));
//       return false;
//     }
//     mBasePtr = ptr;
// #endif // !defined(__MINGW32__)
//
//       mFileName = origFileName != NULL ? strdup(origFileName) : NULL;
//     mBaseLength = adjLength;
//     mDataOffset = offset;
//     mDataPtr = (char*) mBasePtr + adjust;
//     mDataLength = length;
//
//     assert(mBasePtr != NULL);
//
//     ALOGV("MAP: base %s/0x%x data %s/0x%x\n",
//         mBasePtr, mBaseLength, mDataPtr, mDataLength);
//
//     return true;
//   }

  boolean createFromZip(
      String origFileName,
      ZipFile zipFile,
      ZipEntry entry,
      long offset,
      int length,
      boolean readOnly) {
    isFromZip = true;
    this.zipFile = zipFile;
    this.zipEntry = entry;

    assert(fd >= 0);
    assert(offset >= 0);
    // assert(length > 0);

    // init on first use
//    if (mPageSize == -1) {
//      mPageSize = sysconf(_SC_PAGESIZE);
//      if (mPageSize == -1) {
//        ALOGE("could not get _SC_PAGESIZE\n");
//        return false;
//      }
//    }

    // adjust = Math.toIntExact(offset % mPageSize);
    // adjOffset = offset - adjust;
    // adjLength = length + adjust;

    //flags = MAP_SHARED;
    //prot = PROT_READ;
    //if (!readOnly)
    //  prot |= PROT_WRITE;

    // ptr = mmap(null, adjLength, prot, flags, fd, adjOffset);
    // if (ptr == MAP_FAILED) {
    //   ALOGE("mmap(%lld,0x%x) failed: %s\n",
    //       (long long)adjOffset, adjLength, strerror(errno));
    //   return false;
    // }
    // mBasePtr = ptr;

    mFileName = origFileName != null ? origFileName : null;
    //mBaseLength = adjLength;
    mDataOffset = offset;
    //mDataPtr = mBasePtr + adjust;
    mDataLength = toIntExact(entry.getSize());

    //assert(mBasePtr != 0);

    ALOGV("MAP: base %s/0x%x data %s/0x%x\n",
        mBasePtr, mBaseLength, mDataPtr, mDataLength);

    return true;
  }

  static ImmutableMap<String, Long> guessDataOffsets(File zipFile, int length) {
    ImmutableMap.Builder<String, Long> result = ImmutableMap.builder();

    // Parse the zip file entry offsets from the central directory section.
    // See https://en.wikipedia.org/wiki/Zip_(file_format)

    try (RandomAccessFile randomAccessFile = new RandomAccessFile(zipFile, "r")) {

      // First read the 'end of central directory record' in order to find the start of the central
      // directory
      int endOfCdSize = Math.min(MAXIMUM_ZIP_EOCD_SIZE, length);
      int endofCdOffset = length - endOfCdSize;
      randomAccessFile.seek(endofCdOffset);
      byte[] buffer = new byte[endOfCdSize];
      randomAccessFile.readFully(buffer);

      int centralDirOffset = findCentralDir(buffer);
      if (centralDirOffset == -1) {
        // If the zip file contains > 2^16 entries, a Zip64 EOCD is written, and the central
        // dir offset in the regular EOCD may be -1.
        centralDirOffset = findCentralDir64(buffer);
      }
      int offset = centralDirOffset - endofCdOffset;
      if (offset < 0) {
        // read the entire central directory record into memory
        // for the framework jars this max of 5MB for Q
        // TODO: consider using a smaller buffer size and re-reading as necessary
        offset = 0;
        randomAccessFile.seek(centralDirOffset);
        final int cdSize = length - centralDirOffset;
        buffer = new byte[cdSize];
        randomAccessFile.readFully(buffer);
      } else {
        // the central directory is already in the buffer, no need to reread
      }

      // now read the entries
      while (true) {
        // Instead of trusting numRecords, read until we find the
        // end-of-central-directory signature.  numRecords may wrap
        // around with >64K entries.
        int sig = readInt(buffer, offset);
        if (sig == ENDSIG || sig == ENDSIG64) {
          break;
        }

        int bitFlag = readShort(buffer, offset + 8);
        int fileNameLength = readShort(buffer, offset + 28);
        int extraLength = readShort(buffer, offset + 30);
        int fieldCommentLength = readShort(buffer, offset + 32);
        int relativeOffsetOfLocalFileHeader = readInt(buffer, offset + 42);

        byte[] nameBytes = copyBytes(buffer, offset + 46, fileNameLength);
        Charset encoding = getEncoding(bitFlag);
        String fileName = new String(nameBytes, encoding);
        byte[] localHeaderBuffer = new byte[30];
        randomAccessFile.seek(relativeOffsetOfLocalFileHeader);
        randomAccessFile.readFully(localHeaderBuffer);
        // There are two extra field lengths stored in the zip - one in the central directory,
        // one in the local header. And we should use one in local header to calculate the
        // correct file content offset, because they are different some times.
        int localHeaderExtraLength = readShort(localHeaderBuffer, 28);
        int fileOffset =
            relativeOffsetOfLocalFileHeader + 30 + fileNameLength + localHeaderExtraLength;
        result.put(fileName, (long) fileOffset);
        offset += 46 + fileNameLength + extraLength + fieldCommentLength;
      }

      return result.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static byte[] copyBytes(byte[] buffer, int offset, int length) {
    byte[] result = new byte[length];
    System.arraycopy(buffer, offset, result, 0, length);
    return result;
  }

  private static Charset getEncoding(int bitFlags) {
    // UTF-8 now supported in name and comments: check general bit flag, bit
    // 11, to determine if UTF-8 is being used or ISO-8859-1 is being used.
    return (0 != ((bitFlags >>> 11) & 1)) ? UTF_8 : ISO_8859_1;
  }

  private static int findCentralDir(byte[] buffer) throws IOException {
    // find start of central directory by scanning backwards
    int scanOffset = buffer.length - EOCD_SIZE;

    while (true) {
      int val = readInt(buffer, scanOffset);
      if (val == ENDSIG) {
        break;
      }

      // Ok, keep backing up looking for the ZIP end central directory
      // signature.
      --scanOffset;
      if (scanOffset < 0) {
        throw new ZipException("ZIP directory not found, not a ZIP archive.");
      }
    }
    // scanOffset is now start of end of central directory record
    // the 'offset to central dir' data is at position 16 in the record
    int offsetToCentralDir = readInt(buffer, scanOffset + 16);
    return offsetToCentralDir;
  }

  private static int findCentralDir64(byte[] buffer) throws IOException {
    // find start of central directory by scanning backwards
    int scanOffset = buffer.length - EOCD_SIZE - ZIP64_EOCD_LOCATOR_SIZE - ZIP64_EOCD_SIZE;

    while (true) {
      int val = readInt(buffer, scanOffset);
      if (val == ENDSIG64) {
        break;
      }

      // Ok, keep backing up looking for the ZIP end central directory
      // signature.
      --scanOffset;
      if (scanOffset < 0) {
        throw new ZipException("ZIP directory not found, not a ZIP archive.");
      }
    }
    // scanOffset is now start of end of central directory record
    // the 'offset to central dir' data is at position 16 in the record
    long offsetToCentralDir = readLong(buffer, scanOffset + 48);
    return (int) offsetToCentralDir;
  }

  /** Read a 32-bit integer from a bytebuffer in little-endian order. */
  private static int readInt(byte[] buffer, int offset) {
    return Ints.fromBytes(
        buffer[offset + 3], buffer[offset + 2], buffer[offset + 1], buffer[offset]);
  }

  /** Read a 64-bit integer from a bytebuffer in little-endian order. */
  private static long readLong(byte[] buffer, int offset) {
    return Longs.fromBytes(
        buffer[offset + 7],
        buffer[offset + 6],
        buffer[offset + 5],
        buffer[offset + 4],
        buffer[offset + 3],
        buffer[offset + 2],
        buffer[offset + 1],
        buffer[offset]);
  }

  /** Read a 16-bit short from a bytebuffer in little-endian order. */
  private static short readShort(byte[] buffer, int offset) {
    return Shorts.fromBytes(buffer[offset + 1], buffer[offset]);
  }

  /*
   * This represents a memory-mapped file.  It might be the entire file or
   * only part of it.  This requires a little bookkeeping because the mapping
   * needs to be aligned on page boundaries, and in some cases we'd like to
   * have multiple references to the mapped area without creating additional
   * maps.
   *
   * This always uses MAP_SHARED.
   *
   * TODO: we should be able to create a new FileMap that is a subset of
   * an existing FileMap and shares the underlying mapped pages.  Requires
   * completing the refcounting stuff and possibly introducing the notion
   * of a FileMap hierarchy.
   */
  // class FileMap {
  //   public:
  //   FileMap(void);
  //
  //   FileMap(FileMap&& f);
  //   FileMap& operator=(FileMap&& f);

  /*
   * Create a new mapping on an open file.
   *
   * Closing the file descriptor does not unmap the pages, so we don't
   * claim ownership of the fd.
   *
   * Returns "false" on failure.
   */
  // boolean create(String origFileName, int fd,
  //     long offset, int length, boolean readOnly) {
  // }

    // ~FileMap(void);

    /*
     * Return the name of the file this map came from, if known.
     */
    String getFileName() { return mFileName; }

    /*
     * Get a pointer to the piece of the file we requested.
     */
  synchronized byte[] getDataPtr() {
    if (mDataPtr == null) {
      mDataPtr = new byte[mDataLength];

      InputStream is;
      try {
        if (isFromZip) {
          is = zipFile.getInputStream(zipEntry);
        } else {
          is = new FileInputStream(getFileName());
        }
        try {
          readFully(is, mDataPtr);
        } finally {
          is.close();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return mDataPtr;
  }

  public static void readFully(InputStream is, byte[] bytes) throws IOException {
    int size = bytes.length;
    int remaining = size;
    while (remaining > 0) {
      int location = size - remaining;
      int bytesRead = is.read(bytes, location, remaining);
      if (bytesRead == -1) {
        break;
      }
      remaining -= bytesRead;
    }

    if (remaining > 0) {
      throw new RuntimeException("failed to read " + size + " (" + remaining + " bytes unread)");
    }
  }

  /*
   * Get the length we requested.
   */
  int getDataLength() { return mDataLength; }

  /*
   * Get the data offset used to create this map.
   */
  long getDataOffset() { return mDataOffset; }

  public ZipEntry getZipEntry() {
    return zipEntry;
  }

  //   /*
//    * This maps directly to madvise() values, but allows us to avoid
//    * including <sys/mman.h> everywhere.
//    */
//   enum MapAdvice {
//     NORMAL, RANDOM, SEQUENTIAL, WILLNEED, DONTNEED
//   };
//
//   /*
//    * Apply an madvise() call to the entire file.
//    *
//    * Returns 0 on success, -1 on failure.
//    */
//   int advise(MapAdvice advice);
//
//   protected:
//
//   private:
//   // these are not implemented
//   FileMap(const FileMap& src);
//     const FileMap& operator=(const FileMap& src);
//
  String       mFileName;      // original file name, if known
  int       mBasePtr;       // base of mmap area; page aligned
  int      mBaseLength;    // length, measured from "mBasePtr"
  long     mDataOffset;    // offset used when map was created
  byte[]       mDataPtr;       // start of requested data, offset from base
  int      mDataLength;    // length, measured from "mDataPtr"
  static long mPageSize;

  @Override
  public String toString() {
    if (isFromZip) {
      return "FileMap{" +
          "zipFile=" + zipFile.getName() +
          ", zipEntry=" + zipEntry +
          '}';
    } else {
      return "FileMap{" +
          "mFileName='" + mFileName + '\'' +
          '}';
    }
  }
}
