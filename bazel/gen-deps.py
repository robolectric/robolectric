import sys

def getKey(jar):
    key = "org.robolectric\:android-all\:"
    version = jar.split("/")[3].strip()[12:-4]
    key = key + version
    return key

def getValue(jar):
    return "../../" + jar[jar.index("org_robolectric"):]

def main(argv):
    for jar in argv[1:]:
        key = getKey(jar)
        value = getValue(jar)
        print ("%s=%s" % (key, value))

if __name__ == "__main__":
    main(sys.argv)
