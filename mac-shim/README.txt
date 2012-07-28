# Scala on Android, for Macs 

There are discrepancies in the variable/path names in the Android ant build files.  This file replicates Linux variable names for the Mac Android Ant build script.  It allows Mac users to build and rund Scala apps using the build-scala.xml file provided by this project.

## To use

1. Include `mac-shim.xml` in your project's root directory
2. Update `build.xml` in your project root to import the mac-shim.  Import after the Android build script, but before the Scala build script.  See below.
	````
	<import file="${sdk.dir}/tools/ant/build.xml" /> 
	<import file="mac-shim.xml" />
	<import file="build-scala.xml" />
	````

