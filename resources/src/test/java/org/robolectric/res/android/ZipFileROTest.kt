package org.robolectric.res.android

import com.google.common.io.ByteStreams
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipOutputStream

/** Unit test for [ZipFileRO].  */
@RunWith(JUnit4::class)
class ZipFileROTest {

    /** Verify that ZIP entries can be located when there are gaps between them.  */ //
    // The test data (zip_with_gap.zip) was crafted by creating a regular ZIP file with two
    // uncompressed entries, adding 32 spaces between them, and adjusting offsets accordingly
    // (in particular, the contents of the Central Directory and EOCD).
    //
    // 00000000: 504b 0304 0a00 0000 0000 0000 2100 a865  PK..........!..e
    // 00000010: 327e 0400 0000 0400 0000 0200 0000 6630  2~............f0
    // 00000020: 666f 6f0a 2020 2020 2020 2020 2020 2020  foo.
    // 00000030: 2020 2020 2020 2020 2020 2020 2020 2020
    // 00000040: 2020 2020 504b 0304 0a00 0000 0000 0000      PK..........
    // 00000050: 2100 e9b3 a204 0400 0000 0400 0000 0200  !...............
    // 00000060: 0000 6631 6261 720a 504b 0102 1e03 0a00  ..f1bar.PK......
    // 00000070: 0000 0000 0000 2100 a865 327e 0400 0000  ......!..e2~....
    // 00000080: 0400 0000 0200 0000 0000 0000 0000 0000  ................
    // 00000090: a081 0000 0000 6630 504b 0102 1e03 0a00  ......f0PK......
    // 000000a0: 0000 0000 0000 2100 e9b3 a204 0400 0000  ......!.........
    // 000000b0: 0400 0000 0200 0000 0000 0000 0000 0000  ................
    // 000000c0: a081 4400 0000 6631 504b 0506 0000 0000  ..D...f1PK......
    // 000000d0: 0200 0200 6000 0000 6800 0000 0000       ....`...h.....
    @Test
    @Throws(Exception::class)
    fun createEntryFileMap_yieldsCorrectOffset() {
        // Write the test data (provided as a JAR resource) to a regular file.
        // JAR resources are preferred as input since they don't depend on the working directory, but we
        // want a real file for testing.
        val blob = File.createTempFile("prefix", "zip")
        javaClass.getResourceAsStream("/zip_with_gap.zip").use { input -> FileOutputStream(blob).use { output -> ByteStreams.copy(input, output) } }

        val zipFile = ZipFileRO.open(blob.toString())
        val entry = zipFile.findEntryByName("f1")
        val fileMap = zipFile.createEntryFileMap(entry)

        // The contents of file "f1" (i.e. "bar") appears at offset 0x64.
        assertThat(fileMap.dataOffset).isEqualTo(0x64)
    }

    @Test
    @Throws(Exception::class)
    fun open_emptyZip() {
        // ensure ZipFileRO cam handle an empty zip file with no central directory
        val blob = File.createTempFile("prefix", "zip")
        ZipOutputStream(FileOutputStream(blob)).use { zip -> }

        val zipFile = ZipFileRO.open(blob.toString())
        assertThat(zipFile).isNotNull()
    }
}