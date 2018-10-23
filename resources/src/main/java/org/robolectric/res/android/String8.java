package org.robolectric.res.android;

import java.io.File;

// transliterated from https://android.googlesource.com/platform/system/core/+/android-9.0.0_r12/libutils/String8.cpp
// and https://android.googlesource.com/platform/system/core/+/android-9.0.0_r12/include/utils/String8.h
public class String8 {

  private StringBuilder mString;

  public String8() {
    this("");
  }

  public String8(String value) {
    mString = new StringBuilder(value);
  }

  public String8(String8 path) {
    this(path.string());
  }

  public String8(String value, int len) {
    this(value.substring(0, len));
  }

  int length() {
    return mString.length();
  }
//String8 String8::format(const char* fmt, ...)
//{
//    va_list args;
//    va_start(args, fmt);
//    String8 result(formatV(fmt, args));
//    va_end(args);
//    return result;
//}
//String8 String8::formatV(const char* fmt, va_list args)
//{
//    String8 result;
//    result.appendFormatV(fmt, args);
//    return result;
//}
//void String8::clear() {
//    SharedBuffer::bufferFromData(mString)->release();
//    mString = getEmptyString();
//}
//void String8::setTo(const String8& other)
//{
//    SharedBuffer::bufferFromData(other.mString)->acquire();
//    SharedBuffer::bufferFromData(mString)->release();
//    mString = other.mString;
//}
//status_t String8::setTo(const char* other)
//{
//    const char *newString = allocFromUTF8(other, strlen(other));
//    SharedBuffer::bufferFromData(mString)->release();
//    mString = newString;
//    if (mString) return NO_ERROR;
//    mString = getEmptyString();
//    return NO_MEMORY;
//}
//status_t String8::setTo(const char* other, size_t len)
//{
//    const char *newString = allocFromUTF8(other, len);
//    SharedBuffer::bufferFromData(mString)->release();
//    mString = newString;
//    if (mString) return NO_ERROR;
//    mString = getEmptyString();
//    return NO_MEMORY;
//}
//status_t String8::setTo(const char16_t* other, size_t len)
//{
//    const char *newString = allocFromUTF16(other, len);
//    SharedBuffer::bufferFromData(mString)->release();
//    mString = newString;
//    if (mString) return NO_ERROR;
//    mString = getEmptyString();
//    return NO_MEMORY;
//}
//status_t String8::setTo(const char32_t* other, size_t len)
//{
//    const char *newString = allocFromUTF32(other, len);
//    SharedBuffer::bufferFromData(mString)->release();
//    mString = newString;
//    if (mString) return NO_ERROR;
//    mString = getEmptyString();
//    return NO_MEMORY;
//}
//status_t String8::append(const String8& other)
//{
//    const size_t otherLen = other.bytes();
//    if (bytes() == 0) {
//        setTo(other);
//        return NO_ERROR;
//    } else if (otherLen == 0) {
//        return NO_ERROR;
//    }
//    return real_append(other.string(), otherLen);
//}
public String8 append(final String other) {
  mString.append(other);
    return this;
}
//status_t String8::append(const char* other, size_t otherLen)
//{
//    if (bytes() == 0) {
//        return setTo(other, otherLen);
//    } else if (otherLen == 0) {
//        return NO_ERROR;
//    }
//    return real_append(other, otherLen);
//}
//status_t String8::appendFormat(const char* fmt, ...)
//{
//    va_list args;
//    va_start(args, fmt);
//    status_t result = appendFormatV(fmt, args);
//    va_end(args);
//    return result;
//}
//status_t String8::appendFormatV(const char* fmt, va_list args)
//{
//    int n, result = NO_ERROR;
//    va_list tmp_args;
//    /* args is undefined after vsnprintf.
//     * So we need a copy here to avoid the
//     * second vsnprintf access undefined args.
//     */
//    va_copy(tmp_args, args);
//    n = vsnprintf(NULL, 0, fmt, tmp_args);
//    va_end(tmp_args);
//    if (n != 0) {
//        size_t oldLength = length();
//        char* buf = lockBuffer(oldLength + n);
//        if (buf) {
//            vsnprintf(buf + oldLength, n + 1, fmt, args);
//        } else {
//            result = NO_MEMORY;
//        }
//    }
//    return result;
//}
//status_t String8::real_append(const char* other, size_t otherLen)
//{
//    const size_t myLen = bytes();
//
//    SharedBuffer* buf = SharedBuffer::bufferFromData(mString)
//        ->editResize(myLen+otherLen+1);
//    if (buf) {
//        char* str = (char*)buf->data();
//        mString = str;
//        str += myLen;
//        memcpy(str, other, otherLen);
//        str[otherLen] = '\0';
//        return NO_ERROR;
//    }
//    return NO_MEMORY;
//}
//char* String8::lockBuffer(size_t size)
//{
//    SharedBuffer* buf = SharedBuffer::bufferFromData(mString)
//        ->editResize(size+1);
//    if (buf) {
//        char* str = (char*)buf->data();
//        mString = str;
//        return str;
//    }
//    return NULL;
//}
//void String8::unlockBuffer()
//{
//    unlockBuffer(strlen(mString));
//}
//status_t String8::unlockBuffer(size_t size)
//{
//    if (size != this->size()) {
//        SharedBuffer* buf = SharedBuffer::bufferFromData(mString)
//            ->editResize(size+1);
//        if (! buf) {
//            return NO_MEMORY;
//        }
//        char* str = (char*)buf->data();
//        str[size] = 0;
//        mString = str;
//    }
//    return NO_ERROR;
//}
//ssize_t String8::find(const char* other, size_t start) const
//{
//    size_t len = size();
//    if (start >= len) {
//        return -1;
//    }
//    const char* s = mString+start;
//    const char* p = strstr(s, other);
//    return p ? p-mString : -1;
//}
//bool String8::removeAll(const char* other) {
//    ssize_t index = find(other);
//    if (index < 0) return false;
//    char* buf = lockBuffer(size());
//    if (!buf) return false; // out of memory
//    size_t skip = strlen(other);
//    size_t len = size();
//    size_t tail = index;
//    while (size_t(index) < len) {
//        ssize_t next = find(other, index + skip);
//        if (next < 0) {
//            next = len;
//        }
//        memmove(buf + tail, buf + index + skip, next - index - skip);
//        tail += next - index - skip;
//        index = next;
//    }
//    unlockBuffer(tail);
//    return true;
//}
//void String8::toLower()
//{
//    toLower(0, size());
//}
//void String8::toLower(size_t start, size_t length)
//{
//    const size_t len = size();
//    if (start >= len) {
//        return;
//    }
//    if (start+length > len) {
//        length = len-start;
//    }
//    char* buf = lockBuffer(len);
//    buf += start;
//    while (length > 0) {
//        *buf = tolower(*buf);
//        buf++;
//        length--;
//    }
//    unlockBuffer(len);
//}
//void String8::toUpper()
//{
//    toUpper(0, size());
//}
//void String8::toUpper(size_t start, size_t length)
//{
//    const size_t len = size();
//    if (start >= len) {
//        return;
//    }
//    if (start+length > len) {
//        length = len-start;
//    }
//    char* buf = lockBuffer(len);
//    buf += start;
//    while (length > 0) {
//        *buf = toupper(*buf);
//        buf++;
//        length--;
//    }
//    unlockBuffer(len);
//}
//size_t String8::getUtf32Length() const
//{
//    return utf8_to_utf32_length(mString, length());
//}
//int32_t String8::getUtf32At(size_t index, size_t *next_index) const
//{
//    return utf32_from_utf8_at(mString, length(), index, next_index);
//}
//void String8::getUtf32(char32_t* dst) const
//{
//    utf8_to_utf32(mString, length(), dst);
//}
//// ---------------------------------------------------------------------------
//// Path functions
//void String8::setPathName(const char* name)
//{
//    setPathName(name, strlen(name));
//}
//void String8::setPathName(const char* name, size_t len)
//{
//    char* buf = lockBuffer(len);
//    memcpy(buf, name, len);
//    // remove trailing path separator, if present
//    if (len > 0 && buf[len-1] == OS_PATH_SEPARATOR)
//        len--;
//    buf[len] = '\0';
//    unlockBuffer(len);
//}
String8 getPathLeaf() {
  final int cp;
  final String buf = mString.toString();
  cp = buf.lastIndexOf(File.separatorChar);
  if (cp == -1) {
    return new String8(this);
  } else {
    return new String8(buf.substring(cp + 1));
  }
}
//String8 String8::getPathDir(void) const
//{
//    const char* cp;
//    const char*const str = mString;
//    cp = strrchr(str, OS_PATH_SEPARATOR);
//    if (cp == NULL)
//        return String8("");
//    else
//        return String8(str, cp - str);
//}
//String8 String8::walkPath(String8* outRemains) const
//{
//    const char* cp;
//    const char*const str = mString;
//    const char* buf = str;
//    cp = strchr(buf, OS_PATH_SEPARATOR);
//    if (cp == buf) {
//        // don't include a leading '/'.
//        buf = buf+1;
//        cp = strchr(buf, OS_PATH_SEPARATOR);
//    }
//    if (cp == NULL) {
//        String8 res = buf != str ? String8(buf) : *this;
//        if (outRemains) *outRemains = String8("");
//        return res;
//    }
//    String8 res(buf, cp-buf);
//    if (outRemains) *outRemains = String8(cp+1);
//    return res;
//}

/*
 * Helper function for finding the start of an extension in a pathname.
 *
 * Returns a index inside mString, or -1 if no extension was found.
 */
private int find_extension()
{
    int lastSlashIndex;

    final StringBuilder str = mString;
    // only look at the filename
    lastSlashIndex = str.lastIndexOf(File.pathSeparator);
    if (lastSlashIndex == -1) {
      lastSlashIndex = 0;
    } else {
      lastSlashIndex++;
    }
    // find the last dot
    return str.lastIndexOf(".", lastSlashIndex);
}

public String getPathExtension()
{
    int extIndex;
    extIndex = find_extension();
    if (extIndex != -1) {
      return mString.substring(extIndex);
    }
    else {
      return "";
    }
}

