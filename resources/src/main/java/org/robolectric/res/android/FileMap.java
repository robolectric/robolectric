package org.robolectric.res.android;

import static org.robolectric.res.android.Asset.toIntExact;
import static org.robolectric.res.android.Util.ALOGV;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
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


  public boolean createFromZip(String origFileName, ZipFile zipFile, ZipEntry entry, int length,
      boolean readOnly) {
    isFromZip = true;
    this.zipFile = zipFile;
    this.zipEntry = entry;

    int     prot, flags, adjust;
    long adjOffset;
    int  adjLength;

    int ptr;
    long offset = guessOffsetFor(zipFile, entry);

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

  long guessOffsetFor(ZipFile zipFile, ZipEntry zipEntry) {
    Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
    long offset = 0;
    while (zipEntries.hasMoreElements())
    {
      ZipEntry entry = zipEntries.nextElement();
      long fileSize = 0;
      long extra = entry.getExtra() == null ? 0 : entry.getExtra().length;
      offset += 30 + entry.getName().length() + extra;

      if (entry.getName().equals(zipEntry.getName())) {
        return offset;
      }

      if(!entry.isDirectory())
      {
        fileSize = entry.getCompressedSize();

        // Do stuff here with fileSize & offset
      }
      offset += fileSize;
    }
    throw new IllegalStateException("'" + zipEntry.getName() + "' not found");
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
