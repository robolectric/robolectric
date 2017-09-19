package org.robolectric.shadows;

import android.content.res.Resources;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(TestRunners.MultiApiSelfTest.class)
@Config(sdk = Build.VERSION_CODES.N_MR1)
public class XmlPullParserTest {

    @Test
    public void xmlParser() throws IOException, XmlPullParserException {
        Resources resources = RuntimeEnvironment.application.getResources();
        XmlPullParser parser = resources.getXml(R.xml.xml_attrs);
        assertThat(parser).isNotNull();

        assertThat(parser.getAttributeCount()).isEqualTo(-1);

        assertThat(parser.next()).isEqualTo(XmlPullParser.START_DOCUMENT);
        assertThat(parser.next()).isEqualTo(XmlPullParser.START_TAG);

        assertThat(parser.getName()).isEqualTo("whatever");
        int attributeCount = parser.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            System.out.println(parser.getAttributeName(i)
                + ": value=" + parser.getAttributeValue(i)
                + " type=" + parser.getAttributeType(i)
                + " ns=" + parser.getAttributeNamespace(i)
            );
        }
        assertThat(parser.getAttributeCount()).isEqualTo(4);
        assertThat(parser.getAttributeValue(0)).isEqualTo("1111");
        //parser.next();
        //parser.next();
    }
}
