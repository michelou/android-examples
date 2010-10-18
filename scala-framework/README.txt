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
            scala-2.7.7.final/
                README.txt
            scala-2.8.0.final/
                library.properties
                scala-actors.jar
                scala-collection.jar
                scala-immutable.jar
                scala-library.jar
                scala-mutable.jar
            scala-2.8.1.RC3/
                ... (as for 2.8.0.final)
            scala-2.9.0.r23260/
                ... (as for 2.8.0.final)


Have fun!
The Scala Team

