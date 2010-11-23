---
  layout: default
  title: Robolectric IntelliJ Quick Start
---

##Quick Start for IntelliJ

###Project Creation
----------------------
Create a project
- File -> New Project
- Name: MyProject
- Select Type:  "Android Module"
- Next

Fill in the source directory
- "Create source directory" (radio):
- Enter: code/src
- Next

Select the SDK
_<br>(You may need to run the Android tool to download/install an sdk version. Robolectric REQUIRES a Google Apis version of the sdk.)_
- SDK properties: Android X.X Google APIs
- Finish


###Prepare directory structures
------------------------------
At the command line:
<pre>
mkdir -p .../MyProject/code/libs/test
mkdir -p .../MyProject/code/libs/main    #production jars go here e.g. roboguice
mkdir -p .../MyProject/code/test
mkdir -p .../MyProject/code/gen
</pre>

###Install downloaded jars
-------------------------------
<pre>
cp robolectric-X.X-all.jar .../MyProject/code/libs/test
cp junit-4.x.x.jar .../MyProject/code/libs/test
</pre>


###Configure the IntelliJ project
-------------------------------
Open the Modules tab of Project Settings
- In File -> Project Structure...
- Project Settings -> Select "Modules"

Create a new module
- Press the "+" in the tool bar at the top of the dialog to create a new module
- "Create module from scratch" radio -> Next
<blockquote>
	Name: code<br>
	Content root: .../MyProject/code 	# default value<br>
	Module file location: .../MyProject/code  	# default value<br>
	Type: java  	# default selection<br>
	Next<br>
</blockquote>
- Accept .../MyProject/src [java] as a source root<br>
- Next<br>

No additional facets/technologies required

- Finish


###Configure generated source directories
-------------------------
In the Modules tab of Project Settings
- Select "Modules"
- Expand the MyProject module's node
- select "Android" facet
- select "compiler"
- Under AAPT Complier - Destination Directory enter: <code>.../MyProject/code/gen</code>
- Under AIDL Compiler - Destination Directory enter: <code>.../MyProject/code/gen</code>

###Remove unused source directories from the main project
------------------------------
_(you may have to do this several times since IntelliJ
automatically replaces this setting from time to time)_
- Select the MyProject module "Sources" tab
- Delete the "gen" source folder by clicking the "x" at the end of its line
- Delete the "code/gen" source folder by clicking the "x" at the end of its line

###Set up source directories for the "code" module
-------------------------------
- Select the "code" module --> "Sources" tab
- In the source tree out on the far right, select the <code>.../MyProject/code/test</code>
folder and click the green "Test Sources" button above the source tree, adding it as a test source folder
- Select the <code>.../MyProject/code/gen</code> folder and click the blue "Sources" button, adding it as a source folder
- Click "Apply"

NOTE: you may get an error dialog here reading:<br>
"Cannot save settings   Module 'MyProject' must not contain source root .../MyProject/code/src.  The root already
belongs to module 'code'"<br>
To fix this problem follow the steps under "Removed unused source directories from the main project" above.

###Set up dependencies for the "code" module
-------------------------------
- Select the "code" module --> "Dependencies" tab

Add the Robolectric jar
- "Add..." --> "Single Entry Module Library"
- Select .../MyProject/code/libs/test/robolectic-X.X-all.jar
- Click "Ok"
- Click on "Compile" at the end of the newly-created dependency line, and choose "Test" from the resulting list

Add the JUnit jar
-"Add..." --> "Single Entry Module Library"
- Select .../MyProject/code/libs/test/junit-4.x.x.jar
- Click "Ok"
- Click on "Compile" at the end of the newly-created dependency line, and choose "Test" from the resulting list

Add the Android libraries
- "Add..." --> "Library"
- Select "Android X.X Google Apis"  # must be GOOGLE android apis and not just plain android apis

NOTE: Android X.X Google Apis MUST be moved below the junit and robolectric jar.
- Move the Android jar down so it is last in the list.
- Click "Apply"

###Set up dependencies for the main Android Project module
----------------------------
Select "MyProject" module --> "Dependencies tab"

Add the "code" module
- Click "Add..." --> "Module Dependency..."
- Select "code"
- Click "OK"
- Move the newly created dependency "code" row above the Android line by clicking the "Move Up" button
- Check the "code" row's export box
- Leave the code module scope at the default of "scope" --> "compile"
- Click	"Apply"

###Set up exclusions for the main Android Project module
--------------------------------------------------------
- Select "MyProject" module --> "Sources tab"
- In the source tree out on the far right, select the "bin" folder and click the 'Excluded' button above, adding it as an excluded folder
- Click "OK"



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
        MyActivity activity = new MyActivity();
        assertThat(activity.getResources().getString(R.string.app_name), equalTo("MyActivity"));
    }
}

{% endhighlight %}


- Cntl-Shift-f10 will run the test
