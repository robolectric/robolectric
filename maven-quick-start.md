---
  layout: default
  title: Maven Quick Start
---

## Setup
Maven setup presents some challenges, namely telling Maven where the Android SDK jars live, and telling Maven how to build your Android application. 

### Google API Jars
The Google API jars, such as the `maps.jar`, are not available for download from Sonatype. If you plan on using a Google API add-ons you may be interested in [maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer). Using this project will make the maps jars available to your local Maven install. 


### Deploying .apks
The [maven-android-plugin](http://code.google.com/p/maven-android-plugin/) makes it easy to add Robolectric to your
Android project. 

----------------------

##Project Creation
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

        <!-- Make sure this is below the android dependencies -->
        <dependency>
            <groupId>org.robolectric</groupId>
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
                <groupId>com.jayway.maven.plugins.android.generation2</groupId>
                <artifactId>android-maven-plugin</artifactId>
                <version>3.5.0</version>
                <configuration>
                    <sdk>
                        <!-- platform or api level (api level 16 = platform 4.1)-->
                        <platform>16</platform>
                    </sdk>
                    <undeployBeforeDeploy>true</undeployBeforeDeploy>
                </configuration>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
{% endhighlight %}

Note that you need Robolectric and JUnit 4 in 'test' scope, and android (and maps if you're using it) in 'provided' scope.

### Prepare directory structures
You'll need the standard <code>AndroidManifest.xml</code> file in your root directory, as well as something like
the following files:
<pre>
  /res/layout/main.xml
  /res/values/strings.xml
  /src/main/java/com/pivotallabs/MyActivity.java
  /src/test/java/com/pivotallabs/MyActivityTest.java
</pre>

You should then be able to build your project and run tests using <code>mvn install</code>.

### Verify your setup
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

Typing: <code>mvn clean test</code> will run the tests.

### Importing into an IDE
If you're using JetBrains' excellent [IntelliJ IDEA](http://www.jetbrains.com/idea/), just open your
<code>pom.xml</code> as a project, and you're set.

If you're using Eclipse, you can try to get it to work by following the instructions [here](eclipse-quick-start.html).

When running tests from within IntelliJ or Eclipse, be sure to run tests using the JUnit runner and not the Android Test
runner.
