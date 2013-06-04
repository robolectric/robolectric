# Robolectric Release Checklist

Set these properly:
```bash
ROBO_SRC=~/Development/robolectric
ROBO_DOCS=~/Development/robolectric-docs
```

* Ensure every story for this release in the Tracker project is accepted.
* Update release notes at `$ROBO_DOCS/release-notes.md`.
* `cd $ROBO_SRC && mvn clean release:clean release:prepare` (by xian)
* `cd $ROBO_SRC && mvn release:perform` (by xian)
* Update javadocs

```bash
cd $ROBO_DOCS
git fetch
git st # make sure there are no changes...
rm -rf javadoc/*
mv $ROBO_SRC/target/apidocs/* javadoc/
grm
git commit -am "Update javadocs."
git push
```

* Post to [blog](http://robolectric.blogspot.com/)
* Post to [twitter](http://twitter.com/Robolectric)
* Email to [google group](http://groups.google.com/group/robolectric)
* Update [RobolectricSample](https://github.com/robolectric/RobolectricSample)
