@echo off

rem ##########################################################################
rem # Copyright 2009-2011, LAMP/EPFL
rem #
rem # This is free software; see the distribution for copying conditions.
rem # There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A
rem # PARTICULAR PURPOSE.
rem ##########################################################################

rem find the root folder for this distribution
if "%OS%"=="Windows_NT" (
  @setlocal
  call :set_root
  set _ARGS=%*
)

if not "%ANDROID_SDK_ROOT%"=="" goto emulator

rem guess1
if not exist "%SystemDrive%\android-sdk-windows\tools\emulator.exe" goto guess2
set ANDROID_SDK_ROOT=c:\android-sdk-windows
goto emulator

:guess2
if not exist "%ProgramFiles%\android-sdk-windows\tools\emulator.exe" goto error1
set ANDROID_SDK_ROOT=c:\Progra~1\android-sdk-windows

:emulator
set _EMULATOR=%ANDROID_SDK_ROOT%\tools\emulator.exe
if not exist "%_EMULATOR%" goto error2

rem pick up the more recent version of the dx tool
set _DX=%ANDROID_SDK_ROOT%\platform-tools\dx.bat
if not exist "%_DX%" goto error3

if "%SCALA_HOME%"=="" (
    # guess the location of the Scala installation directory
    if exist "c:\Progra~1\scala\lib\scala-library.jar" (
      set SCALA_HOME=c:\Progra~1\scala
    )
    if not exist "%SCALA_HOME" goto error4
)
rem SCALA_VERSION=`$SCALA_HOME/bin/scala -version | sed "s/\(.*\)version \(.*\) --.*/\2/"`
set SCALA_VERSION=2.x
echo Using Scala %SCALA_VERSION% in %SCALA_HOME%

set _FRAMEWORK_DIR=%_ROOT%\framework

set TMP_DIR=%_FRAMEWORK_DIR%\.createdexlibs_tmp
set JVM_LIBS_DIR=%TMP_DIR%\jvm-libs
set DEX_LIBS_DIR=%_FRAMEWORK_DIR%\scala-%SCALA_VERSION%
set DEX_LIBS_DIR2=framework\scala-%SCALA_VERSION%

set _JARCMD=%JAVA_HOME%\bin\jar.exe
if not exist "%_JARCMD%" goto error5

set _COPYCMD=copy /b /d /y
set _XCOPYCMD=xcopy /e /q /r /y

rem ##########################################################################
rem ## Split the Scala library into five smaller pieces (original library
rem ## is too large for the dx tool) and convert them into dex files.

mkdir %JVM_LIBS_DIR%\scala-library\scala 2>nul
mkdir %JVM_LIBS_DIR%\scala-collection\scala 2>nul
mkdir %JVM_LIBS_DIR%\scala-immutable\scala\collection 2>nul
mkdir %JVM_LIBS_DIR%\scala-mutable\scala\collection 2>nul
mkdir %JVM_LIBS_DIR%\scala-actors\scala 2>nul
mkdir %DEX_LIBS_DIR% 2>nul

set _WORKING_DIR=%cd%
cd %JVM_LIBS_DIR%\scala-library\
echo Generating scala-library.jar...
%_JARCMD% xf %SCALA_HOME%\lib\scala-library.jar
echo Generating scala-collection.jar...
%_COPYCMD% library.properties %JVM_LIBS_DIR%\scala-collection\ 1>nul
%_XCOPYCMD% META-INF %JVM_LIBS_DIR%\scala-collection\META-INF\ 1>nul
%_XCOPYCMD% scala\collection %JVM_LIBS_DIR%\scala-collection\scala\collection\ 1>nul
echo Generating scala-immutable.jar...
%_COPYCMD% library.properties %JVM_LIBS_DIR%\scala-immutable\ 1>nul
%_XCOPYCMD% META-INF %JVM_LIBS_DIR%\scala-immutable\META-INF\ 1>nul
%_XCOPYCMD% scala\collection\immutable %JVM_LIBS_DIR%\scala-immutable\scala\collection\immutable\ 1>nul
echo Generating scala-mutable.jar...
%_COPYCMD% library.properties %JVM_LIBS_DIR%\scala-mutable\ 1>nul
%_XCOPYCMD% META-INF %JVM_LIBS_DIR%\scala-mutable\META-INF\ 1>nul
%_XCOPYCMD% scala\collection\mutable %JVM_LIBS_DIR%\scala-mutable\scala\collection\mutable\ 1>nul
echo Generating scala-actors.jar...
%_COPYCMD% library.properties %JVM_LIBS_DIR%\scala-actors\ 1>nul
%_XCOPYCMD% scala\actors %JVM_LIBS_DIR%\scala-actors\scala\actors\ 1>nul

rmdir /s /q %JVM_LIBS_DIR%\scala-collection\scala\collection\immutable 1>nul
rmdir /s /q %JVM_LIBS_DIR%\scala-collection\scala\collection\mutable 1>nul
rmdir /s /q scala\collection 1>nul
rmdir /s /q scala\actors 1>nul

for %%d in (scala-library scala-collection scala-immutable scala-mutable scala-actors) do (
  @echo Converting %%d.jar into a dex file...
  cmd /c %_JARCMD% cf %JVM_LIBS_DIR%\%%d.jar -C %JVM_LIBS_DIR%\%%d .
  cmd /c %_DX% --dex --output=%DEX_LIBS_DIR%\%%d.jar %JVM_LIBS_DIR%\%%d.jar
)
%_COPYCMD% %JVM_LIBS_DIR%\scala-library\library.properties %DEX_LIBS_DIR%\ 1>nul
rmdir /s /q %TMP_DIR% 1>nul
echo Dex files were successfully generated (%DEX_LIBS_DIR2%)

cd %_WORKING_DIR%
goto end

rem ##########################################################################
rem # subroutines

rem Variable "%~dps0" works on WinXP SP2 or newer
rem (see http://support.microsoft.com/?kbid=833431)
rem set _ROOT=%~dps0..
:set_root
  set _BIN_DIR=
  for %%i in (%~sf0) do set _BIN_DIR=%_BIN_DIR%%%~dpsi
  set _ROOT=%_BIN_DIR%..
goto :eof

rem ##########################################################################
rem # errors

:error1
echo Error: environment variable ANDROID_SDK_ROOT is undefined. It should point to your installation directory.
goto end

:error2
echo Error: Emulator '%_EMULATOR%' is unknown.
goto end

:error3
echo Error: dx tool '%_DX%' is unknown.
goto end

:error4
echo Error: environment variable SCALA_HOME is undefined. It should point to your installation directory.
goto end

:error5
echo Error: jar tool '%_JARCMD%' is unknown.
goto end

:end
if "%OS%"=="Windows_NT" @endlocal
