---
layout: default
title: User Guide
---

## Extending Robolectric
Robolectric is a work-in-progress, and we welcome contributions from the community. We encourage developers to [use the standard GitHub workflow](http://help.github.com/fork-a-repo/ "Help.GitHub - Fork A Repo") to fork, enhance, and submit pull requests to us.

### Shadow Classes
Robolectric defines many shadow classes, which modify or extend the behavior of classes in the Android OS. When an Android class is instantiated, Robolectric looks for a corresponding shadow class, and if it finds one it creates a shadow object to associate with it. Every time a method is invoked on an Android class, Robolectric ensures that the shadow class' corresponding method is invoked first (if there is one), so it has a chance to work its magic. This applies to all methods, even static and final methods, because Robolectric is extra tricky!

#### What's in a Name?
Why "Shadow?" Shadow objects are not quite [Proxies](http://en.wikipedia.org/wiki/Proxy_pattern "Proxy pattern - Wikipedia, the free encyclopedia"), not quite [Fakes](http://c2.com/cgi/wiki?FakeObject "Fake Object"), not quite [Mocks or Stubs](http://martinfowler.com/articles/mocksArentStubs.html#TheDifferenceBetweenMocksAndStubs "Mocks Aren't Stubs"). Shadows are sometimes hidden, sometimes seen, and can lead you to the real object. At least we didn't call them "sheep", which we were considering.

### Adding Functionality
If the shadow classes provided with Robolectric don't do what you want, it's possible to change their behavior for a single test, a group of tests, or for your whole suite. Simply declare a class (let's say <code>ShadowFoo</code>) and annotate it <code>@Implements(Foo.class)</code>. Your shadow class may extend one of the stock Robolectric shadows if you like. To let Robolectric know about your shadow, annotate your test method or class with the <code>@Config(shadows=ShadowFoo.class)</code>, or create a file called <code>org.robolectric.Config.properties</code> containing the line <code>shadows=my.package.ShadowFoo</code>.

From Robolectric 2.0 on, the number of shadow classes needed is greatly reduced, because real Android OS code is present.

The library of Shadow classes supplied with Robolectric does not cover the entire Android API. Even if it did, some projects will require behavior that differs from what is in the library. When these situations are encountered it will be necessary to extend existing or add new Shadow classes. Creating new Shadow classes or adding methods to exiting Shadows is easy. Here is an outline of the process, details about each step will follow:

- **Clone the [Robolectric project on GitHub](https://github.com/robolectric/robolectric/):**
We very often make Robolectric a [`git submodule`](http://book.git-scm.com/5_submodules.html "Git Book - Submodules") of the project that we are working on in order to make it easier to add Robolectric functionality as we need it, but you could also create dependencies between projects or build and copy `.jar` files depending on your needs. See [GitHub - Fork A Repo](http://help.github.com/fork-a-repo/ "Help.GitHub - Fork A Repo").

- **Add tests for your Shadow class:**
They live in the `org.robolectric.shadows` package under the `code/tests` folder

- **Develop the implementation:**
Put it in the same package under `code/src`. There are lots of Shadow classes that are already implemented there that can be used as examples. The most important aspects of writing a Shadow class are described below.

- **Register your new class with the Robolectric framework:**
Add it to the list returned by `Robolectric.getDefaultShadowClasses()` and also add an implementation of `Robolectric.shadowOf()`. Just duplicate the examples that are already in the Robolectric class.

#### Shadow Classes
Shadow classes always need a public no-arg constructor so that the Robolectric framework can instantiate them. They are associated to the class that they Shadow with an `@Implements` annotation on the class declaration. In general, they should be implemented as if from scratch, the facilities of the classes they Shadow have almost always been removed and their data members are difficult to access. The methods on a Shadow class usually either Shadow the methods on the original class or facilitate testing by setting up return values or providing access to internal state or logged method calls.

Shadow classes should mimic the production classes' inheritance hierarchy. For example, if you are implementing a Shadow for `ViewGroup`, `ShadowViewGroup`, then your Shadow class should extend `ViewGroup`'s superclass's Shadow, `ShadowView`.  

{% highlight java %}
  ...
  @Implements(ViewGroup.class)
  public class ShadowViewGroup extends ShadowView {
  ...
{% endhighlight %}

#### Methods
Shadow objects implement methods that have the same signature as the Android class. Robolectric will invoke the method on a Shadow object when a method with the same signature on the Android object is invoked.

Suppose an application defined the following line of code:
{% highlight java %}
  ...
  this.imageView.setImageResource(R.drawable.pivotallabs_logo);
  ...
{% endhighlight %}

Under test the `ShadowImageView#setImageResource(int resId)` method on the Shadow instance would be invoked.

Shadow methods must be marked with the `@Implementation` annotation. Robolectric includes a lint test to help ensure this is done correctly.

{% highlight java %}
@Implements(ImageView.class)
public class ShadowImageView extends ShadowView {
  ...	
  @Implementation
  public void setImageResource(int resId) {
    // implementation here.
  }
}
{% endhighlight %}

It is important Shadow methods are implemented on the corresponding Shadow of the class in which they were originally defined. Otherwise Robolectric's lookup mechanism will not find them (even if they have been declared on a Shadow subclass.) For example, the method `setEnabled()` is defined on View. If a `setEnabled()` method is defined on `ShadowViewGroup` instead of `ShadowView` then it will not be found at run time even when `setEnabled()` is called on an instance of `ViewGroup`. 

#### Shadowing Constructors
Once a Shadow object is instantiated, Robolectric will look for a method named  `__constructor__` which has the same arguments as the constructor that was invoked on the real object.

For instance, if the application code were to invoke the TextView constructor which receives a Context:

{% highlight java %}
new TextView(context);
{% endhighlight %}

Robolectric would invoke the following  `__constructor__` method that receives a Context:

{% highlight java %}
@Implements(TextView.class)
public class ShadowTextView {
  ...
  public void __constructor__(Context context) {
    this.context = context;
  }
  ...
{% endhighlight %}

#### Getting access to the real instance
Sometimes Shadow classes may want to refer to the object they are shadowing, e.g. to manipulate fields. A Shadow class can accomplish this by declaring a field annotated `@RealObject`:

{% highlight java %}
@Implements(Point.class)
public class ShadowPoint {
  @RealObject private Point realPoint;
  ...
  public void __constructor__(int x, int y) {
    realPoint.x = x;
    realPoint.y = y;
  }
}
{% endhighlight %}

Robolectric will set realPoint to the actual instance of  `Point` before invoking any other methods.

It is important to note that methods called on the real object will still be intercepted and redirected by Robolectric. This does not often matter in test code, but it has important implications for Shadow class implementors. Since the Shadow class inheritance hierarchy does not always mirror that of their associated Android classes, it is sometimes necessary to make calls through these real objects so that the Robolectric runtime will have the opportunity to route them to the correct Shadow class based on the actual class of the object. Otherwise methods on Shadows of base classes would be unable to access methods on the Shadows of their subclasses.
