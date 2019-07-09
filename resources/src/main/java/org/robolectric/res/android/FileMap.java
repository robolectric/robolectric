package org.robolectric.res.android;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.res.android.Asset.toIntExact;
import static org.robolectric.res.android.Util.ALOGV;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.LittleEndianDataInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileMap {

  private ZipFile zipFile;
  private ZipEntry zipEntry;
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

    int     prot, flags, adjust;
    long adjOffset;
    int  adjLength;

    int ptr;

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

  // TODO: use the Central Directory to get file offsets instead of guessing.
  // https://github.com/robolectric/robolectric/issues/5123
  static ImmutableMap<String, Long> guessDataOffsets(ZipFile zipFile) {
    ImmutableMap.Builder<String, Long> result = ImmutableMap.builder();
    try (RandomAccessFile raf = new RandomAccessFile(zipFile.getName(), "r")) {
      int numEntries = zipFile.size();
      long offset = 0;
      for (int i = 0; i < numEntries; i++) {
        offset = findLocalFileHeader(raf, offset);

        byte[] localFileHeaderBytes = new byte[30];
        raf.seek(offset);
        raf.readFully(localFileHeaderBytes);
        LittleEndianDataInputStream localFileHeader =
            new LittleEndianDataInputStream(new ByteArrayInputStream(localFileHeaderBytes));

        /* signature= */ localFileHeader.readInt();
        /* version= */ localFileHeader.readShort();
        short flags = localFileHeader.readShort();
        /* compressionMethod= */ localFileHeader.readShort();
        /* modificationTimeAndDate= */ localFileHeader.readInt();
        /* crc32= */ localFileHeader.readInt();
        int fileSize = localFileHeader.readInt();
        /* uncompressedFileSize */ localFileHeader.readInt();
        short filenameSize = localFileHeader.readShort();
        short extra = localFileHeader.readShort();

        byte[] filenameBytes = new byte[filenameSize];
        raf.readFully(filenameBytes);
        String filename = new String(filenameBytes, (flags & (1 << 11)) != 0 ? UTF_8 : ISO_8859_1);

        offset += 30 + filenameSize + extra;
        result.put(filename, offset);

        offset += fileSize;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result.build();
  }

  /**
   * Returns the first offset greater than or equal to {@code offset} where the magic number
   * 0x504b0304 may be found.
   *
   * <p>This is needed if there are gaps between ZIP entries, or if <a
   * href="https://en.wikipedia.org/w/index.php?title=Zip_(file_format)&oldid=897956527#Data_descriptor">data
   * descriptors</a> are used.
   *
   * <p>Note it's worth optimizing the reads here. The naive implementation (below) can make some
   * tests 6x slower. <code>
   *   while (true) {
   *     raf.seek(offset);
   *     if (raf.readInt() == 0x504b0304) return offset;
   *     offset++;
   *   }
   * </code>
   */
  private static long findLocalFileHeader(RandomAccessFile raf, long offset) throws IOException {
    byte[] buf = new byte[128];
    while (true) {
      raf.seek(offset);
      int bytesRead = readAtLeast(raf, buf, 4);
      for (int i = 0; i < bytesRead - 3; i++) {
        if (buf[i + 0] == 0x50 && buf[i + 1] == 0x4b && buf[i + 2] == 0x03 && buf[i + 3] == 0x04) {
          return offset + i;
        }
      }
      offset += bytesRead - 3;
    }
  }

  private static int readAtLeast(RandomAccessFile raf, byte[] buf, int n) throws IOException {
    int totalRead = 0;
    do {
      int count = raf.read(buf, totalRead, buf.length - totalRead);
      if (count < 0) {
        throw new EOFException();
      }
      totalRead += count;
    } while (totalRead < n);
    return totalRead;
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
