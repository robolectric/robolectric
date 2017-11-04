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

## 3. Grab dependencies
On Ubuntu run
```
$ sudo apt-get install git-core gnupg gnupg-agent flex bison gperf build-essential \
zip curl zlib1g-dev gcc-multilib g++-multilib libc6-dev-i386 \
lib32ncurses5-dev x11proto-core-dev libx11-dev lib32z-dev ccache \
libgl1-mesa-dev libxml2-utils xsltproc unzip libswitch-perl
```

## 4. Run sync-android.sh to sync and build source
Now use repo to sync the android source, and then build.

```
./sync-android.sh <root source location> <android version> <# parallel jobs>
```

The currently supported Android versions are:

*  `4.1.2_r1`    - Jelly Bean API 16
*  `4.2.2_r1.2`  - Jelly Bean MR1 API 17
*  `4.3_r2`      - Jelly Bean MR2 API 18
*  `4.4_r1`      - Kit Kat API 19
*  `5.0.2_r3`    - Lollipop API 21
*  `5.1.1_r9`    - Lollipop MR1 API 22
*  `6.0.1_r3`    - Marshmallow API 23
*  `7.0.0_r1`    - Nougat API 24
*  `7.1.0_r7`    - Nougat MR1 API 25
*  `8.0.0_r4`    - Oreo API 26

Beware it can take upwards of 100 GB of space to sync and build. 

For more infomation see [Downloading and Building](https://source.android.com/source/requirements)

Choose a <# parallel jobs> value roughly equal to # of free cores on your machine. YMMV.


## 7. Run build-android.sh

Signing Artifacts:
The end of the script will prompt you to sign the new artifacts using GPG. See [Performing a Release](https://github.com/robolectric/robolectric-gradle-plugin/wiki/Performing-a-Release) for instructions on obtaining a GPG key pair.

(Optional) You will be prompted a total of 4 times (once for each artifact). To make this easier, run this command beforehand:
```
$ gpg-agent --daemon
```

Finally, in your Robolectric directory run:
```
$ export SIGNING_PASSWORD=<Passphrase for GPG signing key>
$ build-android.sh <Path to AOSP source directory> <android version> <robolectric sub-version>
```

For Robolectric version `6.0.1_r3-robolectric-0`, android version would be `6.0.1_r3` and  robolectric sub-version `0`.
