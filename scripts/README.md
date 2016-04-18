# Robolectric build-android.sh tutorial

**Note:** Based on [Android documentation](https://source.android.com/source/downloading.html).

This tutorial will allow you to run the `build-android.sh` script in the Robolectric repository, resulting in the corresponding Android version's android-all jar.

## 1. Installing Repo
Repo is a tool that makes it easier to work with Git in the context of Android. For more information about Repo, see the Developing section.

To install Repo make sure you have a bin/ directory in your home directory and that it is included in your path:
```
$ mkdir ~/bin
$ PATH=~/bin:$PATH
```

Download the Repo tool and ensure that it is executable:
```
$ curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
$ chmod a+x ~/bin/repo
```

## 2. Initializing a Repo client
After installing Repo, set up your client to access the Android source repository:

Create an empty directory to hold your working files. If you're using MacOS, this has to be on a case-sensitive filesystem. Give it any name you like:
```
$ mkdir <WORKING_DIRECTORY>
$ cd <WORKING_DIRECTORY>
```

Configure git with your real name and email address. To use the Gerrit code-review tool, you will need an email address that is connected with a [registered Google account](https://myaccount.google.com/?pli=1). Make sure this is a live address at which you can receive messages. The name that you provide here will show up in attributions for your code submissions.
```
$ git config --global user.name "Your Name"
$ git config --global user.email "you@example.com"
```

Run `repo init` to bring down the latest version of Repo with all its most recent bug fixes. You must specify a URL for the manifest, which specifies where the various repositories included in the Android source will be placed within your working directory.
```
$ repo init -u https://android.googlesource.com/platform/manifest

```

To check out a branch other than `master`, specify it with `-b`. For a list of branches, see [Source Code Tags and Builds](https://source.android.com/source/build-numbers.html#source-code-tags-and-builds). The source code must match one of those supported by Robolectric (unless a new API is being supported):

*  `4.1.2_r1`    - Jelly Bean
*  `4.2.2_r1.2`  - Jelly Bean MR1
*  `4.3_r2`      - Jelly Bean MR2
*  `4.4_r1`      - Kit Kat
*  `5.0.0_r2`    - Lollipop
*  `5.1.1_r9`    - Lollipop MR1
*  `6.0.0_r1`    - Marshmallow
```
$ repo init -u https://android.googlesource.com/platform/manifest -b android-6.0.0_r1
```

A successful initialization will end with a message stating that Repo is initialized in your working directory. Your client directory should now contain a `.repo` directory where files such as the manifest will be kept.

## 3. Downloading the Android Source Tree
To pull down the Android source tree to your working directory from the repositories as specified in the default manifest, run
```
$ repo sync -c -j100
```

Beware this will take over an hour and will require 45+ Gigabytes of space. If you face download quota restrictions, try the [Using Authentication](https://source.android.com/source/downloading.html#using-authentication) section.

## 4. Grab dependencies
On Ubuntu run
```
$ sudo apt-get install git-core gnupg flex bison gperf build-essential \
zip curl zlib1g-dev gcc-multilib g++-multilib libc6-dev-i386 \
lib32ncurses5-dev x11proto-core-dev libx11-dev lib32z-dev ccache \
libgl1-mesa-dev libxml2-utils xsltproc unzip libswitch-perl
```

## 5. Set environment variables
```
$ export BUILD_ROOT=<Path to AOSP source directory>
$ export SIGNING_PASSWORD=<Passphrase for GPG signing key>
$ source build/envsetup.sh
```

## 6. Build AOSP
**NOTE:** For Kitkat and below Java 7 and 8 are not supported. Use [Oracle JDK 6](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html) instead.

For Jellybean 4.2 and below:
```
$ lunch generic_x86-eng
$ make -j8
```

For Jellybean 4.3 to Lollipop:
```
$ lunch aosp_x86-eng
$ make -j8
```

For Marshmallow and above:

```
$ tapas core-libart services services.accessibility telephony-common framework ext icu4j-icudata-jarjar
$ ANDROID_COMPILE_WITH_JACK=false make -j8
```

## 7. Run build-android.sh
(Optional) Signing Artifacts:
The end of the script will prompt you to sign the new artifacts.  You will be prompted a total of 4 times (once for each artifact).  To make this easier, run this command beforehand:
```
$ gpg-agent --daemon
```

Finally, in your Robolectric directory run:
```
$ build-android.sh <android version> <robolectric sub-version>
```

For Robolectric version `6.0.0_r1-robolectric-0`, android version would be `6.0.0_r1` and  robolectric sub-version `0`.
