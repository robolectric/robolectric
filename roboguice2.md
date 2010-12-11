---
layout: default
title: Integrating RoboGuice and Robolectric part 2
---

### Test-only Bindings
During test it can be useful to use fake objects that produce well-known values in order to simplify assertions. Below
is an example of a fake <code>Provider&lt;Date&gt;</code>.

{% highlight java %}
/* bound as singleton so tests will get the same provider as the production code */
@Singleton
public class FakeDateProvider implements Provider<Date> {
    private Date date = new Date();

    @Override public Date get() {
        return date;
    }

    public void setDate(String dateString) {
        ...
    }
}
{% endhighlight %}

### Defining and Inserting a Test Module
A test module contains the bindings for our fake date provider.

{% highlight java %}
public class RobolectricSampleTestModule extends AbstractAndroidModule {
    @Override protected void configure() {
        bind(Counter.class).in(Scopes.SINGLETON);
        bind(Date.class).toProvider(FakeDateProvider.class);
    }
}
{% endhighlight %}

Create a new subclass of <code>GuiceApplication</code> that contributes your project's modules to the standard
Android modules that will be injected:

{% highlight java %}
public class SampleGuiceApplication extends GuiceApplication {
    private Module module = new RobolectricSampleModule();

    @Override protected void addApplicationModules(List<Module> modules) {
        modules.add(module);
    }
}
{% endhighlight %}

Add a new setter method so that a test module can be used instead:

{% highlight java %}
public class SampleGuiceApplication extends GuiceApplication {
    ...
    public void setModule(Module module) {
        this.module = module;
    }
}
{% endhighlight %}

Override the production module with a test module in the test runner, like this:

{% highlight java %}
public class InjectedTestRunner extends RobolectricTestRunner {
   ...
   @Override protected Application createApplication() {
       SampleGuiceApplication application =
           (SampleGuiceApplication) super.createApplication();
       application.setModule(new RobolectricSampleTestModule());

       return application;
   }
   ...
}
{% endhighlight %}

### Using the New Bindings
To take advantage of the bindings created for test, the production code needs to inject a <code>Date</code> instead of
instantiating one itself.

{% highlight java %}
public class InjectedActivity extends GuiceActivity {
    ...
    DateFormat dateFormat = DateFormat.getInstance();

    @Inject Date date;

    @Override protected void onCreate(Bundle savedInstanceState) {
        ...
        injectedTextView.setText(caption + " - " + dateFormat.format(date));
    }
}
{% endhighlight %}

The FakeDateProvider can then be injected into the test and used to set injected dates to a well-known value
that can be asserted:

{% highlight java %}
public class InjectedActivityTest {
    @Inject FakeDateProvider fakeDateProvider;

    @Before
    public void setUp() {
        fakeDateProvider.setDate("Dec 8, 2010");
    }

    @Test
    public void shouldAssignStringToTextView() throws Exception {
        injectedActivity.onCreate(null);
        TextView injectedTextView = (TextView) injectedActivity.findViewById(R.id.injected_text_view);

        assertThat(injectedTextView.getText().toString(),
                equalTo("Roboguice Activity tested with Robolectric - Dec 8, 2010"));
    }
}
{% endhighlight %}

