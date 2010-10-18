===============================================================================
                                ANDROID EXAMPLES
                   How to get and installing the Scala library
                as .dex files onto the Android emulator or device 
===============================================================================

This directory must be populated with .dex files corresponding to the Scala
software version used to build Android applications (as several versions of
Scala may be present on the same machine). In particular it should looks as
follows in order to execute the provided "push-jars" Ant command:

   configs/framework/
      library.properties
      README.txt (this file)
      scala-actors.jar
      scala-collection.jar
      scala-immutable.jar
      scala-library.jar
      scala-mutable.jar

For convenience the above library files can be obtained in two different ways:

1) either you generate them using the bin/createdexlibs shell script located at
   the root of the "unlocking-android/" installation directory

2) or you copy them from the separately available scala-framework.zip which
   can be downloaded from the following web page (owned by Stephane
   Micheloud, past member of the Scala project team):

      http://lamp.epfl.ch/~/michelou/android/


Have fun!
The Scala Team

