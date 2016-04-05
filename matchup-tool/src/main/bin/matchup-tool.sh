#!/bin/bash

# -======================-
# User configurable values
# -======================-

export INSTALLDIR="$(dirname $0)"

#------------------------------------------------------------------
# You can adjust the Java minimum and maximum heap space here.
# Just change the Xms and Xmx options. Space is given in megabyte.
#    '-Xms64M' sets the minimum heap space to 64 megabytes
#    '-Xmx512M' sets the maximum heap space to 512 megabytes
#------------------------------------------------------------------
export JAVA_OPTS="-Xmx8192M"

# check if we`re running on CEMS, if so take the java executable externally defined
if [ -z ${MMS_JAVA_EXEC} ]; then
    export JAVA_EXE="$(MMS_JAVA_EXEC)"
else
    export JAVA_EXE="$(which java)"
fi

# -======================-
# Other values
# -======================-

export LIBDIR="$INSTALLDIR"/lib
export OLD_CLASSPATH="$CLASSPATH"
CLASSPATH="$LIBDIR/*:$LIBDIR"

"$JAVA_EXE" "$JAVA_OPTS" -classpath "$CLASSPATH" com.bc.fiduceo.matchup.MatchupToolMain "$@"

export CLASSPATH="$OLD_CLASSPATH"
