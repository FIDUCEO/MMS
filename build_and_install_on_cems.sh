#!/usr/bin/env bash
git pull github master
mvn clean install package assembly:directory

# bash backup_bin_to_bin_archive_with_timestamp.sh
# echo "clean up bin dir"
echo "copy build result to bin dir"
rm -rf /gws/nopw/j04/fiduceo/Software/mms/bin/*
cp -a target/fiduceo-master-1.4.6-MMS/* /gws/nopw/j04/fiduceo/Software/mms/bin

