---
  layout: default
  title: Robolectric Eclipse Quick Start
---

##Quick Start for Eclipse

###Project Creation
-----------------------
Create a project
- File -> New -> Project... -> Android -> Android Project
- Click "Next"

New Android Project dialog
- Project Name: MyProject
- Build Target: Click Target Name: "Google APIs", Vendor: "Google Inc.", Platform "2.2", Api Level: "8"
- Type 'com.example' in package name
- Check Create Activity, enter "MyActivity"
- Click "Finish" (Do NOT create an Android Test project)

Add a source test directory to your project
- Right click on 'MyProject' in the package explorer -> New... -> Folder
- Folder name: test (do not make this a source folder for this project - hang tight)
- Click "Finish"

###Create a *JAVA* project for your tests
------------------------

Create and configure test Java project
- File -> New -> Java Project...
- Project Name: MyProjectTest
- Click "Next"
- Expand the MyProjectTest row and select the "src" row
- Click link "Remove source folder 'src' from build path"
- Click link "Link additional source"
- Browse to and select ".../MyProject/test"
- Click "Finish" on the "Link additional sources" dialog (keep the new Java project dialog open)

Add dependency on the Android project
- Select "Projects" tab at the top of the New Java Project dialog
- Click "Add..."
- Check "MyProject"
- Click "OK"
- Click "Finish" closing the new Java project dialog

###Add required directory structure and jars to test project
At the command line:
<pre>
mkdir -p .../MyProjectTest/lib
cp .../robolectric-x.x.x-all.jar .../MyProjectTest/lib
</pre>

###Configure build path
Back in Eclipse
- Right click "MyProjectTest"
- Select "Refresh"
- Right click "MyProjectTest"
- Select "Build Path" -> "Configure Build Path..."

Add JUnit library
- Select "Libraries" tab at the top of the Properties dialog for MyProjectTest
- Click "Add Library"
- Select "JUnit"
- Click "Next"
- Select JUnit library version 4 (Robolectric is *not* compatible with JUnit 3)
- Click "Finish" (keep the Properties dialog for MyProjectTest open)

Add Robolectric jar
- Click "Add JARs..."
- Expand MyProjectTest -> lib
- Select robolectric-x.x.x-all.jar
- Click "OK" (keep the Properties dialog for MyProjectTest open)

Add Android Jars
- Click "Add External Jars..."
- Navigate to &lt;your android install directory&gt;/platforms/android-8/android.jar
- Click "Open"  (keep the Properties dialog for MyProjectTest open)
- Click "Add External Jars..."
- Navigate to &lt;your android install directory&gt;/add-ons/addon_google_apis_google_inc_8/libs/maps.jar
- Click "Open"
- Click "OK" on the Properties for MyProjectTest dialog

### Create a test Run Configuration
-----------------------------------------------
Your tests will *not* run without this step. Your resources will not be found.
- "Run" -> "Run Configurations..."
- Double click "*JUnit*" (not "Android JUnit Test")
- Name: MyProjectTestConfiguration
- Select the "Run all tests in the selected project, package or source folder:" radio button
- Click the "Search" button
- Select "MyProjectTest"
- TestRunner: JUnit 4
- Click on the link "Multiple launchers available Select one..." at the bottom of the dialog
- Check the "Use configuration specific settings" box
- Select "Eclipse JUnit Launcher"
- Click "OK"
- Click the "Arguments" tab
- Under "Working directory:" select the "Other:" radio button
- Click "Workspace..."
- Select "MyProject" (*not* "MyProjectTest", The value inside of 'Other' edit box should be '${workspace_loc:MyProject}')
- Click "OK"
- Click "Close"

### Verify your setup
--------------------------------------------------------------------------------------------
- Right click the "test" folder under "MyProjectTest"
- Select "New"->"Class"
- Package: "com.example"
- Name: "MyActivityTest"
- Click "Finish"
- Add the following source:
{% highlight java %}
package com.example;

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
        String hello = new MyActivity().getResources().getString(R.string.hello);
        assertThat(hello, equalTo("Hello World, MyActivity!"));
    }
}
{% endhighlight %}

To run the tests
- "Run" -> "Run Configurations..."
- Select "JUnit" -> "MyProjectTestConfiguration"
- Click "Run"

###*If you get a RuntimeException saying: "no such layout layout/main"*
--------------------------------------------------------------------
It means that you have tried to run a test for which you do not have a Run Configuration set up. To remedy this:
- Right click on the test
- "Run As" -> "Run Configurations..."
- Double click "JUnit" (this will magically make the test you're running appear under JUnit)
- Select "MyActivityTest" (or the name of whichever test you are currently trying to run)
- TestRunner: JUnit 4
- Click on the link "Multiple launchers available Select one..." (or it also may appear as "Using XXX Launcher - Select
other...") at the bottom of the dialog
- Check the "Use configuration specific settings" box
- Select "Eclipse JUnit Launcher"
- Click "OK"
- Click the "Arguments" tab
- Under "Working directory:" select the "Other:" radio button
- Click "Workspace..."
- Select "MyProject" (*not* "MyProjectTest")
- Click "OK"
- Click "Close"


Are these instructions helpful? confusing? [Let us know](http://groups.google.com/group/robolectric)! any feedback is helpful. Thanks -Robolectric