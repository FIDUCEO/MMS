#!/usr/bin/env bash
git pull github master
mvn clean install package assembly:directory

bash backup_bin_to_bin_archive_with_timestamp.sh
# echo "clean up bin dir"
# rm -rf /group_workspaces/cems2/fiduceo/Software/mms/bin/*
echo "copy build result to bin dir"
cp -a target/fiduceo-master-1.4.1-MMS/* /group_workspaces/cems2/fiduceo/Software/mms/bin

