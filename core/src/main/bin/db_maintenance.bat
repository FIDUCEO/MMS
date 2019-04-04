@ECHO OFF

:: -======================-
:: User configurable values
:: -======================-

SET INSTALLDIR=%~dp0%

::------------------------------------------------------------------
:: You can adjust the Java minimum and maximum heap space here.
:: Just change the Xms and Xmx options. Space is given in megabyte.
::    '-Xms64M' sets the minimum heap space to 64 megabytes
::    '-Xmx512M' sets the maximum heap space to 512 megabytes
::------------------------------------------------------------------
SET JAVA_OPTS=-Xms64M -Xmx8192M


:: -======================-
:: Other values
:: -======================-

SET JAVAEXE=java.exe
SET LIBDIR=%INSTALLDIR%\lib
SET OLD_CLASSPATH=%CLASSPATH%

SET CLASSPATH=%LIBDIR%\*;%LIBDIR%

CALL "%JAVAEXE%" %JAVA_OPTS% -classpath "%CLASSPATH%" com.bc.fiduceo.db.DbMaintenanceToolMain %*

SET CLASSPATH=%OLD_CLASSPATH%
