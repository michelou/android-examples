INTRODUCTION

SpriteText shows these features:

- How to use OpenGL textures to draw static and dynamic text.
- How to find the 2D screen coordinates corresponding to a 3D point.
- How to keep track of the current transformation matrices.
- How to display a miliseconds per frame counter.

BUILDING

The project directory is ready for use from either Eclipse or with the
activityCreator.py script (if you want to use something other than
eclipse).

To use Eclipse, install Eclipse, the Android SDK and the Eclipse ADT
plugin (if you haven't already, see
http://code.google.com/android/intro/installing.html for more details)
then start Eclipse, and create a new Android project. Use the
"from existing source" option, and browse to this directory. The
Android project should then be created, and you can browse the source,
build the application and run it in the emulator.

To build the application with activityCreator, start a command line
and change to the SpriteText directory, then type
activityCreator.py com.google.android.opengles.spriteText.SpriteText

(It is assumed you have set up activityCreator.py, ant and emulator
to run from the command line. Please see
http://code.google.com/android/intro/installing.html#otherides
for more details)

It should give some warnings about files already existing, this is
expected. Then type:

ant

and the project should build.

Start the emulator and then install the SpriteText.apk file from the
bin directory:

adb install SpriteText.apk

You should then be able to run the app.