  String8 getBasePath() {
    int extIndex;
    extIndex = find_extension();
    if (extIndex == -1) {
      return new String8(this);
    } else {
      return new String8(mString.substring(extIndex));
    }
  }

  public String8 appendPath(String name) {
    if (name.length() == 0) {
      // nothing to do
      return this;
    }
    if (name.charAt(0) != File.separatorChar) {
      mString.append(File.separatorChar);
    }
    mString.append(name);
    return this;
}

//String8& String8::convertToResPath()
//{
//#if OS_PATH_SEPARATOR != RES_PATH_SEPARATOR
//    size_t len = length();
//    if (len > 0) {
//        char * buf = lockBuffer(len);
//        for (char * end = buf + len; buf < end; ++buf) {
//            if (*buf == OS_PATH_SEPARATOR)
//                *buf = RES_PATH_SEPARATOR;
//        }
//        unlockBuffer(len);
//    }
//#endif
//    return *this;
//}
//}; // namespace android

  public final String string() {
    return mString.toString();
  }

  @Override
  public String toString() {
    return mString.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    String8 string8 = (String8) o;

    return mString != null ? mString.toString().equals(string8.mString.toString()) : string8.mString == null;
  }

  @Override
  public int hashCode() {
    return mString != null ? mString.hashCode() : 0;
  }

}


