===============================================================================
                                ANDROID EXAMPLES
                  How to generate and install the Scala library
                as .dex files onto the Android emulator or device 
===============================================================================


This document presents the structure of the framework directory and describes
the usage of the installed shell and Ant build scripts (ant.apache.org/) to
generate and install the Scala library as .dex files onto the Android emulator
or device.


Directory structure
-------------------

The framework directory is organized as follows:

    scala-framework/
        android/
            build.properties
            build.xml
            src/
        bin/
            createdexlibs
        framework/
            scala-2.7.5.final/
                library.properties
                scala-actors.jar
                scala-collection.jar
                scala-immutable.jar
                scala-library.jar
                scala-mutable.jar
            scala-2.7.6.final/
                ... (as for 2.7.5.final)
            scala-2.7.7.final/
                README.txt
            scala-2.8.0.final/
                library.properties
                scala-actors.jar
                scala-collection.jar
                scala-immutable.jar
                scala-library.jar
                scala-mutable.jar
            scala-2.8.1.final/
                ... (as for 2.7.5.final)
            scala-2.9.0.1/
                ... (as for 2.7.5.final)
            scala-2.9.1.RC1/
                ... (as for 2.7.5.final)


Scala API for Android
---------------------

The "android/" project directory contains the source files of the Scala API
presented in the online article "Writing a Scala API for Android"
(http://lamp.epfl.ch/~michelou/android/scala-api-android.html):

The "scala-android.jar" library is for instance used by the following Android
examples:

    android-sdk/ApiDemos/libs/scala-android.jar
    android-sdk/JetBoy/libs/scala-android.jar
    android-sdk/LunarLander/libs/scala-android.jar
    android-sdk/ContactManager/libs/scala-android.jar

    apps-for-android/Panoramio/libs/scala-android.jar
    apps-for-android/Photostream/libs/scala-android.jar


Scala frameworks
----------------

The "bin/createdexlibs" shell script can be used to generate .dex files for
a particular version of the Scala library, e.g. the command

    >env SCALA_HOME=/opt/scala-2.9.0.1 bin/createdexlibs

creates a subdirectory "scala-2.9.0.1/" in the directory "framework/"
which contains the converted .dex files to be installed on the Android
emulator or device (those usage is described in each of the text files
android-sdk/INSTALL.txt and apps-for-android/INSTALL.txt).


Have fun!
The Scala Team

