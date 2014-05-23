# Robolectric Release Checklist

1. Perform a dry run of the release to make sure that everything works:

              mvn -DdryRun=true clean release:clean release:prepare

2. Perform the actual release build:

              mvn clean release:clean release:prepare

3. Double check that you have the release credentials in your settings.xml:

              <settings>
                     <servers>
                            <server>
                                   <id>sonatype-nexus-snapshots</id>
                                   <username>username</username>
                                   <password>password</password>
                            </server>
                            <server>
                                   <id>sonatype-nexus-staging</id>
                                   <username>username</username>
                                   <password>password</password>
                            </server>
                     </servers>
              </settings>

4. Upload the release artifacts to Sonatype:

              mvn release:perform

5. Log into Sonatype, close and release the staging repository.

6. Download the released Javadocs and check them into the `robolectric.github.io` repository.

7. Update the mailing list, Twitter account, and blog.

8. Sit back and have a beer.
