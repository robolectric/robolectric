---
  layout: default
  title: Maven Quick Start
---

##Quick Start for Maven

The [maven-android-plugin](http://code.google.com/p/maven-android-plugin/) makes it easy to add Robolectric to your
Android project.


###Project Creation
----------------------
Create a file named <code>pom.xml</code> in the root of your project based on this example:

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>MySampleApp</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>apk</packaging>
    <name>My Sample App</name>

    <dependencies>
        <dependency>
            <groupId>com.google.android</groupId>
            <artifactId>android</artifactId>
            <version>2.2.1</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.google.android</groupId>
            <artifactId>android-test</artifactId>
            <version>2.2.1</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.google.android.maps</groupId>
            <artifactId>maps</artifactId>
            <version>8_r2</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.pivotallabs</groupId>
            <artifactId>robolectric</artifactId>
            <version>X.X.X</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.8.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>

        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.3.2</version>
            </plugin>
            <plugin>
                <groupId>com.jayway.maven.plugins.android.generation2</groupId>
                <artifactId>maven-android-plugin</artifactId>
                <version>2.8.3</version>
                <configuration>
                    <sdk>
                        <!-- platform or api level (api level 8 = platform 2.2)-->
                        <platform>8</platform>
                    </sdk>
                </configuration>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
{% endhighlight %}

Note that you need Robolectric and JUnit 4 in 'test' scope, and android, android-test, and maps in 'provided' scope.


### Prepare directory structures
------------------------------
You'll need the standard <code>AndroidManifest.xml</code> file in your root directory, as well as something like
the following files:
<pre>
  /res/layout/main.xml
  /res/values/strings.xml
  /src/main/java/com/pivotallabs/MyActivity.java
  /src/test/java/com/pivotallabs/MyActivityTest.java
</pre>

You should then be able to build your project and run tests using <code>maven test</code>.

### Verify your setup
--------------------------------------------------------------------------------------------
In Project View, right click on MyProject>code>test -> New -> Java class ->  MyActivityTest
Add the following source:

{% highlight java %}
import com.example.MyActivity;
import com.example.R;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MyActivityTest {

    @Test
    public void shouldHaveHappySmiles() throws Exception {
        String appName = new MyActivity().getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("MyActivity"));
    }
}

{% endhighlight %}

Typing: <code>mvn clean test</code> will run the test.

### Importing into an IDE

To open the project in IntelliJ: File -> Open Project... -> select the pom.xml.

To open the project in Eclipse, make sure you have the
[M2Eclipse plugin](http://m2eclipse.sonatype.org/installing-m2eclipse.html) installed, then:
*  android update project -p $PATH --target xxx
* Import... -> Existing Maven Projects, find your project.
* &lt;right click project&gt; -> Android -> Convert to Android Project
* &lt;right click "gen" folder&gt; -> Build Path -> Add to Build Path

When running tests from within IntelliJ or Eclipse, be sure to run tests using the JUnit runner, not the Android Test
runner.

### Troubleshooting

#### No Android SDK path could be found.

If Maven reports "No Android SDK path could be found." you need to tell Maven where to find Android by making your
<code>~/.m2/settings.xml</code> file look something like this:

{% highlight xml %}
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
            http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <profiles>
            <profile>
                <id>android</id>
                <properties>
                    <android.sdk.path>
                        PATH / TO / THE / ANDROID / SDK
                    </android.sdk.path>
                </properties>
            </profile>
        </profiles>
        <activeProfiles>
            <!--make the profile active all the time -->
            <activeProfile>android</activeProfile>
        </activeProfiles>
    </settings>
{% endhighlight %}
