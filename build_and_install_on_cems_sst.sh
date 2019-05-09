#!/usr/bin/env bash
git pull github master
mvn clean install package assembly:directory

# bash backup_bin_to_bin_archive_with_timestamp_sst.sh
# echo "clean up bin dir"
echo "copy build result to bin dir"
rm -rf /gws/nopw/j04/esacci_sst/mms_new/bin/*
cp -a target/fiduceo-master-1.4.4-MMS/* /gws/nopw/j04/esacci_sst/mms_new/bin

