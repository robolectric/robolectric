package org.robolectric.res

import com.google.common.truth.Truth.assertThat
import java.io.StringReader
import java.nio.file.Paths
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.res.android.ResTable_config

@RunWith(JUnit4::class)
@SuppressWarnings("NewApi")
class StaxValueLoaderTest {

  private var resourceTable: PackageResourceTable? = null
  private var topLevelNodeHandler: NodeHandler? = null
  private var staxDocumentLoader: StaxDocumentLoader? = null

  @Before
  @Throws(Exception::class)
  fun setUp() {
    resourceTable = PackageResourceTable("pkg")

    topLevelNodeHandler = NodeHandler()
    staxDocumentLoader = StaxDocumentLoader("pkg", null, topLevelNodeHandler)
  }

  @Test
  @Throws(Exception::class)
  fun ignoresXliffTags() {
    topLevelNodeHandler!!.addHandler(
      "resources",
      NodeHandler()
        .addHandler("string", StaxValueLoader(resourceTable, "string", ResType.CHAR_SEQUENCE))
    )

    parse(
      "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">" +
        "<string name=\"preposition_for_date\">on <xliff:g id=\"date\" example=\"May 29\">%s</xliff:g></string>" +
        "</resources>"
    )

    assertThat(
        resourceTable!!.getValue(ResName("pkg:string/preposition_for_date"), ResTable_config()).data
      )
      .isEqualTo("on %s")
  }

  @Test
  @Throws(Exception::class)
  fun ignoresBTags() {
    topLevelNodeHandler!!.addHandler(
      "resources",
      NodeHandler()
        .addHandler(
          "item[@type='string']",
          StaxValueLoader(resourceTable, "string", ResType.CHAR_SEQUENCE)
        )
    )

    parse(
      "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">" +
        "<item type=\"string\" name=\"sms_short_code_details\">This <b>may cause charges</b> on your mobile account.</item>" +
        "</resources>"
    )
    assertThat(
        resourceTable!!.getValue(ResName("pkg:string/sms_short_code_details"), ResTable_config())
          .data
      )
      .isEqualTo("This may cause charges on your mobile account.")
  }

  @Throws(XMLStreamException::class)
  private fun parse(xml: String) {
    val factory = XMLInputFactory.newFactory()
    val xmlStreamReader = factory.createXMLStreamReader(StringReader(xml))
    val path = Paths.get("/tmp/fake.txt")
    val qualifiers = Qualifiers.fromParentDir(path.parent)
    staxDocumentLoader!!.doParse(xmlStreamReader, XmlContext("pkg", path, qualifiers))
  }
}
