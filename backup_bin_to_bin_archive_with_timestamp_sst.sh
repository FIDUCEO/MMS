#!/usr/bin/env bash
mmspath="/group_workspaces/cems2/esacci_sst/mms_new"
binpath=$mmspath/bin
binarcpath=$mmspath/bin_archive
bintime="$(find "$binpath" -name lib -type d -printf "%TF_%TT %f\n" | cut -c -19)"
echo "backup bin dir to bin_archive with timestamp $bintime"
mv "$binpath" "$binarcpath/${binpath##*/}-$bintime"
echo "... done"
